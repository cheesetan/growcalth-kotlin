package com.example.growcalth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.growcalth.ui.theme.Accent
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.growcalth.ui.theme.GrowCalthTheme

class AcknowledgementsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GrowCalthTheme {
                AcknowledgementsScreen(
                    onBackClick = { finish() }
                )
            }
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

            // Development Team Section (includes mascots)
            DevelopmentTeamSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Special Thanks Section
            SpecialThanksSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Packages & Libraries Section
            PackagesLibrariesSection()

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun AboutSection() {
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
}

@Composable
private fun DevelopmentTeamSection() {
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
                role = "Founder and CEO",
                classYear = "Class of 2024",
                iconColor = Color(0xFFF44336), // Red color
                icon = "🏆"
            )

            Spacer(modifier = Modifier.height(24.dp))

            TeamMember(
                name = "Chay Yu Hung Tristan",
                role = "Lead iOS Developer",
                classYear = "Class of 2024",
                iconColor = Color(0xFFE91E63), // Pink color
                icon = "🔨"
            )

            Spacer(modifier = Modifier.height(24.dp))

            TeamMember(
                name = "Felix Forbes Dimjati",
                role = "Social Entrepreneurship Lead",
                classYear = "Class of 2024",
                iconColor = Color(0xFF9C27B0), // Purple color
                icon = "👥"
            )

            Spacer(modifier = Modifier.height(24.dp))

            TeamMember(
                name = "Bellam Nandakumar Aravind",
                role = "Lead Android Developer",
                classYear = "Class of 2024",
                iconColor = Color(0xFFE91E63), // Pink color
                icon = "🔨"
            )

            Spacer(modifier = Modifier.height(24.dp))

            TeamMember(
                name = "Aathithya Jegatheesan",
                role = "Outreach and Communications Lead, Android Developer",
                classYear = "Class of 2024",
                iconColor = Color(0xFFF44336), // Red color
                icon = "💬"
            )

            Spacer(modifier = Modifier.height(24.dp))

            TeamMember(
                name = "Ayaan Jain",
                role = "Finance Lead",
                classYear = "Class of 2024",
                iconColor = Color(0xFFFFC107), // Yellow color
                icon = "$"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mascots Section within the same card
            Text(
                text = "Scoobert - GrowCalth's Mascot",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Loves to exercise. Will do a backflip if you tap him 5 times.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Washington - GrowCalth's Mascot",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tiny little pibble. Will demand you to wash his bellayyy if you tap him 5 times.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TeamMember(
    name: String,
    role: String,
    classYear: String,
    iconColor: Color,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 12.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "$name - $role",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
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

// Remove the separate MascotsSection function since it's now integrated into DevelopmentTeamSection

@Composable
private fun SpecialThanksSection() {
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
                        text = "⭐",
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "SPECIAL THANKS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            // Special Thanks Members
            TeamMember(
                name = "Ms Adele Lim",
                role = "",
                classYear = "Sports and Wellness Department",
                iconColor = Color(0xFFF44336), // Red color
                icon = "🏃"
            )

            Spacer(modifier = Modifier.height(24.dp))

            TeamMember(
                name = "Mr Ng Jun Wei",
                role = "",
                classYear = "Sports and Wellness Department",
                iconColor = Color(0xFFF44336), // Red color
                icon = "🏃"
            )

            Spacer(modifier = Modifier.height(24.dp))

            TeamMember(
                name = "Mr Wade Wang",
                role = "",
                classYear = "Sports and Wellness Department",
                iconColor = Color(0xFFF44336), // Red color
                icon = "🏃"
            )
        }
    }
}

@Composable
private fun PackagesLibrariesSection() {
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
                        text = "📦",
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "PACKAGES & LIBRARIES",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "This section would typically list the open-source libraries and packages used in the development of GrowCalth.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}