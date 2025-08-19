package com.example.growcalth

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.growcalth.ui.theme.GrowCalthTheme
import com.example.growcalth.ui.theme.ThemeMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

// Data class for House Points
data class HousePoints(
    val id: String = "",
    val color: String = "",
    val points: Long = 0L
)

class LeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrowCalthTheme(themeMode = ThemeMode.AUTO) {
                LeaderboardScreen(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun LeaderboardScreen(onBackClick: () -> Unit) {
    var housePoints by remember { mutableStateOf<List<HousePoints>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load house points from Firebase
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("HousePoints")
                .orderBy("points", Query.Direction.DESCENDING)
                .get()
                .await()

            val fetchedHousePoints = result.documents.mapNotNull { document ->
                try {
                    HousePoints(
                        id = document.id,
                        color = document.getString("color") ?: "",
                        points = document.getLong("points") ?: 0L
                    )
                } catch (e: Exception) {
                    Log.e("LeaderboardScreen", "Error parsing house points: ${document.id}", e)
                    null
                }
            }

            housePoints = fetchedHousePoints
            isLoading = false

        } catch (e: Exception) {
            Log.e("LeaderboardScreen", "Error fetching house points", e)
            errorMessage = "Failed to load leaderboard: ${e.message}"
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Leaderboard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading leaderboard...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error occurred",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            housePoints.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 64.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No house points data available",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LeaderboardContent(housePoints = housePoints)
            }
        }
    }
}

@Composable
fun LeaderboardContent(housePoints: List<HousePoints>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Trophy icon
        Box(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .size(80.dp)
                .background(getHouseColor(housePoints.firstOrNull()?.color ?: ""), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏆",
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
        }

        // Podium (top 3)
        if (housePoints.size >= 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // 3rd place
                PodiumColumn(
                    position = 3,
                    height = 120.dp,
                    color = getHouseColor(housePoints[2].color),
                    points = formatExactPoints(housePoints[2].points),
                    emoji = getHouseEmoji(housePoints[2].color)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // 1st place
                PodiumColumn(
                    position = 1,
                    height = 180.dp,
                    color = getHouseColor(housePoints[0].color),
                    points = formatExactPoints(housePoints[0].points),
                    emoji = getHouseEmoji(housePoints[0].color)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // 2nd place
                PodiumColumn(
                    position = 2,
                    height = 150.dp,
                    color = getHouseColor(housePoints[1].color),
                    points = formatExactPoints(housePoints[1].points),
                    emoji = getHouseEmoji(housePoints[1].color)
                )
            }
        }

        // Remaining ranks (4th place onwards)
        if (housePoints.size > 3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                housePoints.drop(3).forEachIndexed { index, house ->
                    val position = index + 4
                    LeaderboardRankItem(
                        position = "${getOrdinal(position)}",
                        points = "${formatExactPoints(house.points)} POINTS",
                        emoji = getHouseEmoji(house.color),
                        backgroundColor = getHouseColor(house.color)
                    )
                }
            }
        } else if (housePoints.size == 2) {
            // Handle case with only 2 houses
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // 1st place
                PodiumColumn(
                    position = 1,
                    height = 180.dp,
                    color = getHouseColor(housePoints[0].color),
                    points = formatExactPoints(housePoints[0].points),
                    emoji = getHouseEmoji(housePoints[0].color)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // 2nd place
                PodiumColumn(
                    position = 2,
                    height = 150.dp,
                    color = getHouseColor(housePoints[1].color),
                    points = formatExactPoints(housePoints[1].points),
                    emoji = getHouseEmoji(housePoints[1].color)
                )
            }
        } else if (housePoints.size == 1) {
            // Handle case with only 1 house
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                PodiumColumn(
                    position = 1,
                    height = 180.dp,
                    color = getHouseColor(housePoints[0].color),
                    points = formatExactPoints(housePoints[0].points),
                    emoji = getHouseEmoji(housePoints[0].color)
                )
            }
        }
    }
}

@Composable
fun PodiumColumn(
    position: Int,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    points: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        // Podium column with content
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(height)
                .clip(RoundedCornerShape(12.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Circle with emoji
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Points text
                Text(
                    text = points,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun LeaderboardRankItem(
    position: String,
    points: String,
    emoji: String,
    backgroundColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Position pill
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Points pill with emoji
        Box(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = points,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper functions
private fun getHouseColor(color: String): Color {
    return when (color.lowercase()) {
        "red" -> Color(0xFFE53E3E)
        "blue" -> Color(0xFF3182CE)
        "green" -> Color(0xFF38A169)
        "yellow" -> Color(0xFFD69E2E)
        "black" -> Color(0xFF2D3748)
        else -> Color(0xFF718096) // Default gray
    }
}

private fun getHouseEmoji(color: String): String {
    return when (color.lowercase()) {
        "red" -> "🔥"
        "blue" -> "🌊"
        "green" -> "🌿"
        "yellow" -> "⚡"
        "black" -> "🖤"
        else -> "🏠"
    }
}

// New function to format exact points with comma separators
private fun formatExactPoints(points: Long): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(points)
}

private fun getOrdinal(number: Int): String {
    return when {
        number % 100 in 11..13 -> "${number}TH"
        number % 10 == 1 -> "${number}ST"
        number % 10 == 2 -> "${number}ND"
        number % 10 == 3 -> "${number}RD"
        else -> "${number}TH"
    }
}