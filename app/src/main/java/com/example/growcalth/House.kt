package com.example.growcalth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.growcalth.ui.theme.Accent
import com.example.growcalth.ui.theme.GrowCalthTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HouseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val schoolId = intent.getStringExtra("schoolId") ?: ""
        val schoolName = intent.getStringExtra("schoolName") ?: ""

        setContent {
            GrowCalthTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    HouseSignUpScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        schoolId = schoolId,
                        schoolName = schoolName
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseSignUpScreen(
    modifier: Modifier = Modifier,
    schoolId: String,
    schoolName: String
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var houseOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedHouse by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Load houses from Firestore
    LaunchedEffect(schoolId) {
        try {
            val snapshot = db.collection("schools")
                .document(schoolId)
                .collection("leaderboard")
                .get()
                .await()

            houseOptions = snapshot.documents.map { it.getString("name") ?: it.id }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to load houses: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Community Icon",
                tint = Accent,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to $schoolName",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (houseOptions.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedHouse,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select House") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    houseOptions.forEach { house ->
                        DropdownMenuItem(
                            text = { Text(house) },
                            onClick = {
                                selectedHouse = house
                                expanded = false
                            }
                        )
                    }
                }
            }
        } else {
            Text("No houses available for this school", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (selectedHouse.isNotEmpty()) {
                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        Toast.makeText(context, "Not signed in", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val userId = currentUser.uid
                    val email = currentUser.email ?: ""

                    val userData = mapOf(
                        "email" to email,
                        "schoolCode" to schoolId, // ✅ corrected key
                        "house" to selectedHouse
                    )

                    (context as? ComponentActivity)?.lifecycleScope?.launch {
                        try {
                            db.collection("users").document(userId).set(userData).await()
                            Toast.makeText(
                                context,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Navigate to landing activity
                            context.startActivity(Intent(context, LandingPageActivity::class.java))
                            context.finish()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please select a house", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent)
        ) {
            Text("Create Account", fontSize = 16.sp, color = Color.White)
        }
    }
}
