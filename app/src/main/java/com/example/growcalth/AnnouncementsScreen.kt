package com.example.growcalth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnnouncementsTab(
    onAnnouncementClick: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Segmented Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Announcements Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == 0) Color.White else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Announcements",
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = Color.Black
                        )
                    }
                    
                    // Events Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == 1) Color.White else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Events",
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = Color.Black
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> AnnouncementsContent(onAnnouncementClick = onAnnouncementClick)
                1 -> EventsContent()
            }
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { /* TODO: Handle new announcement */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp),
            containerColor = Color(0xFFE53E3E),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "New Announcement",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AnnouncementsContent(
    onAnnouncementClick: (String, String, String) -> Unit = { _, _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        AnnouncementCard(
            title = "GrowCalth Feedback - Season 2",
            snippet = "Hey GrowCalth users! Thank you for an ama...",
            timestamp = "29d ago",
            content = "Hey GrowCalth users! Thank you for an amazing Season 2! We've received incredible feedback from our community and we're excited to share some updates with you.\n\nYour engagement and dedication to health and wellness has been inspiring. We've seen remarkable progress across our platform, with users achieving their fitness goals and building healthier habits.\n\nAs we prepare for Season 3, we want to let you know about some exciting new features coming your way:\n\n• Enhanced workout tracking\n• Improved nutrition guidance\n• New community challenges\n• Personalized recommendations\n\nStay tuned for more updates and keep up the great work!",
            onCardClick = { onAnnouncementClick("GrowCalth Feedback - Season 2", "Hey GrowCalth users! Thank you for an amazing Season 2! We've received incredible feedback from our community and we're excited to share some updates with you.\n\nYour engagement and dedication to health and wellness has been inspiring. We've seen remarkable progress across our platform, with users achieving their fitness goals and building healthier habits.\n\nAs we prepare for Season 3, we want to let you know about some exciting new features coming your way:\n\n• Enhanced workout tracking\n• Improved nutrition guidance\n• New community challenges\n• Personalized recommendations\n\nStay tuned for more updates and keep up the great work!", "29d ago") }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        AnnouncementCard(
            title = "Limited-Time Double Points Event - ROU...",
            snippet = "Are you ready for Round 2 of the Grow...",
            timestamp = "84d ago",
            content = "Are you ready for Round 2 of the GrowCalth Double Points Event? This limited-time opportunity is back by popular demand!\n\nFrom now until the end of the month, earn DOUBLE points on all your activities:\n\n• Workout sessions\n• Nutrition logging\n• Step count achievements\n• Community challenges\n• Wellness check-ins\n\nThis is your chance to accelerate your progress and climb the leaderboards faster than ever. Don't miss out on this exclusive opportunity to maximize your rewards.\n\nRemember, the more active you are, the more points you'll earn. Let's make this the most productive month yet!",
            onCardClick = { onAnnouncementClick("Limited-Time Double Points Event - ROUND 2", "Are you ready for Round 2 of the GrowCalth Double Points Event? This limited-time opportunity is back by popular demand!\n\nFrom now until the end of the month, earn DOUBLE points on all your activities:\n\n• Workout sessions\n• Nutrition logging\n• Step count achievements\n• Community challenges\n• Wellness check-ins\n\nThis is your chance to accelerate your progress and climb the leaderboards faster than ever. Don't miss out on this exclusive opportunity to maximize your rewards.\n\nRemember, the more active you are, the more points you'll earn. Let's make this the most productive month yet!", "84d ago") }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        AnnouncementCard(
            title = "Clarification on GrowCalth Points...",
            snippet = "Earlier today, the GrowCalth team ide...",
            timestamp = "120d ago",
            content = "Earlier today, the GrowCalth team identified and resolved a technical issue that was affecting point calculations for some users. We want to clarify how our points system works to ensure transparency.\n\nHow Points Are Calculated:\n\n• Base points for completing activities\n• Bonus points for consistency streaks\n• Multiplier points for achieving goals\n• Community points for participating in challenges\n\nIf you noticed any discrepancies in your point balance, rest assured that all calculations have been corrected and your points are now accurate.\n\nWe apologize for any confusion this may have caused and appreciate your patience as we resolved this issue. Your trust in GrowCalth is important to us, and we're committed to maintaining the highest standards of accuracy and transparency.",
            onCardClick = { onAnnouncementClick("Clarification on GrowCalth Points System", "Earlier today, the GrowCalth team identified and resolved a technical issue that was affecting point calculations for some users. We want to clarify how our points system works to ensure transparency.\n\nHow Points Are Calculated:\n\n• Base points for completing activities\n• Bonus points for consistency streaks\n• Multiplier points for achieving goals\n• Community points for participating in challenges\n\nIf you noticed any discrepancies in your point balance, rest assured that all calculations have been corrected and your points are now accurate.\n\nWe apologize for any confusion this may have caused and appreciate your patience as we resolved this issue. Your trust in GrowCalth is important to us, and we're committed to maintaining the highest standards of accuracy and transparency.", "120d ago") }
        )
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for FAB
    }
}

@Composable
fun EventsContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Events",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFE53E3E)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Events",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No events available",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnnouncementCard(
    title: String,
    snippet: String,
    timestamp: String,
    content: String = "",
    onCardClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = snippet,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = timestamp,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Play arrow icon
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
