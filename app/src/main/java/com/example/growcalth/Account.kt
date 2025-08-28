package com.example.growcalth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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
    private var onDeleteSuccessCallback: (() -> Unit)? = null
    private var onDeleteErrorCallback: ((String) -> Unit)? = null

    // Add Google Sign-In launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)

                // Re-authenticate and then proceed with deletion
                account.idToken?.let { idToken ->
                    CoroutineScope(Dispatchers.Main).launch {
                        reauthenticateWithGoogleAndDelete(
                            idToken = idToken,
                            onSuccess = { onDeleteSuccessCallback?.invoke() },
                            onError = { error -> onDeleteErrorCallback?.invoke(error) }
                        )
                    }
                }
            } catch (e: ApiException) {
                Log.e("AccountActivity", "Google sign-in failed: ${e.message}")
                onDeleteErrorCallback?.invoke("Google sign-in failed: ${e.message}")
            }
        } else {
            onDeleteErrorCallback?.invoke("Google sign-in was cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authManager = FirebaseAuthManager(this)

        setContent {
            GrowCalthTheme {
                AccountScreen(
                    onBackClick = { finish() },
                    onDeleteSuccess = { navigateToMainActivity() },
                    onReauthRequired = { successCallback, errorCallback ->
                        onDeleteSuccessCallback = successCallback
                        onDeleteErrorCallback = errorCallback
                        triggerGoogleReauth()
                    }
                )
            }
        }
    }

    private fun triggerGoogleReauth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Make sure you have this in strings.xml
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
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
    onDeleteSuccess: () -> Unit = {},
    onReauthRequired: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _ -> }
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
        modifier = Modifier.fillMaxSize()
    ) {
        AppTopBar(
            title = "Account",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEBEBF2))
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

                    AccountSection(title = "SIGN IN & SECURITY") {
                        AccountActionRow(
                            label = "Change Password",
                            onClick = {
                                // TODO: Navigate to change password screen
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

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
        GoogleDeleteAccountDialog(
            isDeleting = isDeleting,
            onConfirm = {
                isDeleting = true
                // Directly trigger re-authentication with Google
                onReauthRequired(
                    { // onSuccess
                        isDeleting = false
                        showDeleteDialog = false
                        onDeleteSuccess()
                    },
                    { error -> // onError
                        isDeleting = false
                        showDeleteDialog = false
                        errorMessage = error
                        Log.e("AccountScreen", "Error deleting account: $error")
                    }
                )
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

// Updated delete dialog that shows Google re-auth flow
@Composable
fun GoogleDeleteAccountDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
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
                    text = if (isDeleting)
                        "Please sign in with Google to confirm account deletion. This action cannot be undone and all your data will be permanently removed."
                    else
                        "Are you sure you want to delete your account? You'll need to authenticate with Google to confirm. This action cannot be undone and all your data will be permanently removed.",
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
                        text = "Authenticating with Google...",
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
                            onClick = onConfirm, // This will trigger Google Sign-In
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Sign in & Delete",
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
                    userSchool = schoolName,
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
            ""
        }
        else -> ""
    }
}

private fun determineAccountType(email: String): String {
    return when {
        email.contains("@s2021.ssts.edu.sg") -> "Alumnus"
        email.contains("@s2022.ssts.edu.sg") -> "Alumnus"
        email.contains("@s2023.ssts.edu.sg") -> "Alumnus"
        email.contains("@s2024.ssts.edu.sg") -> "Student"
        email.contains("@s2025.ssts.edu.sg") -> "Student"
        email.contains("@s2026.ssts.edu.sg") -> "Student"
        email.contains("@s2027.ssts.edu.sg") -> "Student"
        email.contains("ssts.edu.sg") -> "Student"
        email.contains("@staff.") -> "Staff"
        email.contains("@teacher.") -> "Teacher"
        else -> "User"
    }
}

// Helper function to re-authenticate with Google and then delete
suspend fun reauthenticateWithGoogleAndDelete(
    idToken: String,
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

        // Create credential with the new ID token
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Re-authenticate the user
        currentUser.reauthenticate(credential).await()

        // Now delete the account
        val db = FirebaseFirestore.getInstance()
        val userId = currentUser.uid

        // Delete user data from Firestore
        db.collection("users")
            .document(userId)
            .delete()
            .await()

        // Delete the user authentication account
        currentUser.delete().await()

        onSuccess()

    } catch (e: Exception) {
        onError("Failed to delete account after re-authentication: ${e.message}")
    }
}