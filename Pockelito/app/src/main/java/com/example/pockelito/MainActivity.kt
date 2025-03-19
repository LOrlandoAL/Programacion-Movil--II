package com.example.pockelito

import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pockelito.ui.theme.PockelitoTheme

class MainActivity : ComponentActivity() {
    private lateinit var soundPool: SoundPool
    private var soundGol = 0
    private var soundVictoria = 0
    private var soundBoton = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .build()

        soundGol = soundPool.load(this, R.raw.gol, 1)
        soundVictoria = soundPool.load(this, R.raw.victoria, 1)
        soundBoton = soundPool.load(this, R.raw.boton, 1)

        setContent {
            PockelitoTheme {
                GameScreen(soundPool, soundGol, soundVictoria, soundBoton)
            }
        }
    }
}

@Composable
fun GameScreen(
    soundPool: SoundPool,
    soundGol: Int,
    soundVictoria: Int,
    soundBoton: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4CAF50))
    ) {
        FootballField(soundPool, soundGol, soundVictoria, soundBoton)
    }
}

fun checkWinner(score: Int, player: String): String? {
    return if (score >= 5) player else null
}

@Composable
fun FootballField(soundPool: SoundPool, soundGol: Int, soundVictoria: Int, soundBoton: Int) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var winner by remember { mutableStateOf<String?>(null) }
        val fieldWidth = constraints.maxWidth.toFloat()
        val fieldHeight = constraints.maxHeight.toFloat()

        val accelerometerValue by rememberAccelerometerSensorValueAsState()
        val ballRadius = 30f

        var ballX by remember { mutableStateOf(fieldWidth / 2) }
        var ballY by remember { mutableStateOf(fieldHeight / 2) }

        var velocityX by remember { mutableStateOf(0f) }
        var velocityY by remember { mutableStateOf(0f) }

        var scoreTop by remember { mutableStateOf(0) }
        var scoreBottom by remember { mutableStateOf(0) }

        val goalWidth = fieldWidth * 0.2f
        val goalHeight = 20f
        val goalLeft = (fieldWidth - goalWidth) / 2
        val goalRight = goalLeft + goalWidth
        val impactFont = FontFamily(
            Font(R.font.impact)
        )
        var showDevMenu by remember { mutableStateOf(false) }
        var flag = 0;

        // 🎮 Pausa
        var isPaused by remember { mutableStateOf(false) }

        if (!isPaused && winner == null) {
            LaunchedEffect(accelerometerValue) {
                val (x, y, _) = accelerometerValue.value
                val sensitivity = 0.5f
                val friction = 0.98f

                velocityX -= x * sensitivity
                velocityY += y * sensitivity

                ballX += velocityX
                ballY += velocityY

                // Goles
                if (ballY - ballRadius <= goalHeight && ballX in goalLeft..goalRight) {
                    scoreBottom++
                    checkWinner(scoreBottom, "Bottom")?.let { winner = it }
                    soundPool.play(soundGol, 1f, 1f, 0, 0, 1f)
                    resetBall(fieldWidth, fieldHeight).also {
                        ballX = it.first
                        ballY = it.second
                        velocityX = 0f
                        velocityY = 0f
                    }
                }
                if (ballY + ballRadius >= fieldHeight - goalHeight && ballX in goalLeft..goalRight) {
                    scoreTop++
                    soundPool.play(soundGol, 1f, 1f, 0, 0, 1f)
                    checkWinner(scoreTop, "Top")?.let { winner = it }
                    resetBall(fieldWidth, fieldHeight).also {
                        ballX = it.first
                        ballY = it.second
                        velocityX = 0f
                        velocityY = 0f
                    }
                }

                // Rebotes
                if (ballX <= ballRadius || ballX >= fieldWidth - ballRadius) {
                    velocityX = -velocityX * 0.8f
                    ballX = ballX.coerceIn(ballRadius, fieldWidth - ballRadius)
                }
                if (ballY <= ballRadius || ballY >= fieldHeight - ballRadius) {
                    velocityY = -velocityY * 0.8f
                    ballY = ballY.coerceIn(ballRadius, fieldHeight - ballRadius)
                }

                velocityX *= friction
                velocityY *= friction
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "$scoreTop  :  $scoreBottom",
                color = Color.White,
                fontSize = 64.sp,
                fontFamily = impactFont,
                modifier = Modifier.align(Alignment.Center)
            )

            // 🛑 Botón de Pausa
            Button(
                onClick = { soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                    isPaused = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4), // Cyan elegante
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("Pausa")
            }

            // ⚙️ Botón Dev
            Button(
                onClick = {
                    soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                    showDevMenu = !showDevMenu
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                Text("Dev", fontSize = 14.sp)
            }

            if (showDevMenu) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        scoreTop++
                        soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) {
                        Text("Top +1", fontSize = 12.sp)
                    }

                    Button(onClick = {
                        scoreBottom++
                        soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
                        Text("Bottom +1", fontSize = 12.sp)
                    }

                    Button(onClick = {
                        soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                        isPaused = true
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))) {
                        Text("Forzar Pausa", fontSize = 12.sp)
                    }

                    Button(onClick = {
                        soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                        winner = "Top"
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                        Text("Ganador Top", fontSize = 12.sp)
                    }

                    Button(onClick = {
                        soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                        winner = "Bottom"
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                        Text("Ganador Bottom", fontSize = 12.sp)
                    }

                    Button(onClick = {
                        soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                        scoreTop = 0
                        scoreBottom = 0
                        winner = null
                        resetBall(fieldWidth, fieldHeight).also {
                            ballX = it.first
                            ballY = it.second
                            velocityX = 0f
                            velocityY = 0f
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                        Text("Reset Todo", fontSize = 12.sp)
                    }
                }
            }

            // ⚽ Dibujo del campo
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.White,
                    size = size,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                )
                drawRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset(goalLeft, 0f),
                    size = androidx.compose.ui.geometry.Size(goalWidth, goalHeight)
                )
                drawRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset(goalLeft, size.height - goalHeight),
                    size = androidx.compose.ui.geometry.Size(goalWidth, goalHeight)
                )
                drawCircle(
                    color = Color.White,
                    radius = ballRadius,
                    center = androidx.compose.ui.geometry.Offset(ballX, ballY)
                )
            }

            // 📋 Menú de Pausa
            if (isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                                isPaused = false },

                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF009688), // Verde azulado
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Text("Continuar", fontSize = 20.sp)
                        }

                        Button(
                            onClick = {
                                soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                                isPaused = false
                                scoreTop = 0
                                scoreBottom = 0
                                resetBall(fieldWidth, fieldHeight).also {
                                    ballX = it.first
                                    ballY = it.second
                                    velocityX = 0f
                                    velocityY = 0f
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F), // Rojo fuerte
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Text("Reiniciar", fontSize = 20.sp)
                        }
                    }
                }
            }

            if (winner != null) {
                LaunchedEffect(winner) {
                    if (winner != null) {
                        soundPool.play(soundVictoria, 1f, 1f, 0, 0, 1f)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Jugador ${winner} Gana!",
                            color = Color.Yellow,
                            fontSize = 25.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Button(
                            onClick = {
                                // Reiniciar_todo
                                soundPool.play(soundBoton, 1f, 1f, 0, 0, 1f)
                                winner = null
                                scoreTop = 0
                                scoreBottom = 0
                                resetBall(fieldWidth, fieldHeight).also {
                                    ballX = it.first
                                    ballY = it.second
                                    velocityX = 0f
                                    velocityY = 0f
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Text("Reiniciar", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

// 🎯 Función para resetear pelota al centro
fun resetBall(fieldWidth: Float, fieldHeight: Float): Pair<Float, Float> {
    return Pair(fieldWidth / 2, fieldHeight / 2)
}
