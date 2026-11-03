package com.horusmaps.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Navy = Color(0xFF062A6B)
private val Blue = Color(0xFF0879D8)
private val Cyan = Color(0xFF25C8E8)
private val Ice = Color(0xFFF4F9FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(5_000)
                showSplash = false
            }

            MaterialTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (showSplash) SplashScreen() else WelcomeScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    val logoScale = remember { Animatable(0.82f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFDFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.horus_logo),
                    contentDescription = "Horus Maps logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "HORUS MAPS",
                color = Navy,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = "The world at your sight",
                color = Color(0xFF667085),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SplashMark() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension * 0.33f
        drawCircle(color = Color.White.copy(alpha = 0.98f))
        drawCircle(
            color = Cyan,
            radius = radius,
            style = Stroke(width = size.minDimension * 0.035f, cap = StrokeCap.Round)
        )
        drawLine(
            color = Blue,
            start = Offset(center.x - radius * 0.52f, center.y + radius * 0.02f),
            end = center,
            strokeWidth = size.minDimension * 0.045f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Blue,
            start = center,
            end = Offset(center.x + radius * 0.5f, center.y - radius * 0.55f),
            strokeWidth = size.minDimension * 0.045f,
            cap = StrokeCap.Round
        )
        drawCircle(color = Navy, radius = size.minDimension * 0.07f)
    }
}

@Composable
private fun WelcomeScreen() {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.88f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentOffset = remember { Animatable(36f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        contentAlpha.animateTo(1f, tween(650, delayMillis = 180))
        contentOffset.animateTo(0f, tween(650, delayMillis = 180))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ice)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.7f))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.horus_logo),
                    contentDescription = "شعار Horus Maps",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentAlpha.value
                        translationY = contentOffset.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "أهلاً بك في Horus Maps",
                    color = Navy,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "استكشف العالم من حولك، واعثر على الأماكن، وخطط رحلتك بسهولة",
                    color = Color(0xFF667085),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentAlpha.value
                        translationY = contentOffset.value
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text("إنشاء حساب", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text(
                        "تسجيل الدخول",
                        color = Navy,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "بالاستمرار، أنت توافق على شروط الاستخدام وسياسة الخصوصية",
                color = Color(0xFF98A2B3),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Splash")
@Composable
private fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true, name = "Welcome", widthDp = 360, heightDp = 800)
@Composable
private fun WelcomeScreenPreview() {
    MaterialTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            WelcomeScreen()
        }
    }
}
