package com.example.growcalth

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.growcalth.ui.theme.Accent
import com.example.growcalth.ui.theme.Surface
import com.example.growcalth.ui.theme.OnSurface
import com.example.growcalth.ui.theme.SurfaceVariant
import com.example.growcalth.ui.theme.OnSurfaceVariant
import com.example.growcalth.ui.theme.Success

@Composable
fun SettingsTab() {
    var showAcknowledgements by remember { mutableStateOf(false) }

    if (showAcknowledgements) {
        AcknowledgementsScreen(
            onBackClick = { showAcknowledgements = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            SettingsSection(title = "ACCOUNT") { AccountCard() }
            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "APPEARANCE") { AppearanceSelector() }
            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "SPECULAR HIGHLIGHTS") { SpecularHighlightsCard() }
            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "PERMISSIONS") { PermissionsCard() }
            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "RESOURCES") {
                CalculatorsCard()
                Spacer(modifier = Modifier.height(16.dp))
                AcknowledgementsCard(onAcknowledgementsClick = { showAcknowledgements = true })
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Contact and Sign out options (no section header)
            ContactAndSignOutCard()
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun AccountCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "C",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "chay_yu_hung@s20",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "21.ssts.edu.sg",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to view account information",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppearanceSelector() {
    var selectedAppearance by remember { mutableStateOf(1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AppearanceOption("Light", 0, selectedAppearance) { selectedAppearance = it }
                AppearanceOption("Automatic", 1, selectedAppearance) { selectedAppearance = it }
                AppearanceOption("Dark", 2, selectedAppearance) { selectedAppearance = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Automatic sets GrowCalth's appearance based on your device's appearance.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun AppearanceOption(
    label: String,
    index: Int,
    selectedAppearance: Int,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selectedAppearance == index) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick(index) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selectedAppearance == index) FontWeight.Medium else FontWeight.Normal,
            color = if (selectedAppearance == index) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SpecularHighlightsCard() {
    var motionSpecularEnabled by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Motion-based",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Specular Highlights",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Motion-based specular highlights shifts the angle of reflection of light based on device rotation. Enabling this feature might impact performance.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            Switch(
                checked = motionSpecularEnabled,
                onCheckedChange = { motionSpecularEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = Success,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
fun PermissionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp)
        ) {
            Text(
                text = "Open GrowCalth",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Accent
            )
            Text(
                text = "Notification Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Accent
            )
        }
    }
}

@Composable
fun CalculatorsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calculators",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AcknowledgementsCard(
    onAcknowledgementsClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAcknowledgementsClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Acknowledgements",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ContactAndSignOutCard() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Contact the Team
            Text(
                text = "Contact the Team",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(16.dp)
            )

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Sign out
            Text(
                text = "Sign out",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Navigate back to MainActivity
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        // If this is called from an Activity, you might want to finish it
                        // (context as? Activity)?.finish()
                    }
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcknowledgementsScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Acknowledgements",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Accent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // About GrowCalth Section
            AboutSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Development Team Section
            DevelopmentTeamSection()

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun AboutSection() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "ABOUT GROWCALTH",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = "GrowCalth is a one stop platform that allows SST Students to participate in house challenges and further fosters house spirit among their house members. Through the app, students are able to be notified of house announcements and events, which encourages house participation and involvement.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun DevelopmentTeamSection() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👥",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "DEVELOPMENT TEAM",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        // Team Members
        TeamMember(
            name = "Han Jeong Seu, Caleb",
            role = "CEO of GrowCalth / Lead Android Developer at GrowCalth",
            classYear = "Class of 2024",
            iconColor = Accent
        )

        Spacer(modifier = Modifier.height(24.dp))

        TeamMember(
            name = "Chay Yu Hung Tristan",
            role = "Lead iOS Developer at GrowCalth",
            classYear = "Class of 2024",
            iconColor = Color(0xFFE91E63) // Pink color
        )

        Spacer(modifier = Modifier.height(24.dp))

        TeamMember(
            name = "Felix Forbes Dimjati",
            role = "Social Entrepreneurship Lead at GrowCalth",
            classYear = "",
            iconColor = Color(0xFF9C27B0) // Purple color
        )
    }
}

@Composable
private fun TeamMember(
    name: String,
    role: String,
    classYear: String,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(iconColor)
                .padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = role,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (classYear.isNotEmpty()) {
                Text(
                    text = classYear,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}