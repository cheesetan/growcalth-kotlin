package com.example.growcalth

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class NapfaActivity : ComponentActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        setContent {
            NapfaScreen(db)
        }
    }
}

data class Student(
    val rank: String = "",
    val name: String = "",
    val className: String = "",
    val score: String = "",
    val id: String = ""
) {
    constructor() : this("", "", "", "", "")

    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "rank" to rank,
            "name" to name,
            "className" to className,
            "score" to score
        )
    }
}

data class TestResults(
    val testName: String,
    val students: List<Student>
)

@Composable
fun NapfaScreen(db: FirebaseFirestore) {
    var availableSemesters by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentSemesterIndex by remember { mutableStateOf(0) }
    var testResults by remember { mutableStateOf<List<TestResults>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingData by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val currentSemester = if (availableSemesters.isNotEmpty()) availableSemesters[currentSemesterIndex] else ""

    // Load available semesters when the composable is first created
    LaunchedEffect(Unit) {
        try {
            Log.d("NapfaActivity", "Loading available semesters...")

            val querySnapshot = db.collection("napfa")
                .get()
                .await()

            val semesters = querySnapshot.documents.map { it.id }.sorted()
            availableSemesters = semesters

            Log.d("NapfaActivity", "Found semesters: $semesters")

            if (semesters.isNotEmpty()) {
                // Load data for the first semester - this will be handled by LaunchedEffect(currentSemester)
                isLoading = false
            } else {
                error = "No semesters found"
                isLoading = false
            }
        } catch (e: Exception) {
            Log.e("NapfaActivity", "Error loading semesters", e)
            error = "Failed to load semesters: ${e.message}"
            isLoading = false
        }
    }

    // Load data when semester changes
    LaunchedEffect(currentSemester) {
        if (currentSemester.isNotEmpty() && !isLoading) {
            isLoadingData = true
            try {
                Log.d("NapfaActivity", "Loading data for semester: $currentSemester")

                val documentSnapshot = db.collection("napfa")
                    .document(currentSemester)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data ?: emptyMap()
                    Log.d("NapfaActivity", "Document data: $data")

                    val results = mutableListOf<TestResults>()

                    // Process all fields in the document (excluding metadata fields)
                    data.entries.forEach { (fieldName, fieldData) ->
                        if (fieldName != "timestamp" && fieldName != "createdBy") { // Skip metadata fields
                            Log.d("NapfaActivity", "Processing field: $fieldName, data: $fieldData")

                            val students = when (fieldData) {
                                is Map<*, *> -> parseStudentData(fieldData as Map<String, Any>, fieldName)
                                is List<*> -> parseStudentList(fieldData as List<String>, fieldName)
                                else -> {
                                    Log.w("NapfaActivity", "Unexpected data format for $fieldName: $fieldData")
                                    emptyList()
                                }
                            }

                            if (students.isNotEmpty()) {
                                results.add(TestResults(fieldName, students))
                            }
                        }
                    }

                    Log.d("NapfaActivity", "Parsed ${results.size} test categories with total ${results.sumOf { it.students.size }} students")
                    testResults = results
                    error = null
                } else {
                    Log.w("NapfaActivity", "Document $currentSemester does not exist")
                    testResults = emptyList()
                    error = "No data found for semester $currentSemester"
                }
            } catch (e: Exception) {
                Log.e("NapfaActivity", "Error loading data for semester $currentSemester", e)
                testResults = emptyList()
                error = "Failed to load data: ${e.message}"
            }
            isLoadingData = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading semesters...")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Semester Navigation Header
                SemesterNavigationHeader(
                    currentSemester = currentSemester,
                    currentIndex = currentSemesterIndex,
                    totalSemesters = availableSemesters.size,
                    isLoading = isLoadingData,
                    onPrevious = {
                        if (currentSemesterIndex > 0) {
                            currentSemesterIndex--
                        }
                    },
                    onNext = {
                        if (currentSemesterIndex < availableSemesters.size - 1) {
                            currentSemesterIndex++
                        }
                    },
                    onRefresh = {
                        isLoadingData = true
                        // Trigger refresh by creating a new coroutine
                    }
                )

                // Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    if (error != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    // Display all test results
                    items(testResults.size) { index ->
                        val testResult = testResults[index]
                        TestSection(
                            title = formatTestName(testResult.testName),
                            results = testResult.students,
                            isLoading = isLoadingData
                        )
                    }

                    if (testResults.isEmpty() && error == null && !isLoadingData) {
                        item {
                            Card {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No test results found for $currentSemester",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterNavigationHeader(
    currentSemester: String,
    currentIndex: Int,
    totalSemesters: Int,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            IconButton(
                onClick = onPrevious,
                enabled = currentIndex > 0 && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous semester"
                )
            }

            // Current semester info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentSemester.uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${currentIndex + 1} of $totalSemesters",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(top = 4.dp),
                        // strokeCap = StrokeCap.Round
                    )
                }
            }

            // Next and Refresh buttons
            Row {
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh data"
                    )
                }

                IconButton(
                    onClick = onNext,
                    enabled = currentIndex < totalSemesters - 1 && !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next semester"
                    )
                }
            }
        }
    }
}

// Load data for a specific semester
// This function has been moved inline to the LaunchedEffect

// Helper function to format test names for display
fun formatTestName(testName: String): String {
    return when (testName.lowercase()) {
        "2.4km" -> "2.4km Run"
        "inclinedpullups" -> "Inclined Pull-ups"
        "pullups" -> "Pull-ups"
        "situps" -> "Sit-ups"
        "standingbroad" -> "Standing Broad Jump"
        "sitandreach" -> "Sit and Reach"
        else -> testName.replaceFirstChar { it.uppercase() }.replace("_", " ")
    }
}

// Helper function to parse student data from Firebase format (Map)
private fun parseStudentData(data: Map<String, Any>, testType: String): List<Student> {
    Log.d("NapfaActivity", "Parsing map data with ${data.size} entries for $testType")

    return data.entries.mapIndexed { index, (key, value) ->
        Log.d("NapfaActivity", "Processing entry: key=$key, value=$value")

        val valueStr = value.toString()
        val parts = valueStr.split("___")

        if (parts.size >= 4) {
            Student(
                rank = "#${parts[0]}",
                name = parts[1],
                className = parts[2],
                score = parts[3],
                id = key
            )
        } else {
            Log.w("NapfaActivity", "Unexpected format for $key: $valueStr (${parts.size} parts)")
            Student(
                rank = "#${index + 1}",
                name = valueStr.substringBefore("___").ifEmpty { key },
                className = "Unknown",
                score = valueStr.substringAfterLast("___").ifEmpty { valueStr },
                id = key
            )
        }
    }.sortedBy { it.rank.removePrefix("#").toIntOrNull() ?: 999 }
}

// Helper function to parse student data from Firebase format (List)
private fun parseStudentList(data: List<String>, testType: String): List<Student> {
    Log.d("NapfaActivity", "Parsing list data with ${data.size} entries for $testType")

    return data.mapIndexed { index, value ->
        Log.d("NapfaActivity", "Processing list entry: $value")

        val parts = value.split("___")
        if (parts.size >= 4) {
            Student(
                rank = "#${parts[0]}",
                name = parts[1],
                className = parts[2],
                score = parts[3],
                id = index.toString()
            )
        } else {
            Student(
                rank = "#${index + 1}",
                name = value.substringBefore("___").ifEmpty { "Student ${index + 1}" },
                className = "Unknown",
                score = value.substringAfterLast("___").ifEmpty { value },
                id = index.toString()
            )
        }
    }.sortedBy { it.rank.removePrefix("#").toIntOrNull() ?: 999 }
}

@Composable
fun TestSection(
    title: String,
    results: List<Student>,
    isLoading: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (results.isNotEmpty()) {
                Text(
                    text = "${results.size} students",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Table
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

                // Loading state
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                } else {
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
                                modifier = Modifier.weight(0.8f),
                                fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
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
                                textAlign = TextAlign.End,
                                fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        // Divider (only if not the last item)
                        if (index < results.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp
                            )
                        }
                    }

                    // Show message if no results
                    if (results.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}