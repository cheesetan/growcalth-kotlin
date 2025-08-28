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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.growcalth.ui.theme.Accent
import com.example.growcalth.ui.theme.GrowCalthTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrowCalthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFEBEBF2)
                ) {
                    SignUpScreen(
                        onBackClick = {
                            finish() // Go back to login page
                        },
                        onContinue = { code ->
                            handleJoinCode(code)
                        }
                    )
                }
            }
        }
    }

    private fun handleJoinCode(joinCode: String) {
        lifecycleScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                // Find school with matching joinCode
                val schoolsSnapshot = db.collection("schools").get().await()
                val matchedSchool = schoolsSnapshot.documents.find { doc ->
                    (doc.get("joinCode") as? String) == joinCode
                }

                if (matchedSchool != null) {
                    val schoolName = matchedSchool.get("schoolName") as? String ?: "Unknown School"
                    val schoolId = matchedSchool.id

                    // Navigate directly to HouseActivity
                    val intent = Intent(this@SignUpActivity, HouseActivity::class.java)
                    intent.putExtra("schoolId", schoolId)
                    intent.putExtra("schoolName", schoolName)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SignUpActivity, "Code doesn't exist", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignUpActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onContinue: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val maxCodeLength = 8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBEBF2))
            .padding(horizontal = 5.dp, vertical = 16.dp)
            .padding(top = 40.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section with back button and content
        Column {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(130.dp))

            // Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Community Icon",
                        tint = Accent,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title and description
            Text(
                text = "Create Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join the House Today",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your school code",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Code input field with underlines
            CodeInputField(
                code = code,
                onCodeChange = { newCode ->
                    if (newCode.length <= maxCodeLength) {
                        code = newCode
                    }
                },
                codeLength = maxCodeLength,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bottom continue button
        Button(
            onClick = { onContinue(code) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 12.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            enabled = code.length == maxCodeLength
        ) {
            Text(
                text = "Create Account",
                color = if (code.length == maxCodeLength) Color.White else Color(0xFF9E9E9E),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CodeInputField(
    code: String,
    onCodeChange: (String) -> Unit,
    codeLength: Int,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = code,
        onValueChange = { newValue ->
            // Allow both letters and digits
            val filteredValue = newValue.filter { it.isLetterOrDigit() }
            onCodeChange(filteredValue.uppercase()) // Convert to uppercase for consistency
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        decorationBox = { _ ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.padding(horizontal = 32.dp)
            ) {
                repeat(codeLength) { index ->
                    CodeDigitBox(
                        digit = code.getOrNull(index)?.toString() ?: "",
                        isFilled = index < code.length,
                        isActive = index == code.length,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
fun CodeDigitBox(
    digit: String,
    isFilled: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // The digit/letter text
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
        ) {
            Text(
                text = digit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFilled) Color.Black else Color.Transparent
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // The underline - only red when character is entered
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    color = if (isFilled) Color.Red else Color(0xFFE0E0E0)
                )
        )
    }
}