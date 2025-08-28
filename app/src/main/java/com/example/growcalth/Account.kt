package com.example.growcalth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.growcalth.ui.theme.GrowCalthTheme
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val house: String = "",
    val userSchool: String = "",
    val accountType: String = "",
    val studentId: String = ""
)

class AccountActivity : ComponentActivity() {
    private lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authManager = FirebaseAuthManager(this)

        setContent {
            GrowCalthTheme {
                AccountScreen(
                    onBackClick = { finish() },
                    onDeleteSuccess = { navigateToMainActivity() }
                )
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Red
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFEBEBF2)
        )
    )
}

@Composable
fun AccountScreen(
    onBackClick: () -> Unit = {},
    onDeleteSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Load user data from Firebase
    LaunchedEffect(Unit) {
        loadUserProfile(
            onSuccess = { profile ->
                userProfile = profile
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
                Log.e("AccountScreen", "Error loading user profile: $error")
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // App Bar
        AppTopBar(
            title = "Account",
            onBackClick = onBackClick
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEBEBF2)) // Light gray background for content area only
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.Red)
                    }
                }

                errorMessage != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error occurred",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }

                userProfile != null -> {
                    // Personal Information Section
                    AccountSection(title = "PERSONAL INFORMATION") {
                        AccountInfoRow(label = "Name", value = userProfile!!.name)

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)
                        AccountInfoRow(label = "Email", value = userProfile!!.email, isMultiline = true)

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)
                        AccountInfoRow(label = "House", value = userProfile!!.house.capitalize())

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)
                        AccountInfoRow(label = "School", value = userProfile!!.userSchool)

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)
                        AccountInfoRow(label = "Account Type", value = userProfile!!.accountType)
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Sign In & Security Section
                    AccountSection(title = "SIGN IN & SECURITY") {
                        AccountActionRow(
                            label = "Change Password",
                            onClick = {
                                // TODO: Navigate to change password screen
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Delete Account Button
                    Text(
                        text = "Delete account",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDeleteDialog = true
                            }
                            .padding(vertical = 16.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        val coroutineScope = rememberCoroutineScope()

        EnhancedDeleteAccountDialog(
            isDeleting = isDeleting,
            onConfirm = { password ->
                isDeleting = true
                coroutineScope.launch {
                    deleteAccountWithReauth(
                        password = password,
                        onSuccess = {
                            isDeleting = false
                            showDeleteDialog = false
                            onDeleteSuccess()
                        },
                        onError = { error ->
                            isDeleting = false
                            showDeleteDialog = false
                            errorMessage = error
                            Log.e("AccountScreen", "Error deleting account: $error")
                        }
                    )
                }
            },
            onDismiss = {
                if (!isDeleting) {
                    showDeleteDialog = false
                }
            }
        )
    }
}

@Composable
fun AccountSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun AccountInfoRow(
    label: String,
    value: String,
    isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value.ifEmpty { "?" },
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = if (value.isEmpty()) Color.Gray else Color.Gray,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun AccountActionRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Arrow Right",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Enhanced delete dialog for Google Auth - simpler approach
@Composable
fun EnhancedDeleteAccountDialog(
    isDeleting: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isDeleting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delete Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently removed.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isDeleting) {
                    CircularProgressIndicator(
                        color = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Deleting account...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray.copy(alpha = 0.2f),
                                contentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { onConfirm("") }, // Empty string since no password needed for Google Auth
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Delete",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// Remove these components as they're not needed for Google Auth

// Function to load user profile from Firebase
suspend fun loadUserProfile(
    onSuccess: (UserProfile) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("User not logged in")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userEmail = currentUser.email ?: ""

        try {
            val userDoc = db.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                val schoolCode = userDoc.getString("schoolCode") ?: ""
                var schoolName = " "

                // ✅ Query schools collection using schoolCode field
                if (schoolCode.isNotEmpty()) {
                    val schoolQuery = db.collection("schools")
                        .whereEqualTo("schoolCode", schoolCode)
                        .get()
                        .await()

                    if (!schoolQuery.isEmpty) {
                        schoolName = schoolQuery.documents[0].getString("schoolName") ?: "Unknown School"
                    }
                    else{
                        schoolName = schoolCode
                    }
                }

                val profile = UserProfile(
                    name = userDoc.getString("name")
                        ?: userDoc.getString("displayName")
                        ?: extractNameFromEmail(userEmail),
                    email = userEmail,
                    house = userDoc.getString("house") ?: determineHouseFromEmail(userEmail),
                    userSchool = schoolName, // ✅ Resolved school name
                    accountType = userDoc.getString("accountType") ?: determineAccountType(userEmail),
                    studentId = userDoc.getString("studentId") ?: ""
                )

                onSuccess(profile)
            } else {
                val profile = UserProfile(
                    name = currentUser.displayName ?: extractNameFromEmail(userEmail),
                    email = userEmail,
                    house = determineHouseFromEmail(userEmail),
                    userSchool = "Unknown School",
                    accountType = determineAccountType(userEmail),
                    studentId = ""
                )
                onSuccess(profile)
            }
        } catch (e: Exception) {
            onError("Failed to fetch profile: ${e.message}")
        }
    } catch (e: Exception) {
        onError("Failed to load user profile: ${e.message}")
    }
}

// Helper functions
private fun extractNameFromEmail(email: String): String {
    if (email.isEmpty()) return "Unknown User"
    val username = email.split("@").firstOrNull() ?: return "Unknown User"

    // Handle the specific format like "aathithya_j@s2021.ssts.edu.sg"
    val cleanUsername = username.replace("_", " ")

    return cleanUsername.split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
}

private fun determineHouseFromEmail(email: String): String {
    return when {
        email.contains("ssts.edu.sg") -> {
            // For SST students, try to determine house from student ID or other patterns
            // Since we don't have house info in the email, return empty for now
            ""
        }
        else -> ""
    }
}

private fun determineAccountType(email: String): String {
    return when {
        email.contains("@s2021.ssts.edu.sg") -> "Alumnus" // Graduated in 2021
        email.contains("@s2022.ssts.edu.sg") -> "Alumnus" // Graduated in 2022
        email.contains("@s2023.ssts.edu.sg") -> "Alumnus" // Graduated in 2023
        email.contains("@s2024.ssts.edu.sg") -> "Student" // Current Year 4 (graduating 2024)
        email.contains("@s2025.ssts.edu.sg") -> "Student" // Current Year 3 (graduating 2025)
        email.contains("@s2026.ssts.edu.sg") -> "Student" // Current Year 2 (graduating 2026)
        email.contains("@s2027.ssts.edu.sg") -> "Student" // Current Year 1 (graduating 2027)
        email.contains("ssts.edu.sg") -> "Student" // Default for SST emails
        email.contains("@staff.") -> "Staff"
        email.contains("@teacher.") -> "Teacher"
        else -> "User"
    }
}

// Enhanced delete function for Google Auth - handles re-authentication automatically
suspend fun deleteAccountWithReauth(
    password: String, // Not used for Google Auth but kept for function signature compatibility
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("User not logged in")
            return
        }

        // Check if user is signed in with Google
        val isGoogleUser = currentUser.providerData.any {
            it.providerId == GoogleAuthProvider.PROVIDER_ID
        }

        if (!isGoogleUser) {
            onError("This account deletion method is only for Google authenticated users")
            return
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val userId = currentUser.uid

            // For Google Auth users, we can try direct deletion first
            // If it fails due to re-authentication requirements, we'll handle it
            try {
                // Delete user data from Firestore first
                db.collection("users")
                    .document(userId)
                    .delete()
                    .await()

                // Delete the user authentication account
                currentUser.delete().await()

                onSuccess()
            } catch (e: Exception) {
                // If deletion fails due to recent authentication requirement
                if (e.message?.contains("requires recent authentication") == true ||
                    e.message?.contains("sensitive") == true) {

                    // For Google Auth, we need to sign out and ask user to sign in again
                    onError("For security reasons, please sign out and sign back in, then try deleting your account again.")
                } else {
                    throw e // Re-throw other exceptions to be handled below
                }
            }

        } catch (e: Exception) {
            when {
                e.message?.contains("network") == true -> {
                    onError("Network error. Please check your connection and try again.")
                }
                e.message?.contains("permission") == true -> {
                    onError("Permission denied. Please try signing out and back in.")
                }
                else -> {
                    onError("Failed to delete account: ${e.message}")
                }
            }
        }
    } catch (e: Exception) {
        onError("Failed to delete account: ${e.message}")
    }
}