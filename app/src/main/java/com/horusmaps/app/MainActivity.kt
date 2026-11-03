package com.horusmaps.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import kotlinx.coroutines.delay

private val Navy = Color(0xFF062A6B)
private val Blue = Color(0xFF0879D8)
private val Cyan = Color(0xFF25C8E8)
private val Ice = Color(0xFFF4F9FF)

private val HorusColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF7FC),
    secondary = Cyan,
    tertiary = Navy,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Navy,
    onSurface = Navy
)

private data class AccountDraft(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val country: String = "",
    val dialCode: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

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
            var accountDraft by remember { mutableStateOf(AccountDraft()) }
            val context = LocalContext.current
            val deviceArabic = java.util.Locale.getDefault().language == "ar"

            LaunchedEffect(Unit) {
                delay(5_000)
                val preferences = context.getSharedPreferences("horus_session", android.content.Context.MODE_PRIVATE)
                val savedName = preferences.getString("account_name", "") ?: ""
                accountName = savedName
                currentScreen = if (savedName.isNotBlank()) AppScreen.Dashboard else AppScreen.Welcome
            }

            MaterialTheme(colorScheme = HorusColorScheme) {
                CompositionLocalProvider(LocalLayoutDirection provides if (deviceArabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Crossfade(
                            targetState = currentScreen,
                            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.Splash -> SplashScreen()
                                AppScreen.Welcome -> WelcomeScreen(onCreateAccount = { currentScreen = AppScreen.CreateAccount }, onLogin = { currentScreen = AppScreen.Login })
                                AppScreen.CreateAccount -> CreateAccountScreen(onNext = { draft ->
                                    accountDraft = draft
                                    accountName = "${draft.firstName} ${draft.lastName}"
                                    currentScreen = AppScreen.HorusEmail
                                }, onBack = { currentScreen = AppScreen.Welcome }, onLogin = { currentScreen = AppScreen.Login }, onTerms = { currentScreen = AppScreen.Terms }, onPrivacy = { currentScreen = AppScreen.Privacy })
                                AppScreen.HorusEmail -> HorusEmailScreen(draft = accountDraft, onContinue = { currentScreen = AppScreen.Success }, onBack = { currentScreen = AppScreen.CreateAccount })
                                AppScreen.Success -> SuccessScreen(accountName = accountName, onContinue = {
                                    context.getSharedPreferences("horus_session", android.content.Context.MODE_PRIVATE).edit().putString("account_name", accountName).apply()
                                    currentScreen = AppScreen.Dashboard
                                })
                                AppScreen.Dashboard -> DashboardScreen(accountName = accountName)
                                AppScreen.Login -> LoginScreen(onBack = { currentScreen = AppScreen.Welcome }, onLoginSuccess = { name -> accountName = name; currentScreen = AppScreen.Dashboard }, onCreateAccount = { currentScreen = AppScreen.CreateAccount })
                                AppScreen.Terms -> LegalScreen(title = "شروط الاستخدام", onBack = { currentScreen = AppScreen.CreateAccount })
                                AppScreen.Privacy -> LegalScreen(title = "سياسة الخصوصية", onBack = { currentScreen = AppScreen.CreateAccount })
                            }
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
private fun WelcomeScreen(onCreateAccount: () -> Unit, onLogin: () -> Unit) {
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
                    onClick = onLogin,
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
    Country("أفغانستان", "+93"), Country("ألبانيا", "+355"), Country("الجزائر", "+213"), Country("أندورا", "+376"),
    Country("أنغولا", "+244"), Country("الأرجنتين", "+54"), Country("أرمينيا", "+374"), Country("أستراليا", "+61"),
    Country("النمسا", "+43"), Country("أذربيجان", "+994"), Country("البحرين", "+973"), Country("بنغلاديش", "+880"),
    Country("بيلاروسيا", "+375"), Country("بلجيكا", "+32"), Country("بنين", "+229"), Country("بوليفيا", "+591"),
    Country("البرازيل", "+55"), Country("بلغاريا", "+359"), Country("الكاميرون", "+237"), Country("كندا", "+1"),
    Country("تشاد", "+235"), Country("تشيلي", "+56"), Country("الصين", "+86"), Country("كولومبيا", "+57"),
    Country("جزر القمر", "+269"), Country("الكونغو", "+242"), Country("كوستاريكا", "+506"), Country("كرواتيا", "+385"),
    Country("كوبا", "+53"), Country("قبرص", "+357"), Country("التشيك", "+420"), Country("الدنمارك", "+45"),
    Country("جيبوتي", "+253"), Country("جمهورية الدومينيكان", "+1"), Country("الإكوادور", "+593"), Country("مصر", "+20"),
    Country("السلفادور", "+503"), Country("إستونيا", "+372"), Country("إثيوبيا", "+251"), Country("فنلندا", "+358"),
    Country("فرنسا", "+33"), Country("جورجيا", "+995"), Country("ألمانيا", "+49"), Country("غانا", "+233"),
    Country("اليونان", "+30"), Country("غواتيمالا", "+502"), Country("غينيا", "+224"), Country("الهند", "+91"),
    Country("إندونيسيا", "+62"), Country("إيران", "+98"), Country("العراق", "+964"), Country("أيرلندا", "+353"),
    Country("إيطاليا", "+39"), Country("اليابان", "+81"), Country("الأردن", "+962"), Country("كازاخستان", "+7"),
    Country("كينيا", "+254"), Country("الكويت", "+965"), Country("لبنان", "+961"), Country("ليبيا", "+218"),
    Country("لوكسمبورغ", "+352"), Country("مدغشقر", "+261"), Country("ماليزيا", "+60"), Country("المالديف", "+960"),
    Country("مالي", "+223"), Country("مالطا", "+356"), Country("موريتانيا", "+222"), Country("المغرب", "+212"),
    Country("المكسيك", "+52"), Country("مولدوفا", "+373"), Country("موناكو", "+377"), Country("منغوليا", "+976"),
    Country("الجبل الأسود", "+382"), Country("ميانمار", "+95"), Country("ناميبيا", "+264"), Country("نيبال", "+977"),
    Country("هولندا", "+31"), Country("نيوزيلندا", "+64"), Country("نيجيريا", "+234"), Country("كوريا الشمالية", "+850"),
    Country("النرويج", "+47"), Country("عمان", "+968"), Country("باكستان", "+92"), Country("فلسطين", "+970"),
    Country("بنما", "+507"), Country("باراغواي", "+595"), Country("بيرو", "+51"), Country("الفلبين", "+63"),
    Country("بولندا", "+48"), Country("البرتغال", "+351"), Country("قطر", "+974"), Country("رومانيا", "+40"),
    Country("روسيا", "+7"), Country("السعودية", "+966"), Country("السنغال", "+221"), Country("صربيا", "+381"),
    Country("سنغافورة", "+65"), Country("سلوفاكيا", "+421"), Country("سلوفينيا", "+386"), Country("الصومال", "+252"),
    Country("جنوب أفريقيا", "+27"), Country("كوريا الجنوبية", "+82"), Country("جنوب السودان", "+211"), Country("إسبانيا", "+34"),
    Country("سريلانكا", "+94"), Country("السودان", "+249"), Country("السويد", "+46"), Country("سويسرا", "+41"),
    Country("سوريا", "+963"), Country("تايلاند", "+66"), Country("تونس", "+216"), Country("تركيا", "+90"),
    Country("أوغندا", "+256"), Country("أوكرانيا", "+380"), Country("الإمارات العربية المتحدة", "+971"), Country("المملكة المتحدة", "+44"),
    Country("الولايات المتحدة", "+1"), Country("أوروغواي", "+598"), Country("أوزبكستان", "+998"), Country("فنزويلا", "+58"),
    Country("فيتنام", "+84"), Country("اليمن", "+967"), Country("زامبيا", "+260"), Country("زيمبابوي", "+263")
).distinctBy { it.name }.sortedBy { it.name }

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
    val normalizedQuery = query.trim().replace(Regex("\\s+"), "")
    val filteredCountries = if (normalizedQuery.isEmpty()) countries else countries.filter { country ->
        country.name.replace(Regex("\\s+"), "").contains(normalizedQuery, ignoreCase = true) ||
            country.dialCode.contains(normalizedQuery)
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp).padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp).padding(horizontal = 18.dp, vertical = 16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("اختر دولتك", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Navy)
                        Spacer(Modifier.height(4.dp))
                        Text("جميع الدول مرتبة أبجديًا", color = Color(0xFF667085), fontSize = 13.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "إغلاق قائمة الدول", tint = Navy)
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = Blue) },
                    placeholder = { Text("ابحث عن الدولة") }
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Top) {
                    items(filteredCountries, key = { it.name }) { country ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(54.dp).clickable {
                                onSelect(country)
                                onDismiss()
                            }.padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(country.name, modifier = Modifier.weight(1f), color = if (country.name == selected.name) Blue else Navy, fontWeight = if (country.name == selected.name) FontWeight.Bold else FontWeight.Normal)
                            Text(country.dialCode, color = Blue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateAccountScreen(onNext: (AccountDraft) -> Unit, onBack: () -> Unit, onLogin: () -> Unit, onTerms: () -> Unit, onPrivacy: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf(countries.first { it.name == "مصر" }) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var accepted by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    val score = passwordScore(password)
    val canContinue = firstName.trim().length >= 2 && familyName.trim().length >= 2 && score >= 3 && confirmPassword == password && confirmPassword.isNotEmpty() && accepted
    val pageAlpha = remember { Animatable(0f) }
    val pageOffset = remember { Animatable(28f) }

    LaunchedEffect(Unit) {
        pageAlpha.animateTo(1f, tween(500))
        pageOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }
    if (showCountryPicker) CountryPicker(country, { country = it }) { showCountryPicker = false }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .graphicsLayer { alpha = pageAlpha.value; translationY = pageOffset.value }
        ) {
            Box(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("العودة", color = Navy, fontWeight = FontWeight.SemiBold) }
                    Spacer(Modifier.weight(1f))
                    Image(painterResource(R.drawable.horus_logo), null, Modifier.size(44.dp))
                }
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("أنشئ حسابك", color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("خطوات بسيطة، واستكشف العالم من حولك.", color = Color(0xFF667085), fontSize = 15.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().height(3.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).fillMaxSize().background(Blue, RoundedCornerShape(8.dp)))
                    Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFFDDE5EF), RoundedCornerShape(8.dp)))
                    Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFFDDE5EF), RoundedCornerShape(8.dp)))
                }
                Spacer(Modifier.height(24.dp))
                Text("بياناتك الشخصية", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = firstName, onValueChange = { firstName = it.take(30) }, label = { Text("الاسم الأول") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = familyName, onValueChange = { familyName = it.take(30) }, label = { Text("اسم العائلة") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { showCountryPicker = true }, modifier = Modifier.width(92.dp).height(56.dp), shape = RoundedCornerShape(14.dp)) { Text(country.dialCode, color = Navy, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it.filter(Char::isDigit) }, label = { Text("رقم الهاتف (اختياري)") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                }
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().clickable(onClick = { showCountryPicker = true })) {
                    OutlinedTextField(
                        value = country.name,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("البلد") },
                        trailingIcon = { Text("⌄", color = Blue, fontSize = 18.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Navy,
                            disabledBorderColor = Color(0xFFB8C0CC),
                            disabledLabelColor = Color(0xFF667085),
                            disabledTrailingIconColor = Blue
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text("حماية حسابك", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showPassword) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                tint = Color(0xFF667085)
                            )
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
                PasswordStrength(password, score)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("تأكيد كلمة المرور") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showConfirmPassword) "إخفاء تأكيد كلمة المرور" else "إظهار تأكيد كلمة المرور",
                                tint = Color(0xFF667085)
                            )
                        }
                    }
                )
                if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                    Spacer(Modifier.height(6.dp))
                    Text("كلمتا المرور غير متطابقتين", color = Color(0xFFD92D20), fontSize = 13.sp)
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                    Column(Modifier.padding(top = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("أوافق على ", color = Navy, fontSize = 13.sp)
                            Text("شروط الاستخدام", color = Blue, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onTerms))
                            Text(" و", color = Navy, fontSize = 13.sp)
                        }
                        Text("سياسة الخصوصية", color = Blue, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onPrivacy))
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(onClick = {
                    onNext(AccountDraft(firstName.trim(), familyName.trim(), phone, country.name, country.dialCode, password, confirmPassword))
                }, enabled = canContinue, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue, disabledContainerColor = Color(0xFFD0D5DD))) { Text("متابعة إلى البريد Horus", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(22.dp))
                TextButton(onClick = onLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("لديك حساب؟ تسجيل الدخول") }
            }
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
private fun HorusEmailScreen(draft: AccountDraft, onContinue: () -> Unit, onBack: () -> Unit) {
    var handle by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val accountName = "${draft.firstName} ${draft.lastName}"
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
            if (error.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(error, color = Color(0xFFD92D20), fontSize = 13.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    loading = true
                    error = ""
                    Thread {
                        runCatching {
                            val connection = java.net.URL("https://horus-maps.horus-egypt-company.workers.dev/api/auth/register")
                                .openConnection() as java.net.HttpURLConnection
                            connection.requestMethod = "POST"
                            connection.doOutput = true
                            connection.connectTimeout = 15_000
                            connection.readTimeout = 15_000
                            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            val payload = org.json.JSONObject()
                                .put("firstName", draft.firstName)
                                .put("lastName", draft.lastName)
                                .put("phone", draft.phone)
                                .put("country", draft.country)
                                .put("dialCode", draft.dialCode)
                                .put("emailHandle", handle)
                                .put("password", draft.password)
                                .put("confirmPassword", draft.confirmPassword)
                            connection.outputStream.use { it.write(payload.toString().toByteArray()) }
                            val response = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
                            org.json.JSONObject(response.bufferedReader().readText())
                        }.onSuccess { result ->
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                loading = false
                                if (result.optBoolean("ok")) onContinue()
                                else error = when (result.optString("error")) {
                                    "email_taken" -> "اسم البريد مستخدم بالفعل"
                                    "invalid_password" -> "كلمة المرور لا تستوفي مستوى الحماية المطلوب"
                                    else -> "تعذر إنشاء الحساب، حاول مرة أخرى"
                                }
                            }
                        }.onFailure {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                loading = false
                                error = "تعذر الاتصال بالخادم. تحقق من الإنترنت ثم حاول مرة أخرى"
                            }
                        }
                    }.start()
                },
                enabled = available && !loading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (loading) "جارٍ إنشاء الحساب..." else "إنشاء الحساب", fontWeight = FontWeight.Bold) }
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("العودة") }
        }
    }
}

@Composable
private fun SuccessScreen(accountName: String, onContinue: () -> Unit) {
    val contentAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.94f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(650))
        contentScale.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
    }
    Box(
        Modifier.fillMaxSize().background(Color(0xFFF6FBFF)).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(contentAlpha.value).scale(contentScale.value)
        ) {
            Box(Modifier.size(112.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.horus_logo), "شعار Horus Maps", Modifier.fillMaxSize().padding(12.dp))
            }
            Spacer(Modifier.height(28.dp))
            Box(Modifier.size(54.dp).background(Color(0xFFE8F7FC), CircleShape), contentAlignment = Alignment.Center) {
                Text("✓", color = Blue, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))
            Text("تم إنشاء حسابك بنجاح", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text("أنت الآن جاهز لبدء رحلتك معنا، يا $accountName", fontSize = 16.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp)) { Text("استكشف Horus Maps", fontWeight = FontWeight.Bold) }
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
private fun LoginScreen(onBack: () -> Unit, onLoginSuccess: (String) -> Unit, onCreateAccount: () -> Unit) {
    var handle by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current
    val pageAlpha = remember { Animatable(0f) }
    val pageOffset = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        pageAlpha.animateTo(1f, tween(500))
        pageOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
                .graphicsLayer { alpha = pageAlpha.value; translationY = pageOffset.value }
        ) {
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("العودة", color = Navy, fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.weight(1f))
                Image(painterResource(R.drawable.horus_logo), null, Modifier.size(44.dp))
            }
            Spacer(Modifier.height(38.dp))
            Text("تسجيل الدخول", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(6.dp))
            Text("أدخل بريد Horus Maps وكلمة مرورك للمتابعة.", color = Color(0xFF667085), fontSize = 15.sp)
            Spacer(Modifier.height(30.dp))
            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it.lowercase().filter { char -> char.isLetterOrDigit() || char == '.' || char == '_' } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                suffix = { Text("@horus.com", color = Color(0xFF667085)) },
                label = { Text("بريد Horus Maps") }
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("كلمة المرور") },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (showPassword) "إخفاء كلمة المرور" else "إظهار كلمة المرور", tint = Color(0xFF667085))
                    }
                }
            )
            if (error.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(error, color = Color(0xFFD92D20), fontSize = 13.sp)
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    error = ""
                    loading = true
                    val email = "${handle}@horus.com"
                    Thread {
                        runCatching {
                            val connection = java.net.URL("https://horus-maps.horus-egypt-company.workers.dev/api/auth/login")
                                .openConnection() as java.net.HttpURLConnection
                            connection.requestMethod = "POST"
                            connection.doOutput = true
                            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            connection.outputStream.use { it.write(org.json.JSONObject().put("email", email).put("password", password).toString().toByteArray()) }
                            val response = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
                            org.json.JSONObject(response.bufferedReader().readText())
                        }.onSuccess { result ->
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                loading = false
                                if (result.optBoolean("ok")) {
                                    val name = result.optJSONObject("user")?.optString("name") ?: handle
                                    context.getSharedPreferences("horus_session", android.content.Context.MODE_PRIVATE).edit()
                                        .putString("account_name", name).putString("account_email", email).apply()
                                    onLoginSuccess(name)
                                } else error = when (result.optString("error")) { "invalid_credentials" -> "البريد أو كلمة المرور غير صحيحة" else -> "تعذر تسجيل الدخول، حاول مرة أخرى" }
                            }
                        }.onFailure {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { loading = false; error = "تعذر الاتصال بالخادم" }
                        }
                    }.start()
                },
                enabled = !loading && handle.length >= 3 && password.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (loading) "جارٍ تسجيل الدخول..." else "تسجيل الدخول", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("ليس لديك حساب؟ ", color = Color(0xFF667085))
                Text("إنشاء حساب", color = Blue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onCreateAccount))
            }
        }
    }
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
            WelcomeScreen(onCreateAccount = {}, onLogin = {})
        }
    }
}
