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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

private enum class AppScreen {
    Splash,
    Welcome,
    CreateAccount,
    HorusEmail,
    Success,
    Dashboard,
    Login,
    Terms,
    Privacy
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
            var accountName by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                delay(5_000)
                currentScreen = AppScreen.Welcome
            }

            MaterialTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            AppScreen.Splash -> SplashScreen()
                            AppScreen.Welcome -> WelcomeScreen(onCreateAccount = { currentScreen = AppScreen.CreateAccount })
                            AppScreen.CreateAccount -> CreateAccountScreen(onNext = { accountName = it; currentScreen = AppScreen.HorusEmail }, onBack = { currentScreen = AppScreen.Welcome }, onTerms = { currentScreen = AppScreen.Terms }, onPrivacy = { currentScreen = AppScreen.Privacy })
                            AppScreen.HorusEmail -> HorusEmailScreen(accountName = accountName, onContinue = { currentScreen = AppScreen.Success }, onBack = { currentScreen = AppScreen.CreateAccount })
                            AppScreen.Success -> SuccessScreen(accountName = accountName, onContinue = { currentScreen = AppScreen.Dashboard })
                            AppScreen.Dashboard -> DashboardScreen(accountName = accountName)
                            AppScreen.Login -> LoginPlaceholderScreen(onBack = { currentScreen = AppScreen.Welcome })
                            AppScreen.Terms -> LegalScreen(title = "شروط الاستخدام", onBack = { currentScreen = AppScreen.CreateAccount })
                            AppScreen.Privacy -> LegalScreen(title = "سياسة الخصوصية", onBack = { currentScreen = AppScreen.CreateAccount })
                        }
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
private fun WelcomeScreen(onCreateAccount: () -> Unit) {
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
                    onClick = onCreateAccount,
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

private data class Country(val name: String, val dialCode: String)

private val countries = listOf(
    Country("الأردن", "+962"), Country("الإمارات العربية المتحدة", "+971"), Country("البحرين", "+973"),
    Country("الجزائر", "+213"), Country("السعودية", "+966"), Country("السودان", "+249"), Country("العراق", "+964"),
    Country("الكويت", "+965"), Country("المغرب", "+212"), Country("اليمن", "+967"), Country("تونس", "+216"),
    Country("سوريا", "+963"), Country("عمان", "+968"), Country("فلسطين", "+970"), Country("قطر", "+974"),
    Country("لبنان", "+961"), Country("ليبيا", "+218"), Country("مصر", "+20"), Country("موريتانيا", "+222"),
    Country("الصومال", "+252"), Country("جيبوتي", "+253"), Country("جزر القمر", "+269")
).sortedBy { it.name }

private fun passwordScore(password: String): Int {
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return score
}

@Composable
private fun CountryPicker(selected: Country, onSelect: (Country) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
            Column(modifier = Modifier.padding(20.dp).height(520.dp)) {
                Text("اختر الدولة", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("بحث عن دولة") })
                Spacer(Modifier.height(10.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    countries.filter { it.name.contains(query.trim(), ignoreCase = true) }.forEach { country ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(country); onDismiss() }.padding(vertical = 13.dp)) {
                            Text(country.name, modifier = Modifier.weight(1f), color = Navy)
                            Text(country.dialCode, color = Blue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateAccountScreen(onNext: (String) -> Unit, onBack: () -> Unit, onTerms: () -> Unit, onPrivacy: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf(countries.first { it.name == "مصر" }) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var accepted by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    val score = passwordScore(password)
    val canContinue = firstName.trim().length >= 2 && familyName.trim().length >= 2 && score >= 3 && accepted

    if (showCountryPicker) CountryPicker(country, { country = it }) { showCountryPicker = false }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 18.dp)) {
            Text("إنشاء حساب", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("أدخل بياناتك الأساسية للبدء في Horus Maps", color = Color(0xFF667085), modifier = Modifier.padding(top = 6.dp, bottom = 24.dp))
            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("الاسم الأول") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = familyName, onValueChange = { familyName = it }, label = { Text("اسم العائلة") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(14.dp))
            androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(value = phone, onValueChange = { phone = it.filter(Char::isDigit) }, label = { Text("رقم الهاتف (اختياري)") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                Spacer(Modifier.width(10.dp))
                Button(onClick = { showCountryPicker = true }, modifier = Modifier.height(56.dp), shape = RoundedCornerShape(12.dp)) { Text(country.dialCode) }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = country.name, onValueChange = {}, readOnly = true, label = { Text("البلد") }, trailingIcon = { Text("⌄", color = Blue) }, modifier = Modifier.fillMaxWidth().clickable { showCountryPicker = true })
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("كلمة المرور") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), trailingIcon = { Text(if (showPassword) "إخفاء" else "عرض", color = Blue, modifier = Modifier.clickable { showPassword = !showPassword }.padding(8.dp)) })
            Spacer(Modifier.height(10.dp))
            PasswordStrength(password, score)
            Spacer(Modifier.height(14.dp))
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                Text("أوافق على ", color = Navy, fontSize = 13.sp)
                Text("شروط الاستخدام", color = Blue, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onTerms))
                Text(" و", color = Navy, fontSize = 13.sp)
                Text("سياسة الخصوصية", color = Blue, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onPrivacy))
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { onNext("${firstName.trim()} ${familyName.trim()}") }, enabled = canContinue, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) { Text("إنشاء الحساب", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("العودة") }
        }
    }
}

@Composable
private fun PasswordStrength(password: String, score: Int) {
    val label = when { password.isEmpty() -> "أدخل كلمة مرور قوية"; score <= 2 -> "ضعيفة"; score == 3 -> "متوسطة"; score == 4 -> "قوية"; else -> "قوية جدًا" }
    val color = when { score <= 2 -> Color(0xFFD92D20); score == 3 -> Color(0xFFF79009); else -> Color(0xFF12B76A) }
    Text("قوة كلمة المرور: $label", color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun HorusEmailScreen(accountName: String, onContinue: () -> Unit, onBack: () -> Unit) {
    var handle by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    val available = checked && handle.matches(Regex("^[a-zA-Z][a-zA-Z0-9._]{2,19}$")) && handle.lowercase() != "admin"
    LaunchedEffect(handle) { checked = false; delay(600); checked = handle.length >= 3 }
    Box(Modifier.fillMaxSize().background(Color.White).padding(24.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text("اختر بريد Horus Maps", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("سيكون عنوانك الرسمي لحساب $accountName", color = Color(0xFF667085), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
            OutlinedTextField(value = handle, onValueChange = { handle = it.lowercase().filter { char -> char.isLetterOrDigit() || char == '.' || char == '_' } }, modifier = Modifier.fillMaxWidth(), singleLine = true, suffix = { Text("@horus.com") }, label = { Text("اسم البريد") })
            Spacer(Modifier.height(12.dp))
            Text(if (!checked) "" else if (available) "هذا الاسم متاح" else "لا يمكن استخدام هذا الاسم", color = if (available) Color(0xFF12B76A) else Color(0xFFD92D20))
            Spacer(Modifier.height(28.dp))
            Button(onClick = onContinue, enabled = available, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) { Text("متابعة", fontWeight = FontWeight.Bold) }
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("العودة") }
        }
    }
}

@Composable
private fun SuccessScreen(accountName: String, onContinue: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(250); started = true }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(started) { if (started) alpha.animateTo(1f, tween(700)) }
    Box(Modifier.fillMaxSize().background(Color(0xFFF8FBFF)).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(alpha.value)) {
            Image(painterResource(R.drawable.horus_logo), null, modifier = Modifier.size(140.dp))
            Spacer(Modifier.height(24.dp))
            Text("تم إنشاء حسابك بنجاح", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text("مرحبًا بك يا $accountName", fontSize = 18.sp, color = Blue, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp)); Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) { Text("فتح لوحة التحكم", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun DashboardScreen(accountName: String) {
    Box(Modifier.fillMaxSize().background(Ice).padding(24.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.horus_logo), null, modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(18.dp)); Text("مرحبًا $accountName", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("لوحة تحكم Horus Maps", color = Color(0xFF667085), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun LoginPlaceholderScreen(onBack: () -> Unit) {
    LegalScreen("تسجيل الدخول", onBack)
}

@Composable
private fun LegalScreen(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.White).padding(24.dp)) {
        Column {
            Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("سيتم إضافة المحتوى الكامل للسياسة والشروط في المرحلة التالية.", modifier = Modifier.padding(top = 18.dp), color = Color(0xFF667085))
            Spacer(Modifier.weight(1f)); OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("العودة") }
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
            WelcomeScreen(onCreateAccount = {})
        }
    }
}
