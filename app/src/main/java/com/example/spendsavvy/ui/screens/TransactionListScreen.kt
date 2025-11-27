package com.example.spendsavvy.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import android.graphics.Paint
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendsavvy.R
import com.example.spendsavvy.models.Transaction
import com.example.spendsavvy.utils.Utils
import com.example.spendsavvy.viewmodels.HomeScreenViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

@Composable
fun TransactionListScreen(
    navController: NavController, viewModel: HomeScreenViewModel = viewModel()
) {
    val state = viewModel.transactions.collectAsState()
    var filterType by remember { mutableStateOf("All") }
    var dateRange by remember { mutableStateOf("All Time") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) } // State for dialog visibility
    var selectedFormat by remember { mutableStateOf("") } // State for selected export format

    val filteredTransactions = when (filterType) {
        "Expense" -> state.value.filter { it.type == "Expense" }
        "Income" -> state.value.filter { it.type == "Income" }
        else -> state.value
    }

    val filteredByDateRange = filterTransactionsByDateRange(filteredTransactions, dateRange)

    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(Color.White),
            title = { Text("Transactions", style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { menuExpanded = !menuExpanded }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filter",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { showExportDialog = true }) { // Show the export dialog
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Export",
                        tint = Color.Black
                    )
                }
            })
    }, content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Show selected filters
            Text(
                text = "Filters: Type - $filterType, Date Range - $dateRange",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dropdown Menus
            AnimatedVisibility(visible = menuExpanded,
                enter = slideInVertically(initialOffsetY = { -it / 2 }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column {
                    // Type Filter Dropdown
                    ExpenseDropDown(listOfItems = listOf("All", "Expense", "Income"),
                        onItemSelected = { selected ->
                            filterType = selected
                            menuExpanded = false // Close menu after selection
                        })
                    Spacer(modifier = Modifier.height(8.dp))

                    // Date Range Filter Dropdown
                    ExpenseDropDown(listOfItems = listOf(
                        "All Time",
                        "Yesterday",
                        "Today",
                        "Last 30 Days",
                        "Last 90 Days",
                        "Last Year"
                    ), onItemSelected = { selected ->
                        dateRange = selected
                        menuExpanded = false // Close menu after selection
                    })
                }
            }

            // Transaction List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredByDateRange) { item ->
                    val icon = Utils.getItemIcon(item)
                    val amount = if (item.type == "Income") item.amount else item.amount * -1
                    TransactionItem(
                        title = item.title,
                        amount = amount.toString(),
                        icon = icon!!,
                        date = item.date,
                        color = if (item.type == "Income") Color(0xFF4CAF50) else Color(
                            0xFFF44336
                        ),
                        modifier = Modifier.animateItemPlacement(tween(100))
                    )
                }
            }

            // Show message if no transactions match the filters
            if (filteredByDateRange.isEmpty()) {
                Text(
                    text = "No transactions found.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                )
            }
        }

        // Export Dialog
        if (showExportDialog) {
            AlertDialog(onDismissRequest = { showExportDialog = false }, confirmButton = {
                TextButton(onClick = {
                    exportTransactions(
                        navController.context, filteredByDateRange, selectedFormat
                    )
                    showExportDialog = false // Close dialog after exporting
                }) {
                    Text("Export")
                }
            }, dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }, title = { Text("Export Transactions") }, text = {
                Column {
                    listOf("CSV", "PDF").forEach { format ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedFormat == format,
                                onClick = { selectedFormat = format })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(format)
                        }
                    }
                }
            })
        }
    }, containerColor = Color.White
    )
}


@RequiresApi(Build.VERSION_CODES.O)
fun filterTransactionsByDateRange(
    transactions: List<Transaction>, dateRange: String
): List<Transaction> {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val currentDate = LocalDate.now()

    val startDate: LocalDate? = when (dateRange) {
        "All Time" -> null
        "Today" -> currentDate
        "Yesterday" -> currentDate.minusDays(1)
        "Last 30 Days" -> currentDate.minusDays(30)
        "Last 90 Days" -> currentDate.minusDays(90)
        "Last Year" -> currentDate.minusYears(1)
        else -> null
    }

    return if (startDate == null) {
        transactions
    } else {
        transactions.filter { transaction ->
            val transactionDate = LocalDate.parse(transaction.date, formatter)
            when (dateRange) {
                "Yesterday" -> transactionDate == startDate
                else -> transactionDate >= startDate && transactionDate <= currentDate
            }
        }
    }
}

fun exportTransactions(context: Context, transactions: List<Transaction>, format: String) {
    when (format) {
        "CSV" -> exportTransactionsToCsv(context, transactions)
        "PDF" -> exportTransactionsToPdf(context, transactions)
        else -> Log.e("ExportTransactions", "Unsupported format: $format")
    }
}


fun exportTransactionsToCsv(context: Context, transactions: List<Transaction>) {
    val csvHeader = "Title,Amount,Date,Type\n"
    val csvContent = StringBuilder(csvHeader)

    // Add transactions as rows
    for (transaction in transactions) {
        csvContent.append("${transaction.title},${transaction.amount},${transaction.date},${transaction.type}\n")
    }

    try {
        val fileName = "transactions_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        file.writeText(csvContent.toString())

        // Share the file using an intent
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share CSV"))
    } catch (e: Exception) {
        Log.e("ExportTransactions", "Error exporting transactions to CSV: ${e.message}")
    }
}


fun exportTransactionsToPdf(context: Context, transactions: List<Transaction>) {
    val pdfDocument = PdfDocument()
    val paint = Paint()
    paint.textSize = 12f // Set text size for the Paint object
    paint.color = android.graphics.Color.BLACK
    val headerPaint = Paint() // Special paint for headers
    headerPaint.textSize = 14f
    headerPaint.color = android.graphics.Color.BLACK
    headerPaint.isFakeBoldText = true

    var y = 40 // Starting Y position for the first page
    var pageCount = 1 // To track page count

    // Create the first page
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, pageCount).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    // Title and header
    canvas.drawText("Transactions Report", 10f, y.toFloat(), headerPaint)
    y += 20

    // Draw table headers
    canvas.drawText("Title", 10f, y.toFloat(), headerPaint)
    canvas.drawText("Amount", 120f, y.toFloat(), headerPaint)
    canvas.drawText("Date", 180f, y.toFloat(), headerPaint)  // Adjusted for better spacing
    canvas.drawText("Type", 250f, y.toFloat(), headerPaint)  // Adjusted for better spacing
    y += 20

    // Draw a line under the headers
    paint.color = android.graphics.Color.GRAY
    canvas.drawLine(10f, y.toFloat(), 290f, y.toFloat(), paint)
    y += 10

    // Loop through the transactions and write them to the PDF
    for (transaction in transactions) {
        canvas.drawText(transaction.title, 10f, y.toFloat(), paint)
        canvas.drawText("${transaction.amount}", 120f, y.toFloat(), paint)
        canvas.drawText(transaction.date, 180f, y.toFloat(), paint)  // Adjusted to align correctly
        canvas.drawText(transaction.type, 250f, y.toFloat(), paint)  // Adjusted to align correctly
        y += 20

        // Handle page overflow by starting a new page if needed
        if (y > 550) { // Assuming a page height of 600
            pdfDocument.finishPage(page)
            pageCount++ // Increment page count
            y = 40 // Reset Y position for the new page

            // Create a new page and continue writing the report
            val newPageInfo = PdfDocument.PageInfo.Builder(300, 600, pageCount).create()
            page = pdfDocument.startPage(newPageInfo)
            canvas = page.canvas
            canvas.drawText("Transactions Report - Continued", 10f, y.toFloat(), headerPaint)
            y += 20

            // Draw table headers again on the new page
            canvas.drawText("Title", 10f, y.toFloat(), headerPaint)
            canvas.drawText("Amount", 120f, y.toFloat(), headerPaint)
            canvas.drawText("Date", 180f, y.toFloat(), headerPaint)  // Adjusted for better spacing
            canvas.drawText("Type", 250f, y.toFloat(), headerPaint)  // Adjusted for better spacing
            y += 20

            // Draw a line under the headers
            canvas.drawLine(10f, y.toFloat(), 290f, y.toFloat(), paint)
            y += 10
        }
    }

    // Finish the last page
    pdfDocument.finishPage(page)

    try {
        val fileName = "transactions_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)

        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        // Share the file using an intent
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    } catch (e: Exception) {
        Log.e("ExportTransactions", "Error exporting transactions to PDF: ${e.message}")
    }
}




