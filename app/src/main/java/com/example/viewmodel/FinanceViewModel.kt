package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.FinanceDao
import com.example.data.local.MyEvoDatabase
import com.example.data.model.DebtLoanItem
import com.example.data.model.DebtType
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinanceItem
import com.example.data.model.FinanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FinanceViewModel links the Room FinanceDao with the UI, ensuring proper lifecycle
 * management through viewModelScope and reactive StateFlows for monthly budget calculations
 * and debt/loan tracking.
 */
class FinanceViewModel(
    private val financeDao: FinanceDao,
    application: Application? = null
) : ViewModel() {

    // Target monthly budget limit (Default IDR 3,000,000)
    private val _monthlyBudgetGoal = MutableStateFlow(3000000.0)
    val monthlyBudgetGoal: StateFlow<Double> = _monthlyBudgetGoal.asStateFlow()

    // Active selected month filter (YYYY-MM)
    private val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    private val _selectedMonth = MutableStateFlow(currentMonthYear)
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // 1. Raw Data Streams from Room DAO
    val financeEntries: StateFlow<List<FinanceItem>> = financeDao.getAllFinanceEntries()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val debtsLoans: StateFlow<List<DebtLoanItem>> = financeDao.getAllDebtsLoans()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Computed Monthly Totals & Calculations
    val totalIncome: StateFlow<Double> = financeEntries
        .combine(_selectedMonth) { entries, month ->
            entries.filter { it.type == FinanceType.INCOME && it.monthYear == month }
                .sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = financeEntries
        .combine(_selectedMonth) { entries, month ->
            entries.filter { it.type == FinanceType.EXPENSE && it.monthYear == month }
                .sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netSavings: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val remainingBudget: StateFlow<Double> = combine(_monthlyBudgetGoal, totalExpense) { goal, expense ->
        (goal - expense).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3000000.0)

    val budgetUsagePercent: StateFlow<Float> = combine(_monthlyBudgetGoal, totalExpense) { goal, expense ->
        if (goal <= 0.0) 0f else (expense / goal).toFloat().coerceIn(0f, 1f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // 3. Computed Debt & Loan Totals (Active / Unsettled)
    val totalDebt: StateFlow<Double> = debtsLoans
        .combine(_debtsFilter) { list, _ ->
            list.filter { it.type == DebtType.DEBT && !it.isSettled }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLoan: StateFlow<Double> = debtsLoans
        .combine(_debtsFilter) { list, _ ->
            list.filter { it.type == DebtType.LOAN && !it.isSettled }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Expense breakdown by category
    val categoryExpenses: StateFlow<Map<ExpenseCategory, Double>> = financeEntries
        .combine(_selectedMonth) { entries, month ->
            entries
                .filter { it.type == FinanceType.EXPENSE && it.monthYear == month }
                .groupBy { it.category }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // UI state message / alerts
    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    fun setSelectedMonth(monthYear: String) {
        _selectedMonth.value = monthYear
    }

    fun updateMonthlyBudgetGoal(goal: Double) {
        _monthlyBudgetGoal.value = goal.coerceAtLeast(100000.0)
        _actionFeedback.value = "Target anggaran berhasil diperbarui!"
    }

    // --- Finance Transaction CRUD Operations ---

    fun addFinance(
        type: FinanceType,
        category: ExpenseCategory,
        amount: Double,
        note: String,
        date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    ) {
        viewModelScope.launch {
            val monthYear = if (date.length >= 7) date.substring(0, 7) else currentMonthYear
            val item = FinanceItem(
                type = type,
                category = category,
                amount = amount,
                note = note,
                date = date,
                monthYear = monthYear
            )
            financeDao.insertFinance(item)
            _actionFeedback.value = if (type == FinanceType.INCOME) "Pemasukan berhasil dicatat!" else "Pengeluaran berhasil dicatat!"
        }
    }

    fun deleteFinance(item: FinanceItem) {
        viewModelScope.launch {
            financeDao.deleteFinance(item)
            _actionFeedback.value = "Transaksi dihapus."
        }
    }

    fun clearAllFinance() {
        viewModelScope.launch {
            financeDao.clearAllFinance()
            _actionFeedback.value = "Seluruh riwayat transaksi telah dibersihkan."
        }
    }

    // --- Debt & Loan CRUD Operations ---

    fun addDebtLoan(
        type: DebtType,
        personName: String,
        amount: Double,
        dueDate: String,
        note: String = "",
        contactPhone: String = ""
    ) {
        viewModelScope.launch {
            val item = DebtLoanItem(
                type = type,
                personName = personName,
                amount = amount,
                dueDate = dueDate,
                note = note,
                contactPhone = contactPhone
            )
            financeDao.insertDebtLoan(item)
            _actionFeedback.value = if (type == DebtType.DEBT) "Catatan utang tersimpan!" else "Catatan piutang tersimpan!"
        }
    }

    fun toggleDebtLoanSettled(item: DebtLoanItem) {
        viewModelScope.launch {
            val updated = item.copy(isSettled = !item.isSettled)
            financeDao.updateDebtLoan(updated)
            _actionFeedback.value = if (updated.isSettled) "Ditandai telah lunas!" else "Status lunas dibatalkan."
        }
    }

    fun updateDebtLoan(item: DebtLoanItem) {
        viewModelScope.launch {
            financeDao.updateDebtLoan(item)
        }
    }

    fun deleteDebtLoan(item: DebtLoanItem) {
        viewModelScope.launch {
            financeDao.deleteDebtLoan(item)
            _actionFeedback.value = "Catatan pinjaman dihapus."
        }
    }

    fun clearAllDebtsLoans() {
        viewModelScope.launch {
            financeDao.clearAllDebtsLoans()
            _actionFeedback.value = "Seluruh catatan utang/piutang telah dibersihkan."
        }
    }

    companion object {
        private val _debtsFilter = MutableStateFlow(Unit)

        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = MyEvoDatabase.getDatabase(application)
                    return FinanceViewModel(db.financeDao(), application) as T
                }
            }
    }
}
