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
import com.example.growcalth.ui.theme.GrowCalthTheme
import kotlin.math.cos
import kotlin.math.sin

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
    ANNOUNCEMENTS(" Updates", Icons.Default.Notifications),
    CHALLENGES("CHALLENGES", Icons.Default.Star),
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
                    .background(Color.White)
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
                    color = Color.Black
                )

                if (selectedDestination == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF374151)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "You are unable to earn GrowCalth points.",
                            color = Color.White,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (selectedDestination == index) Color(0xFFE53E3E) else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                maxLines = 1,
                                color = if (selectedDestination == index) Color(0xFFE53E3E) else Color.Black,
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
                    color = Color.Black
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E)),
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gray circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Gray, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Empty circle, just for visual
            }

            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Text(
                text = value.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Decrease button
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Text(
                    text = "−",
                    color = Color.Gray,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Increase button
            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color(0xFFE53E3E),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HomeTab(onGoalClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            LeaderboardItem("1ST", "7,293 POINTS", Color(0xFFFFD700))
            LeaderboardItem("2ND", "6,780 POINTS", Color(0xFF22C55E))
            LeaderboardItem("3RD", "6,739 POINTS", Color(0xFF3B82F6))
        }

        Spacer(modifier = Modifier.height(12.dp))

        val context = LocalContext.current
        Text(
            text = "view more >",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp)
                .clickable {
                    context.startActivity(Intent(context, LeaderboardActivity::class.java))
                }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGoalClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "What's your next goal?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF374151)),
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
                // Background circle
                Canvas(
                    modifier = Modifier.size(110.dp)
                ) {
                    val strokeWidth = 8.dp.toPx()
                    val radius = (size.width - strokeWidth) / 2

                    // Background arc
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                    )

                    // Progress arc
                    drawArc(
                        color = Color(0xFFE53E3E),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                    )
                }

                // Text content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = unit,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = remaining,
                color = Color.White,
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
                    color = Color(0xFF374151),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Points pill
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = color,
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