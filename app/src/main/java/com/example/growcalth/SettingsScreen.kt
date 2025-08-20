package com.example.growcalth

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.growcalth.ui.theme.Success
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsTab() {
    val context = LocalContext.current

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

        SettingsSection(title = "PERMISSIONS") { PermissionsCard() }
        Spacer(modifier = Modifier.height(32.dp))

        SettingsSection(title = "RESOURCES") {
            CalculatorsCard()
            Spacer(modifier = Modifier.height(16.dp))
            AcknowledgementsCard(
                onAcknowledgementsClick = {
                    val intent = Intent(context, AcknowledgementsActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Contact and Sign out options (no section header)
        ContactAndSignOutCard()
        Spacer(modifier = Modifier.height(100.dp))
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
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "No email"

    // Extract first letter for avatar, default to "U" if no email
    val firstLetter = if (userEmail != "No email" && userEmail.isNotEmpty()) {
        userEmail.first().uppercaseChar().toString()
    } else {
        "U"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Navigate to AccountActivity
                    val intent = Intent(context, AccountActivity::class.java)
                    context.startActivity(intent)
                }
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
                    text = firstLetter,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userEmail,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
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
                if (selectedAppearance == index) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
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
fun PermissionsCard() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Open the app's system settings page
                    try {
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.parse("package:${context.packageName}")
                            addCategory(Intent.CATEGORY_DEFAULT)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback: open general settings if app settings can't be opened
                        try {
                            val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(fallbackIntent)
                        } catch (ex: Exception) {
                            // Final fallback - do nothing if even general settings can't be opened
                        }
                    }
                }
                .padding(16.dp)
        ) {
            Text(
                text = "Open GrowCalth",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Accent
            )
            Text(
                text = "App Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Accent
            )
        }
    }
}

@Composable
fun CalculatorsCard() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(context, CalculatorActivity::class.java)
                    context.startActivity(intent)
                }
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
                    .clickable {
                        val emailIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("growcalth.main@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "Contact from GrowCalth App")
                            putExtra(Intent.EXTRA_TEXT, "Hello GrowCalth Team,\n\n")
                        }
                        try {
                            context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                        } catch (ex: android.content.ActivityNotFoundException) {
                            // Handle case where no email app is available
                        }
                    }
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
                        // Firebase sign out
                        FirebaseAuth.getInstance().signOut()

                        // Navigate back to MainActivity
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                    .padding(16.dp)
            )
        }
    }
}