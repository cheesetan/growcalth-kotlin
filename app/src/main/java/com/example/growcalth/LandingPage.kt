package com.example.growcalth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import com.example.growcalth.ui.theme.GrowCalthTheme
import com.example.growcalth.ui.theme.Accent
import com.example.growcalth.ui.theme.Surface
import com.example.growcalth.ui.theme.OnSurface
import com.example.growcalth.ui.theme.SurfaceVariant
import com.example.growcalth.ui.theme.Gold
import com.example.growcalth.ui.theme.Success
import com.example.growcalth.ui.theme.Info
import kotlin.math.cos
import kotlin.math.sin
import com.example.growcalth.LeaderboardActivity
import com.example.growcalth.ChallengesScreen
import com.example.growcalth.AnnouncementsTab
import com.example.growcalth.SettingsTab
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

class LandingPageActivity : ComponentActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        setContent {
            GrowCalthTheme {
                LandingPage(db = db)
            }
        }
    }
}

enum class Destination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    ANNOUNCEMENTS("Updates", Icons.Default.Notifications),
    CHALLENGES("Challenges", Icons.Default.Star),
    NAPFA("NAPFA", Icons.Default.Person),
    SETTINGS("Settings", Icons.Default.Settings),
}

@Composable
fun LandingPage(db: FirebaseFirestore, modifier: Modifier = Modifier) {
    val startDestination = Destination.HOME
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(top = 40.dp) // Add more space from top
            ) {
                Text(
                    text = when (selectedDestination) {
                        0 -> "Home"
                        1 -> "Updates"
                        2 -> "Challenges"
                        3 -> "NAPFA"
                        4 -> "Settings"
                        else -> "Home"
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedDestination == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "You are unable to earn GrowCalth points.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Responsive navigation bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp) // Fixed height - shorter than before
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            color = Color.White, // White border color
                            shape = RoundedCornerShape(40.dp)
                        )
                        .padding(4.dp) // 4 pixel white border
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            color = Color(0xFFF0F0F5), // Much lighter background
                            shape = RoundedCornerShape(36.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 10.dp) // Balanced padding
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Destination.entries.forEachIndexed { index, destination ->
                            NavigationItem(
                                destination = destination,
                                isSelected = selectedDestination == index,
                                onClick = { selectedDestination = index }
                            )
                        }
                    }
                }
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when (selectedDestination) {
                0 -> HomeTab(onGoalClick = { showGoalDialog = true })
                1 -> AnnouncementsTab()
                2 -> ChallengesScreen()
                3 -> NapfaTab(db = db) // Pass db parameter here
                4 -> SettingsTab()
            }
        }
    }

    // Goal Dialog
    if (showGoalDialog) {
        GoalDialog(onDismiss = { showGoalDialog = false })
    }
}

@Composable
fun NavigationItem(
    destination: Destination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp), // Consistent spacing
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxHeight() // Use full available height
            .width(60.dp) // Fixed width for consistency
            .padding(horizontal = 2.dp, vertical = 4.dp) // Responsive padding
    ) {
        // Icon with circular radial background for active tab
        Box(
            modifier = Modifier
                .size(32.dp) // Fixed responsive size
                .aspectRatio(1f), // Keep it circular
            contentAlignment = Alignment.Center
        ) {
            // Circular radial background for active tab
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp) // 90% of container size
                        .clip(CircleShape)
                        .background(Color(0xFFFFCDD2))
                )
            }

            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                modifier = Modifier.size(18.dp), // 60% of container size
                tint = if (isSelected) Color(0xFFE91E63) else Color(0xFF424242)
            )
        }

        // Label - responsive text
        Text(
            text = destination.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFFE91E63) else Color(0xFF000000),
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }
}

@Composable
fun GoalDialog(onDismiss: () -> Unit) {
    var stepsGoal by remember { mutableStateOf(2700) }
    var distanceGoal by remember { mutableStateOf(2500) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Edit Goal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Steps Goal
                GoalItem(
                    label = "Steps",
                    value = stepsGoal,
                    onValueChange = { stepsGoal = it },
                    onDecrease = { if (stepsGoal > 100) stepsGoal -= 100 },
                    onIncrease = { stepsGoal += 100 }
                )

                // Distance Goal
                GoalItem(
                    label = "Distance",
                    value = distanceGoal,
                    onValueChange = { distanceGoal = it },
                    onDecrease = { if (distanceGoal > 100) distanceGoal -= 100 },
                    onIncrease = { distanceGoal += 100 }
                )

                // Save Button (non-functional)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Goals",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GoalItem(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Decrease button (far left)
        FilledTonalButton(
            onClick = { onDecrease() },
            modifier = Modifier.size(40.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "−",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Label
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Value
        Text(
            text = value.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        // Increase button (far right)
        FilledTonalButton(
            onClick = { onIncrease() },
            modifier = Modifier.size(40.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Accent)
        ) {
            Text(
                text = "+",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeTab(onGoalClick: () -> Unit = {}, viewModel: HealthDataView = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var topHousePoints by remember { mutableStateOf<List<HousePoints>>(emptyList()) }
    var isLoadingLeaderboard by remember { mutableStateOf(true) }

    val steps by viewModel.steps.collectAsState(inital = 0L)
    val distance by viewModel.distance.collectAsState(initial = 0.0)
    // Load top 3 house points from Firebase
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("schools").document("sst").collection("leaderboard")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .await()

            val fetchedHousePoints = result.documents.mapNotNull { document ->
                try {
                    HousePoints(
                        id = document.id,
                        color = document.getString("name") ?: "",
                        points = document.getLong("points") ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }

            topHousePoints = fetchedHousePoints
            isLoadingLeaderboard = false

        } catch (e: Exception) {
            isLoadingLeaderboard = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HealthMetricCard(
                value = "11,307 Real: $steps",
                unit = "steps",
                remaining = "0 steps left",
                progress = 1f,
                modifier = Modifier.weight(1f)
            )

            HealthMetricCard(
                value = "7.96 Real:  ${String.format("%.2f", distance / 1000)}",
                unit = "km",
                remaining = "0.00 km left",
                progress = 0.8f,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Leaderboard Section
        Text(
            text = "Leaderboard",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoadingLeaderboard) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                topHousePoints.forEachIndexed { index, house ->
                    val position = when (index) {
                        0 -> "1ST"
                        1 -> "2ND"
                        2 -> "3RD"
                        else -> "${index + 1}TH"
                    }
                    LeaderboardItem(
                        position = position,
                        points = "${formatExactPoints(house.points)} POINTS",
                        color = getHouseColor(house.color)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        TextButton(
            onClick = {
                context.startActivity(Intent(context, LeaderboardActivity::class.java))
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "View more >",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Goal Button
        Button(
            onClick = onGoalClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "What's your next goal?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun NapfaTab(db: FirebaseFirestore) {
    NapfaScreen(db)
}

@Composable
fun HealthMetricCard(
    value: String,
    unit: String,
    remaining: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Get colors outside of Canvas scope
                val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                val progressColor = Accent

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp) // Add equal padding on all sides
                ) {
                    val strokeWidth = 8.dp.toPx()

                    // Now the canvas has equal padding, so we can use the full size
                    val canvasSize = minOf(size.width, size.height)
                    val radius = (canvasSize - strokeWidth) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Calculate the bounding rectangle for the arc
                    val arcSize = Size(canvasSize - strokeWidth, canvasSize - strokeWidth)
                    val topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    )

                    // Background arc (full circle)
                    drawArc(
                        color = backgroundColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = arcSize,
                        topLeft = topLeft
                    )

                    // Progress arc
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = arcSize,
                        topLeft = topLeft
                    )
                }

                // Text content - this will be centered by the Box's contentAlignment
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = unit,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = remaining,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LeaderboardItem(
    position: String,
    points: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Position pill
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Points pill
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = color.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = points,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
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

// Function to format exact points with comma separators
private fun formatExactPoints(points: Long): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(points)
}