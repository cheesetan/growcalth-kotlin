package com.example.growcalth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
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
            color = Color(0xFF9CA3AF),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    .background(Color(0xFFE53E3E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "C",
                    color = Color.White,
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
                    color = Color.Black
                )
                Text(
                    text = "21.ssts.edu.sg",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "Tap to view account information",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = Color(0xFFD1D5DB)
            )
        }
    }
}

@Composable
fun AppearanceSelector() {
    var selectedAppearance by remember { mutableStateOf(1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                color = Color(0xFF9CA3AF),
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
                if (selectedAppearance == index) Color(0xFFE5E7EB) else Color.Transparent,
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
            color = Color.Black
        )
    }
}

@Composable
fun SpecularHighlightsCard() {
    var motionSpecularEnabled by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = Color.Black
                )
                Text(
                    text = "Specular Highlights",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Motion-based specular highlights shifts the angle of reflection of light based on device rotation. Enabling this feature might impact performance.",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 20.sp
                )
            }

            Switch(
                checked = motionSpecularEnabled,
                onCheckedChange = { motionSpecularEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF34D399),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD1D5DB)
                )
            )
        }
    }
}

@Composable
fun PermissionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                color = Color(0xFFE53E3E)
            )
            Text(
                text = "Notification Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE53E3E)
            )
        }
    }
}