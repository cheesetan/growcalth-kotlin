package com.example.growcalth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.health.connect.client.HealthConnectClient
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

class LandingPageActivity : ComponentActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        // Setup permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle permission result
        }

        setContent {
            GrowCalthTheme {
                LandingPage(db = db, permissionLauncher = permissionLauncher)
            }
        }
    }
}

sealed class IconType {
    data class Vector(val imageVector: ImageVector) : IconType()
    data class Drawable(val painter: @Composable () -> Painter) : IconType()
}

enum class Destination(val label: String, val icon: IconType) {
    HOME("Home", IconType.Vector(Icons.Default.Home)),
    ANNOUNCEMENTS("Announcements", IconType.Drawable { painterResource(id = R.drawable.campaign_24px) }),
    CHALLENGES("Challenges", IconType.Drawable { painterResource(id = R.drawable.flag_24px) }),
    NAPFA("NAPFA", IconType.Drawable { painterResource(id = R.drawable.directions_run_24px) }),
    SETTINGS("Settings", IconType.Vector(Icons.Default.Settings)),
}

@Composable
fun LandingPage(
    db: FirebaseFirestore,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    modifier: Modifier = Modifier
) {
    val startDestination = Destination.HOME
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Light mode background
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(top = 40.dp)
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
                    color = Color(0xFF1A1A1A) // Dark text for light mode
                )

                if (selectedDestination == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5) // Light gray card
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "You are unable to earn GrowCalth points.",
                            color = Color(0xFF666666),
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
                    .padding(horizontal = 24.dp, vertical = 36.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(40.dp),
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(40.dp)
                        )
                        .padding(2.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            color = Color(0xFFF8F9FA),
                            shape = RoundedCornerShape(36.dp)
                        )
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
                .background(Color.White) // Light mode background
                .padding(contentPadding)
        ) {
            when (selectedDestination) {
                0 -> HomeTab(
                    onGoalClick = { showGoalDialog = true },
                    permissionLauncher = permissionLauncher
                )
                1 -> AnnouncementsTab()
                2 -> ChallengesScreen()
                3 -> NapfaTab(db = db)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clickable { onClick() }
            .width(40.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .padding(vertical = 5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63).copy(alpha = 0.15f)) // Light pink background
                )
            }

            when (destination.icon) {
                is IconType.Vector -> {
                    Icon(
                        imageVector = destination.icon.imageVector,
                        contentDescription = destination.label,
                        modifier = Modifier.size(60.dp).padding(vertical = 5.dp),
                        tint = if (isSelected) Color(0xFFE91E63) else Color(0xFF666666)
                    )
                }
                is IconType.Drawable -> {
                    Icon(
                        painter = destination.icon.painter(),
                        contentDescription = destination.label,
                        modifier = Modifier.size(60.dp).padding(vertical = 5.dp),
                        tint = if (isSelected) Color(0xFFE91E63) else Color(0xFF666666)
                    )
                }
            }
        }

        Text(
            text = destination.label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFFE91E63) else Color(0xFF666666),
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset(y = (-16).dp)
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = Color(0xFF1A1A1A)
                )

                GoalItem(
                    label = "Steps",
                    value = stepsGoal,
                    onValueChange = { stepsGoal = it },
                    onDecrease = { if (stepsGoal > 100) stepsGoal -= 100 },
                    onIncrease = { stepsGoal += 100 }
                )

                GoalItem(
                    label = "Distance",
                    value = distanceGoal,
                    onValueChange = { distanceGoal = it },
                    onDecrease = { if (distanceGoal > 100) distanceGoal -= 100 },
                    onIncrease = { distanceGoal += 100 }
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Goals",
                        color = Color.White,
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
        FilledTonalButton(
            onClick = { onDecrease() },
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Text(
                text = "−",
                color = Color(0xFF666666),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A)
        )

        Text(
            text = value.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.weight(1f))

        FilledTonalButton(
            onClick = { onIncrease() },
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFFE91E63)
            )
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeTab(
    onGoalClick: () -> Unit = {},
    permissionLauncher: ActivityResultLauncher<Array<String>>
) {
    val context = LocalContext.current
    val healthViewModel: HealthDataViewModel = viewModel(factory = HealthDataViewModel.Factory(context))

    // Observe health data
    val steps by healthViewModel.steps.collectAsState()
    val distance by healthViewModel.distance.collectAsState()
    val hasPermissions by healthViewModel.hasPermissions.collectAsState()
    val isLoading by healthViewModel.isLoading.collectAsState()

    var topHousePoints by remember { mutableStateOf<List<HousePoints>>(emptyList()) }
    var isLoadingLeaderboard by remember { mutableStateOf(true) }

    // Handle permission request
    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            val permissionsToRequest = healthViewModel.getPermissionStrings()
            if (permissionsToRequest.isNotEmpty()) {
                permissionLauncher.launch(permissionsToRequest)
            }
        }
    }

    // Load Firebase leaderboard data
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
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Health metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HealthMetricCard(
                value = if (hasPermissions) "${NumberFormat.getNumberInstance().format(steps)}" else "0",
                unit = "steps",
                remaining = "0 steps left", // You can calculate this based on your goals
                progress = if (hasPermissions) (steps / 10000f).coerceAtMost(1f) else 0f,
                modifier = Modifier.weight(1f),
                isLoading = isLoading,
                hasPermissions = hasPermissions,
                onRetry = { healthViewModel.loadHealthData() }
            )

            HealthMetricCard(
                value = if (hasPermissions) String.format("%.2f", distance / 1000) else "0.00",
                unit = "km",
                remaining = "0.00 km left",
                progress = if (hasPermissions) {
                    distance?.let { d -> (d.toFloat() / 1000f / 5f).coerceAtMost(1f) } ?: 0f
                } else 0f,                modifier = Modifier.weight(1f),
                isLoading = isLoading,
                hasPermissions = hasPermissions,
                onRetry = { healthViewModel.loadHealthData() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Leaderboard Section
        Text(
            text = "Leaderboard",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
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
                    color = Color(0xFFE91E63),
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
                color = Color(0xFFE91E63),
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
                containerColor = Color(0xFFE91E63)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "What's your next goal?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
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
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    hasPermissions: Boolean = true,
    onRetry: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFFE91E63),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                } else if (!hasPermissions) {
                    // Show refresh icon when no permissions
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    // Health data visualization
                    val backgroundColor = Color(0xFFE0E0E0)
                    val progressColor = Color(0xFFE91E63)

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        val strokeWidth = 8.dp.toPx()
                        val canvasSize = minOf(size.width, size.height)
                        val radius = (canvasSize - strokeWidth) / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val arcSize = Size(canvasSize - strokeWidth, canvasSize - strokeWidth)
                        val topLeft = Offset(center.x - radius, center.y - radius)

                        // Background arc
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
                }

                if (!isLoading && hasPermissions) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = value,
                            color = Color(0xFF1A1A1A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = unit,
                            color = Color(0xFF666666),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (!hasPermissions) "Tap to connect" else remaining,
                color = Color(0xFF666666),
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
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position,
                color = Color(0xFF666666),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Points pill
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = color.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = points,
                color = Color.White,
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
        else -> Color(0xFF718096)
    }
}

private fun formatExactPoints(points: Long): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(points)
}