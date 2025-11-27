package com.example.spendsavvy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendsavvy.R
import com.example.spendsavvy.viewmodels.StatsScreenViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

// ViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController, viewModel: StatsScreenViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsState()
    val topExpenses by viewModel.topExpenses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(Color.White),
                title = { Text("Statistics", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ) {
                // LineChart with padding and header
                if (chartData.isNotEmpty()) {
                    Text(
                        text = "Expenses Over Time",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 5.dp).padding(horizontal = 10.dp)
                    )
                    LineChart(entries = chartData)
                } else {
                    Text(text = "Add Expenses to see statistics", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(10.dp)) // Increased space between chart and list

                // Display top expenses if available
                if (topExpenses.isNotEmpty()) {
                    TransactionList(
                        navController = navController,
                        modifier = Modifier.fillMaxWidth(),
                        list = topExpenses,
                        title = "Top Spending"
                    )
                } else {
                    Text(text = "No expenses available", color = Color.Gray)
                }
            }
        }
    }
}


@Composable
fun LineChart(entries: List<Pair<String, Double>>) {
    // Ensure there is data to plot
    if (entries.isEmpty()) {
        Text("No data to display", modifier = Modifier.fillMaxSize(), color = Color.Gray)
        return
    }

    // Convert Pair to Entry for MPAndroidChart
    val chartEntries = entries.mapIndexed { index, entry ->
        Entry(index.toFloat(), entry.second.toFloat())
    }

    val context = LocalContext.current
    AndroidView(
        factory = {
            val lineChart = LineChart(context)

            // Configure the LineChart programmatically
            val dataSet = LineDataSet(chartEntries, "Expenses").apply {
                color = android.graphics.Color.parseColor("#FF2F7E79")
                valueTextColor = android.graphics.Color.BLACK
                lineWidth = 3f
                axisDependency = YAxis.AxisDependency.RIGHT
                setDrawFilled(true)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                valueTextSize = 12f
                valueTextColor = android.graphics.Color.parseColor("#FF2F7E79")
                val drawable = ContextCompat.getDrawable(context, R.drawable.char_gradient)
                drawable?.let {
                    fillDrawable = it
                }
            }

            // Configure the chart
            lineChart.xAxis.valueFormatter =
                object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // Convert index to date string
                        val date = entries.getOrNull(value.toInt())?.first
                        return date ?: ""
                    }
                }

            // Configure the x-axis to display dates properly
            lineChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f  // Ensure only one label per data point
                setDrawAxisLine(true)
                setDrawGridLines(false)
            }

            // Configure the y-axis
            lineChart.axisLeft.isEnabled = false
            lineChart.axisRight.isEnabled = true
            lineChart.axisRight.setDrawGridLines(false)

            // Set the data to the chart
            lineChart.data = com.github.mikephil.charting.data.LineData(dataSet)

            // Update the chart
            lineChart
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // Set the height for the chart
            .padding(16.dp) // Added padding to ensure space around the chart
    ) { lineChart ->
        lineChart.invalidate() // Ensure chart is updated with new data
    }
}










