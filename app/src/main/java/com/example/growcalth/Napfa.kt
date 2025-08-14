package com.example.growcalth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class NapfaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NapfaScreen()
        }
    }
}

data class Student(
    val rank: String,
    val name: String,
    val className: String,
    val score: String
)

@Composable
fun NapfaScreen() {
    val sitUpResults = listOf(
        Student("#1", "Scoobert", "S201", "100"),
        Student("#2", "Washington", "S202", "90"),
        Student("#3", "Gmail", "S204", "80"),
        Student("#4", "Bnuuy", "S210", "78"),
        Student("#5", "Mulch", "S209", "68")
    )

    val shuttleRunResults = listOf(
        Student("#1", "Scoobert", "S201", "1.0s"),
        Student("#2", "Washington", "S202", "2.9s"),
        Student("#3", "Gmail", "S204", "3.1s"),
        Student("#4", "Bnuuy", "S210", "3.3s"),
        Student("#5", "Mulch", "S209", "4.0s")
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                // Sit ups Section
                TestSection(
                    title = "Sit ups",
                    results = sitUpResults
                )
            }

            item {
                // Shuttle Run Section
                TestSection(
                    title = "Shuttle Run",
                    results = shuttleRunResults
                )
            }
        }
    }
}

@Composable
fun TestSection(
    title: String,
    results: List<Student>
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Table
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = "Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Class",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.weight(1.2f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Score",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            // Data Rows
            results.forEach { student ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.rank,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = student.name,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = student.className,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1.2f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = student.score,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                // Divider
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp
                )
            }
        }
    }
}