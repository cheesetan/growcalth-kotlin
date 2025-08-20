package com.example.growcalth

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NapfaScreen(db: FirebaseFirestore) {
    var availableYears by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentYearIndex by remember { mutableStateOf(0) }
    var testResults by remember { mutableStateOf<List<TestResults>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingData by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedLevel by remember { mutableStateOf("Sec 2") }
    var showYearDropdown by remember { mutableStateOf(false) }

    val currentYear = if (availableYears.isNotEmpty()) availableYears[currentYearIndex] else ""

    // Load available years when the composable is first created
    LaunchedEffect(Unit) {
        try {
            Log.d("NapfaActivity", "Loading NAPFA data...")

            val querySnapshot = db.collection("schools")
                .document("sst")
                .collection("napfa")
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val documentIds = querySnapshot.documents.map { it.id }
                Log.d("NapfaActivity", "Found documents: $documentIds")

                // Extract unique years from document IDs (s2-2023, s4-2024, etc.)
                val years = documentIds
                    .mapNotNull { documentId ->
                        // Extract year from document IDs like "s2-2023", "s4-2024"
                        val parts = documentId.split("-")
                        if (parts.size == 2) parts[1] else null
                    }
                    .distinct()
                    .sorted()

                availableYears = years
                Log.d("NapfaActivity", "Found years: $years")

                if (years.isNotEmpty()) {
                    currentYearIndex = years.size - 1 // Start with the latest year
                    isLoading = false
                } else {
                    error = "No NAPFA data found"
                    isLoading = false
                }
            } else {
                error = "No NAPFA documents found"
                isLoading = false
            }
        } catch (e: Exception) {
            Log.e("NapfaActivity", "Error loading NAPFA data", e)
            error = "Failed to load data: ${e.message}"
            isLoading = false
        }
    }

    // Load data when year or level changes
    LaunchedEffect(currentYear, selectedLevel) {
        if (currentYear.isNotEmpty() && !isLoading) {
            isLoadingData = true
            try {
                Log.d("NapfaActivity", "Loading data for year: $currentYear, level: $selectedLevel")

                // Determine the document ID based on selected level and year
                val levelPrefix = when (selectedLevel) {
                    "Sec 2" -> "s2"
                    "Sec 4" -> "s4"
                    else -> "s2"
                }
                val documentId = "$levelPrefix-$currentYear"

                Log.d("NapfaActivity", "Looking for document: $documentId")

                val documentSnapshot = db.collection("schools")
                    .document("sst")
                    .collection("napfa")
                    .document(documentId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data ?: emptyMap()
                    Log.d("NapfaActivity", "Found data for $documentId: ${data.keys}")

                    val results = mutableListOf<TestResults>()

                    data.entries.forEach { (testName, testData) ->
                        if (testName != "timestamp" && testName != "createdBy") {
                            Log.d("NapfaActivity", "Processing test: $testName, data: $testData")

                            val students = when (testData) {
                                is Map<*, *> -> parseStudentData(testData as Map<String, Any>, testName)
                                is List<*> -> parseStudentList(testData as List<String>, testName)
                                else -> {
                                    Log.w("NapfaActivity", "Unexpected data format for $testName: $testData")
                                    emptyList()
                                }
                            }

                            if (students.isNotEmpty()) {
                                results.add(TestResults(testName, students))
                            }
                        }
                    }

                    Log.d("NapfaActivity", "Parsed ${results.size} test categories")
                    testResults = results
                    error = null
                } else {
                    Log.w("NapfaActivity", "No document found for $documentId")
                    testResults = emptyList()
                    error = "No data found for $selectedLevel in $currentYear"
                }
            } catch (e: Exception) {
                Log.e("NapfaActivity", "Error loading data for year $currentYear", e)
                testResults = emptyList()
                error = "Failed to load data: ${e.message}"
            }
            isLoadingData = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {

            // Year Dropdown and Level Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year Dropdown
                ExposedDropdownMenuBox(
                    expanded = showYearDropdown,
                    onExpandedChange = { showYearDropdown = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "Year $currentYear",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showYearDropdown)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFDFDFE5),
                            focusedBorderColor = Color(0xFFDFDFE5),
                            unfocusedContainerColor = Color(0xFFDFDFE5),
                            focusedContainerColor = Color(0xFFDFDFE5)
                        ),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = showYearDropdown,
                        onDismissRequest = { showYearDropdown = false }
                    ) {
                        availableYears.forEachIndexed { index, year ->
                            DropdownMenuItem(
                                text = { Text("Year $year") },
                                onClick = {
                                    currentYearIndex = index
                                    showYearDropdown = false
                                }
                            )
                        }
                    }
                }

                // Level Selection Tabs
                LevelSelectionTabs(
                    selectedLevel = selectedLevel,
                    onLevelSelected = { selectedLevel = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (error != null) {
                    item {
                        ErrorCard(error = error!!)
                    }
                } else if (testResults.isNotEmpty()) {
                    items(testResults.size) { index ->
                        val testResult = testResults[index]
                        ModernTestSection(
                            title = formatTestName(testResult.testName),
                            results = testResult.students,
                            isLoading = isLoadingData
                        )
                    }
                } else if (!isLoadingData) {
                    item {
                        EmptyStateCard(year = currentYear, level = selectedLevel)
                    }
                }

                // My Records Section
                item {
                    MyRecordsSection()
                }
            }
        }
    }
}


@Composable
fun LevelSelectionTabs(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val levels = listOf("Sec 2", "Sec 4")

        levels.forEach { level ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (selectedLevel == level) Color(0xFF4F4F52)
                        else Color(0xFFDFDFE5)
                    )
                    .clickable { onLevelSelected(level) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedLevel == level) Color.White else Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
fun ModernTestSection(
    title: String,
    results: List<Student>,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Title
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = "Name",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Class",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Score",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
            }

            // Results
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.Red,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                results.take(5).forEachIndexed { index, student ->
                    StudentResultRow(
                        student = student,
                        position = index + 1
                    )
                    if (index < results.take(5).size - 1) {
                        HorizontalDivider(
                            color = Color(0xFFE0E0E0),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentResultRow(
    student: Student,
    position: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        Text(
            text = "#$position",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.width(32.dp)
        )

        // Name
        Text(
            text = student.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        // Class
        Text(
            text = student.className,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.Center
        )

        // Score
        Text(
            text = student.score,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun MyRecordsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "My Records",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = error,
            modifier = Modifier.padding(20.dp),
            color = Color.Red,
            fontSize = 16.sp
        )
    }
}

@Composable
fun EmptyStateCard(year: String, level: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No test results found for $level in $year",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper function to format test names for display
fun formatTestName(testName: String): String {
    return when (testName.lowercase()) {
        "2.4km" -> "2.4KM RUN"
        "inclinedpullups" -> "Inclined Pull-ups"
        "pullups" -> "Pull-ups"
        "situps" -> "Sit ups"
        "standingbroad" -> "Standing Broad Jump"
        "sitandreach" -> "Sit and Reach"
        "shuttlerun" -> "Shuttle Run"
        else -> testName.split("(?=[A-Z])".toRegex()).joinToString(" ").lowercase()
            .split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
    }
}

// Helper function to parse student data from Firebase format (Map)
private fun parseStudentData(data: Map<String, Any>, testType: String): List<Student> {
    Log.d("NapfaActivity", "Parsing map data with ${data.size} entries for $testType")

    return data.entries.mapIndexed { index, (key, value) ->
        Log.d("NapfaActivity", "Processing entry: key=$key, value=$value")

        val valueStr = value.toString()
        val parts = valueStr.split("___") // Use triple underscore as separator

        if (parts.size >= 4) {
            Student(
                rank = parts[0],
                name = parts[1],
                className = parts[2],
                score = parts[3],
                id = key
            )
        } else if (parts.size == 3) {
            // Handle cases where rank might be missing or different format
            Student(
                rank = "${index + 1}",
                name = parts[0],
                className = parts[1],
                score = parts[2],
                id = key
            )
        } else {
            Log.w("NapfaActivity", "Unexpected format for $key: $valueStr (${parts.size} parts)")
            // Fallback parsing with single underscore
            val singleParts = valueStr.split("_")
            if (singleParts.size >= 4) {
                Student(
                    rank = singleParts[0],
                    name = singleParts[1],
                    className = singleParts[2],
                    score = singleParts.drop(3).joinToString("_"), // Join remaining parts for score
                    id = key
                )
            } else {
                Student(
                    rank = "${index + 1}",
                    name = valueStr.substringBefore("_").ifEmpty { key },
                    className = "Unknown",
                    score = valueStr.substringAfterLast("_").ifEmpty { valueStr },
                    id = key
                )
            }
        }
    }.sortedBy { it.rank.toIntOrNull() ?: 999 }
}

// Helper function to parse student data from Firebase format (List)
private fun parseStudentList(data: List<String>, testType: String): List<Student> {
    Log.d("NapfaActivity", "Parsing list data with ${data.size} entries for $testType")

    return data.mapIndexed { index, value ->
        Log.d("NapfaActivity", "Processing list entry: $value")

        val parts = value.split("___") // Use triple underscore as separator
        if (parts.size >= 4) {
            Student(
                rank = parts[0],
                name = parts[1],
                className = parts[2],
                score = parts[3],
                id = index.toString()
            )
        } else if (parts.size == 3) {
            Student(
                rank = "${index + 1}",
                name = parts[0],
                className = parts[1],
                score = parts[2],
                id = index.toString()
            )
        } else {
            // Fallback parsing with single underscore
            val singleParts = value.split("_")
            if (singleParts.size >= 4) {
                Student(
                    rank = singleParts[0],
                    name = singleParts[1],
                    className = singleParts[2],
                    score = singleParts.drop(3).joinToString("_"),
                    id = index.toString()
                )
            } else {
                Student(
                    rank = "${index + 1}",
                    name = value.substringBefore("_").ifEmpty { "Student ${index + 1}" },
                    className = "Unknown",
                    score = value.substringAfterLast("_").ifEmpty { value },
                    id = index.toString()
                )
            }
        }
    }.sortedBy { it.rank.toIntOrNull() ?: 999 }
}