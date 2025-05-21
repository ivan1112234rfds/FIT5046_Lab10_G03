package com.example.activitymanager

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import com.example.activitymanager.BottomNavigationBar

@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf("Dashboard") }

    val quarterStats = viewModel.quarterStats
    val typeStats = viewModel.typeStats
    Log.d("DashboardStats", "Quarter Stats: $quarterStats")
    Log.d("DashboardStats", "Type Stats: $typeStats")

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                navController = navController
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.Black
                )
            )

            // 🟦 Bar Chart (Quarter)
            Card(
                modifier = Modifier.fillMaxWidth().height(280.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                var barChartRef: BarChart? = null

                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    factory = { context ->
                        BarChart(context).also { barChartRef = it }
                    },
                    update = { chart ->
                        val entries = quarterStats.mapIndexed { index, stat ->
                            BarEntry(index.toFloat(), stat.count.toFloat())
                        }
                        val labels = quarterStats.map { it.quarter }

                        val dataSet = BarDataSet(entries, "Quarterly activity statistics").apply {
                            color = Color.rgb(100, 149, 237)
                        }

                        val barData = BarData(dataSet).apply { barWidth = 0.9f }

                        chart.data = barData
                        chart.setFitBars(true)
                        chart.description.isEnabled = false
                        chart.animateY(1000)

                        chart.xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelCount = entries.size
                            valueFormatter = IndexAxisValueFormatter(labels)
                        }

                        chart.invalidate()
                    }
                )
            }

            // 🟧 Pie Chart (Type)
            Card(
                modifier = Modifier.fillMaxWidth().height(280.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    factory = { context ->
                        PieChart(context)
                    },
                    update = { chart ->
                        val entries = typeStats.map {
                            PieEntry(it.count.toFloat(), it.type)
                        }

                        val pieDataSet = PieDataSet(entries, "Participate in activity statistics").apply {
                            colors = ColorTemplate.MATERIAL_COLORS.toList()
                            sliceSpace = 3f
                            selectionShift = 5f
                        }

                        val pieData = PieData(pieDataSet).apply {
                            setValueTextSize(14f)
                        }

                        chart.data = pieData
                        chart.description.isEnabled = false
                        chart.isDrawHoleEnabled = true
                        chart.holeRadius = 40f
                        chart.animateY(1000)

                        chart.invalidate()
                    }
                )
            }
        }
    }
}

