package com.example.growcalth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NapfaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NapfaScreen(semester = "s2-2024")
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
fun NapfaScreen(semester: String) {
    val db = FirebaseFirestore.getInstance()
    var resultsMap by remember { mutableStateOf<Map<String, List<Student>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(semester) {
        isLoading = true
        try {
            val snapshot = db.collection(semester).get().await()
            val tempMap = mutableMapOf<String, List<Student>>()

            for (doc in snapshot.documents) {
                val fieldMap = doc.data ?: continue
                for ((fieldName, value) in fieldMap) {
                    val list = (value as? List<*>)?.mapNotNull { raw ->
                        (raw as? String)?.split("___")?.let { parts ->
                            if (parts.size >= 4) {
                                Student(
                                    rank = parts[0],
                                    name = parts[1],
                                    className = parts[2],
                                    score = parts[3]
                                )
                            } else null
                        }
                    } ?: emptyList()
                    tempMap[fieldName] = list
                }
            }
            resultsMap = tempMap
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(resultsMap.toList()) { (testName, students) ->
                    TestSection(title = testName, results = students)
                }
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
        // Use replaceFirst instead of replaceFirstChar to avoid API 26+ requirement
        val formattedTitle = if (title.isNotEmpty()) {
            title.first().uppercaseChar() + title.drop(1)
        } else {
            title
        }

        Text(
            text = formattedTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "#",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = "Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "Class",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.2f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Score",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                // Data Rows
                results.forEachIndexed { index, student ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = student.rank,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            text = student.name,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = student.className,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = student.score,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }

                    if (index < results.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}