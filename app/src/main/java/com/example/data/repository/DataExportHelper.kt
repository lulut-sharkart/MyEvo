package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.data.model.DebtLoanItem
import com.example.data.model.DebtType
import com.example.data.model.FinanceItem
import com.example.data.model.FinanceType
import com.example.data.model.TaskItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExportHelper {

    fun generateTasksCsv(tasks: List<TaskItem>): String {
        val sb = StringBuilder()
        sb.append("ID,Judul Tugas,Deskripsi,Kategori,Prioritas,Tenggat Tanggal,Tenggat Waktu,Status Selesai,Pengingat (Menit),Tanggal Dibuat\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        tasks.forEach { task ->
            val id = task.id
            val title = escapeCsv(task.title)
            val desc = escapeCsv(task.description)
            val cat = task.category.name
            val priority = task.priority.name
            val dueDate = task.dueDate
            val dueTime = task.dueTime
            val status = if (task.isCompleted) "SELESAI" else "BELUM SELESAI"
            val reminder = "${task.reminderMinutesBefore}m"
            val created = dateFormat.format(Date(task.createdAt))

            sb.append("$id,$title,$desc,$cat,$priority,$dueDate,$dueTime,$status,$reminder,$created\n")
        }
        return sb.toString()
    }

    fun generateFinanceCsv(
        finances: List<FinanceItem>,
        debts: List<DebtLoanItem>
    ): String {
        val sb = StringBuilder()
        sb.append("ID,Tipe Transaksi,Kategori / Pihak,Nominal (Rp),Catatan,Tanggal,Bulan / Status Pelunasan\n")

        finances.forEach { f ->
            val id = "FIN-${f.id}"
            val type = if (f.type == FinanceType.INCOME) "PEMASUKAN" else "PENGELUARAN"
            val cat = f.category.name
            val amount = String.format(Locale.US, "%.2f", f.amount)
            val note = escapeCsv(f.note)
            val date = f.date
            val month = f.monthYear

            sb.append("$id,$type,$cat,$amount,$note,$date,$month\n")
        }

        debts.forEach { d ->
            val id = "DEBT-${d.id}"
            val type = if (d.type == DebtType.DEBT) "UTANG (HARUS DIBAYAR)" else "PIUTANG (AKAN DITERIMA)"
            val person = escapeCsv(d.personName)
            val amount = String.format(Locale.US, "%.2f", d.amount)
            val note = escapeCsv(d.note)
            val dueDate = d.dueDate
            val status = if (d.isSettled) "LUNAS" else "BELUM LUNAS"

            sb.append("$id,$type,$person,$amount,$note,$dueDate,$status\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }

    fun exportAndShareCsv(
        context: Context,
        baseFileName: String,
        csvContent: String
    ): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${baseFileName}_$timestamp.csv"

        val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { out ->
            out.write(csvContent.toByteArray(Charsets.UTF_8))
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Ekspor Data MyEvo: $fileName")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Simpan atau Bagikan Berkas CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // If sharing fails, file is still written to storage
        }

        return file
    }
}
