package com.example.growcalth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Data classes remain the same
data class Announcement(
    val id: String = "",
    val header: String = "",
    val text: String = "",
    val name: String = "",
    val dateAdded: Date? = null
)

data class HouseEvent(
    val id: String = "",
    val header: String = "",
    val desc: String = "",
    val name: String = "",
    val date: String = "",
    val venue: String = "",
    val dateAdded: Date? = null
)

@Composable
fun AnnouncementsTab(
    onAnnouncementClick: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedAnnouncement by remember { mutableStateOf<Announcement?>(null) }
    var selectedEvent by remember { mutableStateOf<HouseEvent?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBEBF2)) // Light mode background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Modern Capsule Segmented Control
            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp), // Space between tabs
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
            ) {
                // Announcements Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (selectedTab == 0) {
                                Modifier
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(25.dp),
                                        ambientColor = Color.Black.copy(alpha = 0.1f),
                                        spotColor = Color.Black.copy(alpha = 0.15f)
                                    )
                                    .background(
                                        Color.White,
                                        RoundedCornerShape(25.dp)
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = Color.Black.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(25.dp)
                                    )
                            } else {
                                Modifier
                                    .background(
                                        Color(0xFFF8F8F8),
                                        RoundedCornerShape(25.dp)
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = Color.Black.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(25.dp)
                                    )
                            }
                        )
                        .clickable { selectedTab = 0 }
                        .height(36.dp)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Announcements",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, // Always bold
                        color = Color(0xFF2D2D2E)
                    )
                }

                // Events Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (selectedTab == 1) {
                                Modifier
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(25.dp),
                                        ambientColor = Color.Black.copy(alpha = 0.1f),
                                        spotColor = Color.Black.copy(alpha = 0.15f)
                                    )
                                    .background(
                                        Color.White,
                                        RoundedCornerShape(25.dp)
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = Color.Black.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(25.dp)
                                    )
                            } else {
                                Modifier
                                    .background(
                                        Color(0xFFF8F8F8),
                                        RoundedCornerShape(25.dp)
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = Color.Black.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(25.dp)
                                    )
                            }
                        )
                        .clickable { selectedTab = 1 }
                        .height(36.dp)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Events",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, // Always bold
                        color = Color(0xFF2D2D2E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    if (selectedAnnouncement != null) {
                        AnnouncementDetailView(
                            announcement = selectedAnnouncement!!,
                            onBack = { selectedAnnouncement = null }
                        )
                    } else {
                        AnnouncementsContent(
                            onAnnouncementClick = { title, content, timestamp ->
                                selectedAnnouncement = Announcement(
                                    header = title,
                                    text = content,
                                    dateAdded = null
                                )
                                onAnnouncementClick(title, content, timestamp)
                            }
                        )
                    }
                }
                1 -> {
                    if (selectedEvent != null) {
                        EventDetailView(
                            event = selectedEvent!!,
                            onBack = { selectedEvent = null }
                        )
                    } else {
                        EventsContent(
                            onEventClick = { header, desc, date, venue ->
                                selectedEvent = HouseEvent(
                                    header = header,
                                    desc = desc,
                                    date = date,
                                    venue = venue
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementsContent(
    onAnnouncementClick: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load announcements from Firebase
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("schools").document("sst").collection("announcements")
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .await()

            val fetchedAnnouncements = result.documents.mapNotNull { document ->
                try {
                    Announcement(
                        id = document.id,
                        header = document.getString("title") ?: "",
                        text = document.getString("description") ?: "",
                        dateAdded = document.getTimestamp("dateAdded")?.toDate()
                    )
                } catch (e: Exception) {
                    Log.e("AnnouncementsContent", "Error parsing announcement: ${document.id}", e)
                    null
                }
            }

            announcements = fetchedAnnouncements
            isLoading = false

        } catch (e: Exception) {
            Log.e("AnnouncementsContent", "Error fetching announcements", e)
            errorMessage = "Failed to load announcements: ${e.message}"
            isLoading = false
        }
    }

    // UI Content
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        when {
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE91E63)
                    )
                }
            }

            errorMessage != null -> {
                // Error state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error occurred",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            announcements.isEmpty() -> {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "No announcements",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBBBBBB)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No announcements yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Check back later for updates",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                // Display announcements
                announcements.forEachIndexed { index, announcement ->
                    ModernAnnouncementCard(
                        title = announcement.header,
                        content = announcement.text,
                        timestamp = formatTimestamp(announcement.dateAdded),
                        onCardClick = {
                            onAnnouncementClick(
                                announcement.header,
                                announcement.text,
                                formatTimestamp(announcement.dateAdded)
                            )
                        }
                    )

                    if (index < announcements.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Extra space for bottom navigation
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ModernAnnouncementCard(
    title: String,
    content: String,
    timestamp: String,
    onCardClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header row with title and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = timestamp,
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content preview
            Text(
                text = "Dear Students,",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Content snippet with arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = content.take(60) + if (content.length > 60) "..." else "",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View details",
                    tint = Color(0xFFCCCCCC),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EventsContent(
    onEventClick: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    var events by remember { mutableStateOf<List<HouseEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load events from Firebase
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("schools").document("sst").collection("houseEvents")
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .await()

            val fetchedEvents = result.documents.mapNotNull { document ->
                try {
                    HouseEvent(
                        id = document.id,
                        header = document.getString("title") ?: "",
                        desc = document.getString("description") ?: "",
                        name = "", // Keep as empty string since not in Firestore
                        date = formatEventDate(document.getTimestamp("eventDate")?.toDate()), // Convert Date to String
                        venue = document.getString("venue") ?: "",
                        dateAdded = document.getTimestamp("dateAdded")?.toDate()
                    )
                } catch (e: Exception) {
                    Log.e("EventsContent", "Error parsing event: ${document.id}", e)
                    null
                }
            }

            events = fetchedEvents
            isLoading = false

        } catch (e: Exception) {
            Log.e("EventsContent", "Error fetching events", e)
            errorMessage = "Failed to load events: ${e.message}"
            isLoading = false
        }
    }

    // UI Content
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFE91E63))
                }
            }

            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error occurred",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp
                    )
                }
            }

            events.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "No events",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBBBBBB)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No events yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Check back later for upcoming events",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                events.forEachIndexed { index, event ->
                    ModernEventCard(
                        title = event.header,
                        description = event.desc,
                        date = event.date,
                        venue = event.venue,
                        timestamp = formatTimestamp(event.dateAdded),
                        onCardClick = {
                            onEventClick(event.header, event.desc, event.date, event.venue)
                        }
                    )

                    if (index < events.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ModernEventCard(
    title: String,
    description: String,
    date: String,
    venue: String,
    timestamp: String,
    onCardClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View details",
                    tint = Color(0xFFCCCCCC),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (date.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = date,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (venue.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = venue,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (description.isNotEmpty()) {
                Text(
                    text = description.take(100) + if (description.length > 100) "..." else "",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AnnouncementDetailView(
    announcement: Announcement,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Back button and header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1A1A1A)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Announcement",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }

        // Content card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = announcement.header,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = announcement.text,
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun EventDetailView(
    event: HouseEvent,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1A1A1A)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Event Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = event.header,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (event.date.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFE91E63)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.date,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (event.venue.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Venue",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFE91E63)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.venue,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (event.desc.isNotEmpty()) {
                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.desc,
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// Helper function to format event date (NEW)
private fun formatEventDate(date: Date?): String {
    if (date == null) return ""
    val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(date)
}

// Helper function to format timestamp
private fun formatTimestamp(date: Date?): String {
    if (date == null) return "Unknown"

    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 1000 * 60 -> "Just now"
        diff < 1000 * 60 * 60 -> "${diff / (1000 * 60)}m ago"
        diff < 1000 * 60 * 60 * 24 -> "${diff / (1000 * 60 * 60)}h ago"
        diff < 1000 * 60 * 60 * 24 * 30 -> "${diff / (1000 * 60 * 60 * 24)}d ago"
        else -> {
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            formatter.format(date)
        }
    }
}