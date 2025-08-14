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
import com.example.growcalth.NapfaScreen
import com.example.growcalth.ChallengesScreen
import com.example.growcalth.AnnouncementsTab
import com.example.growcalth.SettingsTab

class LandingPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrowCalthTheme {
                LandingPage()
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
fun LandingPage(modifier: Modifier = Modifier) {
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (selectedDestination == index) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                maxLines = 1,
                                color = if (selectedDestination == index) Accent else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (selectedDestination == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = selectedDestination == index,
                        onClick = {
                            selectedDestination = index
                        }
                    )
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
                3 -> NapfaTab()
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
fun HomeTab(onGoalClick: () -> Unit = {}) {
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
                value = "11,307",
                unit = "steps",
                remaining = "0 steps left",
                progress = 1f,
                modifier = Modifier.weight(1f)
            )

            HealthMetricCard(
                value = "7.96",
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

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            LeaderboardItem("1ST", "7,293 POINTS", Gold)
            LeaderboardItem("2ND", "6,780 POINTS", Success)
            LeaderboardItem("3RD", "6,739 POINTS", Info)
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
fun NapfaTab() {
    NapfaScreen()
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