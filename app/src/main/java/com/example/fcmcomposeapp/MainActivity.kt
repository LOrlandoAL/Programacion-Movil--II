package com.example.fcmcomposeapp

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.FloatingActionButtonDefaults.elevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            var tokenFCM by remember { mutableStateOf("Obteniendo token...") }
            val context = LocalContext.current

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                tokenFCM = if (task.isSuccessful) task.result else "Error al obtener token"
            }

            val rolexColors = darkColorScheme(
                primary = Color(0xFF2ecc71),
                onPrimary = Color.Black,
                background = Color(0xFF0e1a12),
                surface = Color(0xFF1a2d22),
                onSurface = Color.White
            )

            MaterialTheme(colorScheme = rolexColors) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("FCM Notifier", fontWeight = FontWeight.Bold)
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = rolexColors.primary,
                                titleContentColor = rolexColors.onPrimary
                            )
                        )
                    },
                    containerColor = rolexColors.background,
                    floatingActionButton = {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("FCM Token", tokenFCM)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Token copiado", Toast.LENGTH_SHORT).show()
                                },
                                containerColor = rolexColors.primary,
                                contentColor = Color.Black
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                            }

                            FloatingActionButton(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Mi token FCM es:\n$tokenFCM")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir token con..."))
                                },
                                containerColor = rolexColors.primary,
                                contentColor = Color.Black
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir")
                            }
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(12.dp),
                            colors = CardDefaults.cardColors(containerColor = rolexColors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Token FCM:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = rolexColors.primary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 80.dp)
                                        .background(Color(0xFF273a2e), shape = RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = tokenFCM,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
