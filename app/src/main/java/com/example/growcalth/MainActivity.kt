package com.example.growcalth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.growcalth.ui.theme.Accent
import com.example.growcalth.ui.theme.GrowCalthTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Firebase Authentication Results
sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

// Firebase Authentication Manager
class FirebaseAuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("growcalth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_UID = "user_uid"
        private const val KEY_USER_NAME = "user_name"
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isLoggedIn(): Boolean {
        val firebaseLoggedIn = auth.currentUser != null
        val localLoggedIn = sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
        return firebaseLoggedIn && localLoggedIn
    }

    private fun saveLoginState(user: FirebaseUser) {
        sharedPrefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_UID, user.uid)
            putString(KEY_USER_NAME, user.displayName)
            apply()
        }
    }

    fun logout() {
        auth.signOut()
        sharedPrefs.edit().clear().apply()
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                saveLoginState(user)
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Google sign-in failed")
            }
        } catch (e: Exception) {
            AuthResult.Error("Google sign-in error: ${e.message ?: "Unknown error"}")
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase first
        FirebaseApp.initializeApp(this)
        authManager = FirebaseAuthManager(this)

        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("MainActivity", "Google Sign-In result received")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                Log.d("MainActivity", "ID Token received: ${idToken != null}")
                if (idToken != null) {
                    handleGoogleLogin(idToken)
                } else {
                    showToast("Google sign-in failed: No ID token")
                }
            } catch (e: ApiException) {
                Log.e("MainActivity", "Google sign-in failed", e)
                showToast("Google sign-in failed: ${e.statusCode}")
            }
        }

        if (authManager.isLoggedIn()) {
            navigateToLandingPage()
            return
        }

        enableEdgeToEdge()
        setContent {
            GrowCalthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFEBEBF2)
                ) {
                    GoogleLoginScreen(
                        onGoogleLoginClick = { launchGoogleSignIn() },
                        onSignUpClick = { navigateToSignUp() }
                    )
                }
            }
        }
    }

    private fun navigateToLandingPage() {
        val intent = Intent(this, LandingPageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun launchGoogleSignIn() {
        try {
            // Use the web client ID from your Firebase project
            val webClientId = getString(R.string.default_web_client_id)
            Log.d("MainActivity", "Web Client ID: $webClientId")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)

            // Sign out first to ensure account picker shows
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error launching Google Sign-In", e)
            showToast("Error: ${e.message}")
        }
    }

    private fun handleGoogleLogin(idToken: String) {
        lifecycleScope.launch {
            try {
                when (val result = authManager.signInWithGoogle(idToken)) {
                    is AuthResult.Success -> {
                        val user = result.user
                        val db: FirebaseFirestore = Firebase.firestore

                        // Check if user exists in Firestore by UID
                        val snapshot = try {
                            db.collection("users")
                                .document(user.uid)
                                .get()
                                .await()
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error checking user in Firestore", e)
                            showToast("Error checking user: ${e.message}")
                            return@launch
                        }

                        if (snapshot.exists()) {
                            // User exists → navigate to landing page
                            showToast("Welcome back!")
                            navigateToLandingPage()
                        } else {
                            // User doesn't exist → navigate to signup
                            showToast("Complete your sign-up")
                            navigateToSignUp()
                        }
                    }
                    is AuthResult.Error -> {
                        Log.e("MainActivity", "Google sign-in error: ${result.message}")
                        showToast(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Unexpected error during Google login", e)
                showToast("Unexpected error: ${e.message}")
            }
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, message, duration).show()
    }
}

@Composable
fun GoogleLoginScreen(
    onGoogleLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBEBF2))
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // House Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "House Icon",
                modifier = Modifier.size(60.dp),
                tint = Accent
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome Back",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The House You Need.",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to contribute to your House",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Google Login Button
        Button(
            onClick = onGoogleLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent
            )
        ) {
            Text(
                text = "Sign in with Google",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fixed Sign Up Text - only "Sign up" is clickable
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
            Text(
                text = "Sign up",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onSignUpClick() }
            )
        }
    }
}