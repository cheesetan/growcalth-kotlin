package com.example.growcalth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.growcalth.ui.theme.Accent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.growcalth.ui.theme.GrowCalthTheme
import com.example.growcalth.LandingPageActivity
import com.example.growcalth.SignUpActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.lifecycleScope

// Firebase imports
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

// Firebase Authentication Manager
class FirebaseAuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("growcalth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_UID = "user_uid"
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isLoggedIn(): Boolean {
        val firebaseLoggedIn = auth.currentUser != null
        val localLoggedIn = sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
        return firebaseLoggedIn && localLoggedIn
    }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    private fun saveLoginState(user: FirebaseUser) {
        sharedPrefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_UID, user.uid)
            apply()
        }
    }

    fun logout() {
        auth.signOut()
        sharedPrefs.edit().clear().apply()
    }

    suspend fun signInUser(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                saveLoginState(user)
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Authentication failed")
            }
        } catch (e: Exception) {
            when {
                e.message?.contains("password") == true ||
                        e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> {
                    AuthResult.Error("Invalid email or password")
                }
                e.message?.contains("network") == true -> {
                    AuthResult.Error("Network error. Please check your connection")
                }
                e.message?.contains("too-many-requests") == true -> {
                    AuthResult.Error("Too many failed attempts. Please try again later")
                }
                else -> {
                    AuthResult.Error("Sign in failed: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }
}

// Firebase Authentication Results
sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class MainActivity : ComponentActivity() {
    private lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        authManager = FirebaseAuthManager(this)

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            navigateToLandingPage()
            return
        }

        enableEdgeToEdge()
        setContent {
            GrowCalthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFAFAFA)
                ) {
                    LoginScreen(
                        authManager = authManager,
                        modifier = Modifier.fillMaxSize(),
                        onSignUpClick = {
                            val intent = Intent(this, SignUpActivity::class.java)
                            startActivity(intent)
                        },
                        onLoginSuccess = {
                            navigateToLandingPage()
                        }
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

    private fun handleLogin(email: String, password: String) {
        lifecycleScope.launch {
            when (val result = authManager.signInUser(email, password)) {
                is AuthResult.Success -> {
                    Toast.makeText(this@MainActivity, "Welcome back!", Toast.LENGTH_SHORT).show()
                    navigateToLandingPage()
                }
                is AuthResult.Error -> {
                    Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    authManager: FirebaseAuthManager,
    modifier: Modifier = Modifier,
    onSignUpClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun handleLogin() {
        if (email.isNotBlank() && password.isNotBlank()) {
            isLoading = true
            scope.launch {
                when (val result = authManager.signInUser(email, password)) {
                    is AuthResult.Success -> {
                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                    is AuthResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
                isLoading = false
            }
        } else {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .fillMaxSize(),
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

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("School Email", color = Color(0xFF757575), fontSize = 16.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = Color(0xFF757575), fontSize = 16.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Login button
        Button(
            onClick = { handleLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Login",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign up text
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account yet? ",
                color = Color(0xFF757575),
                fontSize = 16.sp
            )
            TextButton(
                onClick = onSignUpClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Accent
                ),
                enabled = !isLoading
            ) {
                Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}