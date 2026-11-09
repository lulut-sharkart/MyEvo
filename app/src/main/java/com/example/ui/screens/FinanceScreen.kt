package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtLoanItem
import com.example.data.model.DebtType
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinanceItem
import com.example.data.model.FinanceType
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.SunsetPink
import com.example.viewmodel.MyEvoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    val financeList by viewModel.financeEntries.collectAsState()
    val debtsLoans by viewModel.debtsLoans.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Anggaran & Pengeluaran, 1: Utang & Piutang
    var showAddFinanceDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
    }

    val totalIncome = remember(financeList) {
        financeList.filter { it.type == FinanceType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(financeList) {
        financeList.filter { it.type == FinanceType.EXPENSE }.sumOf { it.amount }
    }
    val budgetProgress = remember(totalExpense, userProfile.monthlyBudgetGoal) {
        (totalExpense / userProfile.monthlyBudgetGoal).toFloat().coerceIn(0f, 1f)
    }

    val animatedBudgetProgress by animateFloatAsState(targetValue = budgetProgress, label = "budget")

    // Category breakdown
    val categoryBreakdown = remember(financeList) {
        val expenseEntries = financeList.filter { it.type == FinanceType.EXPENSE }
        val map = mutableMapOf<ExpenseCategory, Double>()
        expenseEntries.forEach { item ->
            map[item.category] = (map[item.category] ?: 0.0) + item.amount
        }
        map.toList().sortedByDescending { it.second }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                AppHeader(
                    title = "Keuangan & Anggaran",
                    subtitle = if (userProfile.isGuest) "Mode Tamu • Finansial Lokal" else "Pantau pengeluaran, utang & piutang",
                    isOnline = isOnline,
                    isSyncing = isSyncing,
                    isDarkMode = userProfile.isDarkMode,
                    isReadingMode = userProfile.isReadingMode,
                    isGuest = userProfile.isGuest,
                    onToggleOnline = { viewModel.toggleOnlineMode() },
                    onSyncClick = { viewModel.triggerCloudBackup() },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onToggleReadingMode = { viewModel.toggleReadingMode() },
                    onGuestBannerClick = { viewModel.navigateTo(com.example.viewmodel.AppScreen.PROFILE) }
                )
            }

            // Tabs: Anggaran vs Utang Piutang
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = NeonIndigo,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Anggaran & Pengeluaran", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_budget")
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Catatan Utang & Piutang", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_debt")
                    )
                }
            }

            if (activeTab == 0) {
                // TAB 0: ANGGARAN & PENGELUARAN
                // Monthly Budget Calculator Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("budget_calculator_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Kalkulator Anggaran Bulanan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Bulan November 2026",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { showEditBudgetDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Budget",
                                        tint = NeonIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Terpakai:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = currencyFormatter.format(totalExpense),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = PriorityHigh
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Batas Target:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = currencyFormatter.format(userProfile.monthlyBudgetGoal),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { animatedBudgetProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (budgetProgress > 0.85f) PriorityHigh else NeonIndigo,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val remaining = userProfile.monthlyBudgetGoal - totalExpense
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(budgetProgress * 100).toInt()}% terpakai",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (budgetProgress > 0.85f) PriorityHigh else NeonIndigo
                                )
                                Text(
                                    text = if (remaining >= 0) "Sisa: ${currencyFormatter.format(remaining)}" else "Overbudget: ${currencyFormatter.format(-remaining)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining >= 0) EmeraldGreen else PriorityHigh
                                )
                            }
                        }
                    }
                }

                // Income / Expense Summary Cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = "Pemasukan",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pemasukan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currencyFormatter.format(totalIncome),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PriorityHigh.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = "Pengeluaran",
                                        tint = PriorityHigh,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pengeluaran", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PriorityHigh)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currencyFormatter.format(totalExpense),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Category Distribution Chart Breakdown
                if (categoryBreakdown.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Grafik Visual Distribusi Pengeluaran",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                categoryBreakdown.forEach { (cat, amount) ->
                                    val pct = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                                    val catColor = when (cat) {
                                        ExpenseCategory.FOOD -> CoralOrange
                                        ExpenseCategory.BILLS -> ElectricCyan
                                        ExpenseCategory.TRANSPORT -> AmberGold
                                        ExpenseCategory.SHOPPING -> SunsetPink
                                        ExpenseCategory.HEALTH -> EmeraldGreen
                                        else -> NeonIndigo
                                    }

                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = cat.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = "${currencyFormatter.format(amount)} (${(pct * 100).toInt()}%)",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        LinearProgressIndicator(
                                            progress = { pct },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = catColor,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Transactions List
                item {
                    Text(
                        text = "Riwayat Transaksi Terkini (${financeList.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (financeList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("💳", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Belum ada transaksi bulan ini",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Catat pemasukan atau pengeluaran harianmu untuk mengontrol anggaran.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showAddFinanceDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Catat Transaksi Pertama")
                                }
                            }
                        }
                    }
                } else {
                    items(financeList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.type == FinanceType.INCOME) EmeraldGreen.copy(alpha = 0.2f) else PriorityHigh.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.type == FinanceType.INCOME) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (item.type == FinanceType.INCOME) EmeraldGreen else PriorityHigh,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.note.ifBlank { item.category.name },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.category.name} • ${item.date}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = (if (item.type == FinanceType.INCOME) "+ " else "- ") + currencyFormatter.format(item.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (item.type == FinanceType.INCOME) EmeraldGreen else PriorityHigh
                            )

                            IconButton(
                                onClick = { viewModel.deleteFinance(item) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    }
                }
            } else {
                // TAB 1: UTANG & PIUTANG (DEBT & LOAN TRACKER)
                item {
                    val totalDebt = debtsLoans.filter { it.type == DebtType.DEBT && !it.isSettled }.sumOf { it.amount }
                    val totalLoan = debtsLoans.filter { it.type == DebtType.LOAN && !it.isSettled }.sumOf { it.amount }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PriorityHigh.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Utang Saya (Wajib Bayar)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PriorityHigh)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currencyFormatter.format(totalDebt),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Piutang (Hak Tagih)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currencyFormatter.format(totalLoan),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Daftar Pinjaman & Utang (${debtsLoans.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (debtsLoans.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🤝", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tidak ada catatan utang atau piutang",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Keuangan Anda bersih! Catat jika meminjam atau meminjamkan uang.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showAddDebtDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Catatan Pinjaman")
                                }
                            }
                        }
                    }
                } else {
                    items(debtsLoans, key = { it.id }) { debtItem ->
                        DebtLoanCard(
                            item = debtItem,
                            currencyFormatter = currencyFormatter,
                            onToggleSettled = { viewModel.toggleDebtLoanSettled(debtItem) },
                            onDelete = { viewModel.deleteDebtLoan(debtItem) }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = {
                if (activeTab == 0) showAddFinanceDialog = true else showAddDebtDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_finance_fab"),
            containerColor = NeonIndigo,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
        }

        if (showAddFinanceDialog) {
            AddFinanceDialog(
                onDismiss = { showAddFinanceDialog = false },
                onAdd = { type, cat, amt, note, date ->
                    viewModel.addFinance(type, cat, amt, note, date)
                    showAddFinanceDialog = false
                }
            )
        }

        if (showAddDebtDialog) {
            AddDebtDialog(
                onDismiss = { showAddDebtDialog = false },
                onAdd = { type, person, amt, due, note, phone ->
                    viewModel.addDebtLoan(type, person, amt, due, note, phone)
                    showAddDebtDialog = false
                }
            )
        }

        if (showEditBudgetDialog) {
            EditBudgetDialog(
                currentGoal = userProfile.monthlyBudgetGoal,
                onDismiss = { showEditBudgetDialog = false },
                onSave = { newGoal ->
                    viewModel.updateMonthlyBudget(newGoal)
                    showEditBudgetDialog = false
                }
            )
        }
    }
}

@Composable
fun DebtLoanCard(
    item: DebtLoanItem,
    currencyFormatter: NumberFormat,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("debt_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleSettled,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (item.isSettled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Settled",
                    tint = if (item.isSettled) EmeraldGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusPill(
                        text = if (item.type == DebtType.DEBT) "Utang Saya" else "Piutang Orang",
                        color = if (item.type == DebtType.DEBT) PriorityHigh else EmeraldGreen
                    )
                    if (item.isSettled) {
                        StatusPill(text = "LUNAS", color = EmeraldGreen)
                    } else {
                        StatusPill(text = "Jatuh Tempo: ${item.dueDate}", color = AmberGold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.personName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.contactPhone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = ElectricCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.contactPhone,
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(item.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (item.type == DebtType.DEBT) PriorityHigh else EmeraldGreen
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddFinanceDialog(
    onDismiss: () -> Unit,
    onAdd: (type: FinanceType, cat: ExpenseCategory, amount: Double, note: String, date: String) -> Unit
) {
    var type by remember { mutableStateOf(FinanceType.EXPENSE) }
    var category by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Transaksi Keuangan") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = FinanceType.EXPENSE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == FinanceType.EXPENSE) PriorityHigh else Color.Gray.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pengeluaran")
                    }
                    Button(
                        onClick = { type = FinanceType.INCOME },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == FinanceType.INCOME) EmeraldGreen else Color.Gray.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pemasukan")
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Jumlah (Rp)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("finance_amount_input")
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keterangan / Catatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selection
                Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ExpenseCategory.values().toList()) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onAdd(type, category, amt, note, date)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                Text("Simpan Transaksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onAdd: (type: DebtType, person: String, amount: Double, dueDate: String, note: String, phone: String) -> Unit
) {
    var type by remember { mutableStateOf(DebtType.LOAN) }
    var personName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-11-30") }
    var note by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Utang / Piutang") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = DebtType.LOAN },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == DebtType.LOAN) EmeraldGreen else Color.Gray.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Piutang (Hak Tagih)", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { type = DebtType.DEBT },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == DebtType.DEBT) PriorityHigh else Color.Gray.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Utang Saya", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Nama Pihak / Teman / Toko") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Nominal (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Batas Jatuh Tempo (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. WhatsApp / HP (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keperluan / Keterangan") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (personName.isNotBlank() && amt > 0) {
                        onAdd(type, personName, amt, dueDate, note, phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                Text("Simpan Catatan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun EditBudgetDialog(
    currentGoal: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var goalStr by remember { mutableStateOf(currentGoal.toLong().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Target Anggaran Bulanan") },
        text = {
            Column {
                Text(
                    text = "Tentukan batas pengeluaran maksimal setiap bulan agar keuangan terkontrol dan tidak overbudget.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = goalStr,
                    onValueChange = { goalStr = it },
                    label = { Text("Batas Anggaran Bulanan (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val g = goalStr.toDoubleOrNull() ?: currentGoal
                    onSave(g)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                Text("Simpan Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
