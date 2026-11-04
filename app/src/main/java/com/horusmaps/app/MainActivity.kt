package com.horusmaps.app

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.runtime.DisposableEffect
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

private data class NearbyPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String
)

@Composable
private fun DashboardScreen(accountName: String) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var mapLibreMap by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    var searchText by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf("") }
    var darkMap by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("الخريطة") }
    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var locationMessage by remember { mutableStateOf("نحتاج إذن الموقع لعرض مكانك والأماكن القريبة") }
    var currentLocation by remember { mutableStateOf<org.maplibre.android.geometry.LatLng?>(null) }
    var nearbyPlaces by remember { mutableStateOf<List<NearbyPlace>>(emptyList()) }
    var loadingNearby by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<NearbyPlace?>(null) }
    var routeMessage by remember { mutableStateOf("") }
    var loadingRoute by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf<List<NearbyPlace>>(emptyList()) }
    var dashboardVisible by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf<org.maplibre.geojson.LineString?>(null) }
    val dashboardAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (dashboardVisible) 1f else 0f,
        animationSpec = tween(650, easing = FastOutSlowInEasing), label = "dashboard_fade"
    )
    val dashboardOffset by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (dashboardVisible) 0f else 26f,
        animationSpec = tween(650, easing = FastOutSlowInEasing), label = "dashboard_offset"
    )

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasLocationPermission =
            grants[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationMessage = if (hasLocationPermission) "جارٍ تحديد موقعك..." else "إذن الموقع غير متاح. يمكنك تفعيله من إعدادات التطبيق."
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("horus_map_data", android.content.Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        favorites = saved.mapNotNull { raw ->
            runCatching {
                val json = org.json.JSONObject(raw)
                NearbyPlace(json.getString("name"), json.getDouble("lat"), json.getDouble("lon"), json.optString("category", "مكان"))
            }.getOrNull()
        }
        kotlinx.coroutines.delay(90)
        dashboardVisible = true
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    androidx.compose.runtime.DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val manager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(location: android.location.Location) {
                    currentLocation = org.maplibre.android.geometry.LatLng(location.latitude, location.longitude)
                    locationMessage = "تم تحديد موقعك"
                }
            }
            try {
                val providers = listOf(android.location.LocationManager.NETWORK_PROVIDER, android.location.LocationManager.GPS_PROVIDER)
                providers.forEach { provider ->
                    if (manager.isProviderEnabled(provider)) {
                        manager.getLastKnownLocation(provider)?.let { last ->
                            if (currentLocation == null || last.time > 0L) {
                                currentLocation = org.maplibre.android.geometry.LatLng(last.latitude, last.longitude)
                            }
                        }
                        manager.requestLocationUpdates(provider, 5000L, 10f, listener, android.os.Looper.getMainLooper())
                    }
                }
            } catch (_: SecurityException) {
                locationMessage = "تعذر الوصول إلى الموقع. راجع أذونات التطبيق."
            } catch (_: Exception) {
                locationMessage = "خدمة تحديد الموقع غير متاحة حاليًا"
            }
            onDispose { runCatching { manager.removeUpdates(listener) } }
        }
    }

    val styleUrl = if (darkMap) "https://tiles.openfreemap.org/styles/dark"
        else "https://tiles.openfreemap.org/styles/liberty"

    fun updatePointSource(sourceId: String, points: List<NearbyPlace>) {
        val style = mapLibreMap?.style ?: return
        val features = points.map { place ->
            org.maplibre.geojson.Feature.fromGeometry(
                org.maplibre.geojson.Point.fromLngLat(place.longitude, place.latitude)
            )
        }
        val collection = org.maplibre.geojson.FeatureCollection.fromFeatures(features)
        (style.getSource(sourceId) as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(collection)
    }

    fun searchPlace() {
        val query = searchText.trim()
        if (query.isEmpty() || searching) return
        searching = true
        searchMessage = ""
        focusManager.clearFocus()
        Thread {
            runCatching {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                val url = java.net.URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=1")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 12000
                connection.readTimeout = 12000
                connection.setRequestProperty("User-Agent", "HorusMaps/1.0 (Android)")
                val results = org.json.JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
                if (results.length() == 0) throw IllegalStateException("لم يتم العثور على المكان")
                val result = results.getJSONObject(0)
                Pair(
                    org.maplibre.android.geometry.LatLng(result.getDouble("lat"), result.getDouble("lon")),
                    result.optString("display_name", query)
                )
            }.onSuccess { result ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    searching = false
                    searchMessage = result.second
                    mapLibreMap?.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(result.first, 14.5))
                }
            }.onFailure { error ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    searching = false
                    searchMessage = error.message ?: "تعذر البحث عن المكان"
                }
            }
        }.start()
    }

    fun loadNearbyPlaces() {
        val origin = currentLocation
        if (origin == null || loadingNearby) {
            if (origin == null) locationMessage = "حدد موقعك أولًا لعرض الأماكن القريبة"
            return
        }
        loadingNearby = true
        routeMessage = ""
        Thread {
            runCatching {
                val query = "[out:json][timeout:20];(nwr(around:1600,${origin.latitude},${origin.longitude})[name];);out center tags 35;"
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                val connection = java.net.URL("https://overpass-api.de/api/interpreter?data=$encoded").openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 25000
                val root = org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                val elements = root.optJSONArray("elements") ?: org.json.JSONArray()
                val places = mutableListOf<NearbyPlace>()
                for (index in 0 until elements.length()) {
                    val item = elements.optJSONObject(index) ?: continue
                    val tags = item.optJSONObject("tags") ?: continue
                    val name = tags.optString("name").trim()
                    if (name.isBlank()) continue
                    val center = item.optJSONObject("center")
                    val lat = if (item.has("lat")) item.optDouble("lat") else center?.optDouble("lat") ?: Double.NaN
                    val lon = if (item.has("lon")) item.optDouble("lon") else center?.optDouble("lon") ?: Double.NaN
                    if (!lat.isFinite() || !lon.isFinite()) continue
                    val category = listOf("amenity", "shop", "tourism", "leisure").firstNotNullOfOrNull { key ->
                        tags.optString(key).takeIf { it.isNotBlank() }
                    } ?: "مكان"
                    places.add(NearbyPlace(name, lat, lon, category))
                }
                places.distinctBy { it.name.lowercase() }.take(35)
            }.onSuccess { places ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    nearbyPlaces = places
                    loadingNearby = false
                    locationMessage = if (places.isEmpty()) "لم نعثر على أماكن مسجلة بالقرب منك" else "تم العثور على ${places.size} مكان قريب"
                    updatePointSource("horus-nearby-source", places)
                }
            }.onFailure {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    loadingNearby = false
                    locationMessage = "تعذر تحميل الأماكن القريبة. حاول مرة أخرى بعد قليل."
                }
            }
        }.start()
    }

    fun saveFavorite(place: NearbyPlace) {
        val exists = favorites.any { it.name == place.name && it.latitude == place.latitude && it.longitude == place.longitude }
        favorites = if (exists) favorites.filterNot { it.name == place.name && it.latitude == place.latitude && it.longitude == place.longitude } else favorites + place
        val serialized = favorites.map { placeItem ->
            org.json.JSONObject().put("name", placeItem.name).put("lat", placeItem.latitude)
                .put("lon", placeItem.longitude).put("category", placeItem.category).toString()
        }.toSet()
        context.getSharedPreferences("horus_map_data", android.content.Context.MODE_PRIVATE)
            .edit().putStringSet("favorites", serialized).apply()
    }

    fun drawRoute(destination: NearbyPlace) {
        val origin = currentLocation
        if (origin == null || loadingRoute) {
            routeMessage = "فعّل إذن الموقع أولًا لبدء المسار"
            return
        }
        selectedPlace = destination
        loadingRoute = true
        routeMessage = "جارٍ حساب المسار..."
        Thread {
            runCatching {
                val url = java.net.URL(
                    "https://router.project-osrm.org/route/v1/driving/${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?overview=full&geometries=geojson"
                )
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 12000
                connection.readTimeout = 20000
                val root = org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                val routes = root.optJSONArray("routes") ?: throw IllegalStateException("لم يتم العثور على مسار")
                if (routes.length() == 0) throw IllegalStateException("لم يتم العثور على مسار")
                val route = routes.getJSONObject(0)
                val coordinates = route.getJSONObject("geometry").getJSONArray("coordinates")
                val points = (0 until coordinates.length()).map { index ->
                    val pair = coordinates.getJSONArray(index)
                    org.maplibre.geojson.Point.fromLngLat(pair.getDouble(0), pair.getDouble(1))
                }
                val distanceKm = route.optDouble("distance") / 1000.0
                val durationMin = route.optDouble("duration") / 60.0
                Pair(org.maplibre.geojson.LineString.fromLngLats(points), "المسافة ${"%.1f".format(java.util.Locale.US, distanceKm)} كم • ${durationMin.toInt()} دقيقة تقريبًا")
            }.onSuccess { result ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    loadingRoute = false
                    routeMessage = result.second
                    currentRoute = result.first
                    val style = mapLibreMap?.style
                    (style?.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(result.first)
                    mapLibreMap?.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                        org.maplibre.android.geometry.LatLng(destination.latitude, destination.longitude), 13.5
                    ))
                    selectedTab = "المسارات"
                }
            }.onFailure {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    loadingRoute = false
                    routeMessage = "تعذر حساب المسار الآن. تحقق من الاتصال وحاول مجددًا."
                }
            }
        }.start()
    }

    fun installMapLayers(style: org.maplibre.android.maps.Style) {
        if (style.getSource("horus-user-source") == null) {
            style.addSource(org.maplibre.android.style.sources.GeoJsonSource(
                "horus-user-source", org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf())
            ))
            style.addLayer(org.maplibre.android.style.layers.CircleLayer("horus-user-layer", "horus-user-source").withProperties(
                org.maplibre.android.style.layers.PropertyFactory.circleColor(android.graphics.Color.parseColor("#1677E8")),
                org.maplibre.android.style.layers.PropertyFactory.circleRadius(8f),
                org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor(android.graphics.Color.WHITE),
                org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth(3f)
            ))
        }
        if (style.getSource("horus-nearby-source") == null) {
            style.addSource(org.maplibre.android.style.sources.GeoJsonSource(
                "horus-nearby-source", org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf())
            ))
            style.addLayer(org.maplibre.android.style.layers.CircleLayer("horus-nearby-layer", "horus-nearby-source").withProperties(
                org.maplibre.android.style.layers.PropertyFactory.circleColor(android.graphics.Color.parseColor("#F59E0B")),
                org.maplibre.android.style.layers.PropertyFactory.circleRadius(6f),
                org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor(android.graphics.Color.WHITE),
                org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth(2f)
            ))
        }
        if (style.getSource("horus-route-source") == null) {
            style.addSource(org.maplibre.android.style.sources.GeoJsonSource(
                "horus-route-source", org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf())
            ))
            style.addLayer(org.maplibre.android.style.layers.LineLayer("horus-route-layer", "horus-route-source").withProperties(
                org.maplibre.android.style.layers.PropertyFactory.lineColor(android.graphics.Color.parseColor("#0879D8")),
                org.maplibre.android.style.layers.PropertyFactory.lineWidth(5f),
                org.maplibre.android.style.layers.PropertyFactory.lineCap("round"),
                org.maplibre.android.style.layers.PropertyFactory.lineJoin("round")
            ))
        }
        currentLocation?.let { location ->
            val feature = org.maplibre.geojson.Feature.fromGeometry(org.maplibre.geojson.Point.fromLngLat(location.longitude, location.latitude))
            (style.getSource("horus-user-source") as? org.maplibre.android.style.sources.GeoJsonSource)
                ?.setGeoJson(org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf(feature)))
        }
        if (nearbyPlaces.isNotEmpty()) {
            val features = nearbyPlaces.map { place ->
                org.maplibre.geojson.Feature.fromGeometry(org.maplibre.geojson.Point.fromLngLat(place.longitude, place.latitude))
            }
            (style.getSource("horus-nearby-source") as? org.maplibre.android.style.sources.GeoJsonSource)
                ?.setGeoJson(org.maplibre.geojson.FeatureCollection.fromFeatures(features))
        }
        currentRoute?.let { line ->
            (style.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(line)
        }
    }

    LaunchedEffect(currentLocation, mapLibreMap) {
        val location = currentLocation
        if (location != null && mapLibreMap != null) {
            val point = org.maplibre.geojson.Point.fromLngLat(location.longitude, location.latitude)
            val feature = org.maplibre.geojson.Feature.fromGeometry(point)
            val collection = org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf(feature))
            (mapLibreMap?.style?.getSource("horus-user-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(collection)
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF3F6F9))) {
        androidx.compose.ui.viewinterop.AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                org.maplibre.android.MapLibre.getInstance(viewContext)
                org.maplibre.android.maps.MapView(viewContext).apply {
                    onCreate(null)
                    onStart()
                    onResume()
                    getMapAsync { map ->
                        mapLibreMap = map
                        map.setStyle(styleUrl) { style ->
                            map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                .target(org.maplibre.android.geometry.LatLng(30.0444, 31.2357)).zoom(11.5).build()
                            installMapLayers(style)
                        }
                    }
                }
            },
            update = { }
        )

        Column(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().graphicsLayer {
                alpha = dashboardAlpha
                translationY = -dashboardOffset
            }.padding(start = 15.dp, end = 15.dp, top = 40.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(shape = RoundedCornerShape(21.dp), color = Color.White, shadowElevation = 7.dp) {
                Column(Modifier.padding(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.horus_logo), "Horus Maps", Modifier.size(43.dp).background(Color(0xFFF1F6FB), RoundedCornerShape(13.dp)).padding(4.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("مرحبًا، ${accountName.ifBlank { "صديقنا" }}", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("اكتشف ما حولك بسهولة", color = Color(0xFF7B8794), fontSize = 11.sp)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEAF4FF)) {
                            Text("H", color = Blue, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp))
                        }
                    }
                    Spacer(Modifier.height(11.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = searchText, onValueChange = { searchText = it },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                            singleLine = true, shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("ابحث عن مكان أو مدينة", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Outlined.Search, "بحث", tint = Blue) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { searchPlace() })
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Blue, modifier = Modifier.size(52.dp).clickable { searchPlace() }) {
                            Box(contentAlignment = Alignment.Center) { Text(if (searching) "…" else "⌕", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                    if (searchMessage.isNotBlank()) {
                        Text(searchMessage, color = Color(0xFF667085), fontSize = 10.sp, maxLines = 2, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            if (!hasLocationPermission || currentLocation == null) {
                Surface(shape = RoundedCornerShape(15.dp), color = Color.White.copy(alpha = 0.97f), shadowElevation = 3.dp) {
                    Row(Modifier.fillMaxWidth().clickable {
                        if (!hasLocationPermission) permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                    }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("◎", color = Blue, fontSize = 20.sp)
                        Spacer(Modifier.width(9.dp))
                        Text(locationMessage, color = Color(0xFF667085), fontSize = 11.sp, modifier = Modifier.weight(1f))
                        if (!hasLocationPermission) Text("تفعيل", color = Blue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 13.dp).graphicsLayer {
                alpha = dashboardAlpha
                translationX = dashboardOffset
            },
            verticalArrangement = Arrangement.spacedBy(9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 5.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", fontSize = 25.sp, color = Navy, modifier = Modifier.clickable { mapLibreMap?.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.zoomIn()) }.padding(horizontal = 16.dp, vertical = 4.dp))
                    Box(Modifier.width(28.dp).height(1.dp).background(Color(0xFFE8EDF3)))
                    Text("−", fontSize = 25.sp, color = Navy, modifier = Modifier.clickable { mapLibreMap?.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.zoomOut()) }.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 5.dp, modifier = Modifier.clickable {
                currentLocation?.let { mapLibreMap?.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(it, 15.5)) }
                    ?: run { if (!hasLocationPermission) permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)) }
            }) { Text("◎", color = Blue, fontSize = 23.sp, modifier = Modifier.padding(12.dp)) }
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 5.dp, modifier = Modifier.clickable {
                darkMap = !darkMap
                mapLibreMap?.setStyle(if (darkMap) "https://tiles.openfreemap.org/styles/dark" else "https://tiles.openfreemap.org/styles/liberty") { style -> installMapLayers(style) }
            }) { Text(if (darkMap) "☀" else "◐", color = Navy, fontSize = 21.sp, modifier = Modifier.padding(13.dp)) }
        }

        if (selectedTab == "الخريطة") {
            Surface(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 88.dp, start = 15.dp, end = 15.dp),
                shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.97f), shadowElevation = 5.dp
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("أماكن قريبة منك", color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(if (nearbyPlaces.isEmpty()) "اكتشف المقاهي والمتاجر والمعالم حولك" else "${nearbyPlaces.size} مكان في محيطك", color = Color(0xFF7B8794), fontSize = 10.sp)
                        }
                        Button(
                            onClick = { loadNearbyPlaces() },
                            enabled = !loadingNearby && currentLocation != null,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 13.dp, vertical = 7.dp),
                            shape = RoundedCornerShape(11.dp)
                        ) { Text(if (loadingNearby) "جارٍ البحث..." else "استكشف القريب", fontSize = 11.sp) }
                    }
                    if (nearbyPlaces.isNotEmpty()) {
                        Spacer(Modifier.height(7.dp))
                        LazyColumn(Modifier.heightIn(max = 118.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(nearbyPlaces.take(5)) { place ->
                                Row(Modifier.fillMaxWidth().clickable { drawRoute(place) }.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("●", color = Color(0xFFF59E0B), fontSize = 12.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(place.name, color = Navy, fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.SemiBold)
                                        Text(place.category, color = Color(0xFF98A2B3), fontSize = 9.sp)
                                    }
                                    Text("اتجاهات", color = Blue, fontSize = 10.sp)
                                    Text(if (favorites.any { it.name == place.name && it.latitude == place.latitude }) "♥" else "♡", color = Blue, fontSize = 18.sp, modifier = Modifier.clickable { saveFavorite(place) }.padding(start = 9.dp))
                                }
                            }
                        }
                    }
                    if (locationMessage.isNotBlank()) Text(locationMessage, color = Color(0xFF7B8794), fontSize = 9.sp, maxLines = 1)
                }
            }
        } else if (selectedTab == "المحفوظات" || selectedTab == "المسارات" || selectedTab == "حسابي") {
            Surface(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 88.dp, start = 15.dp, end = 15.dp),
                shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 5.dp
            ) {
                Column(Modifier.padding(15.dp)) {
                    Text(when (selectedTab) { "المحفوظات" -> "أماكنك المحفوظة"; "المسارات" -> "المسار الحالي"; else -> "حسابك في Horus Maps" }, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(7.dp))
                    when (selectedTab) {
                        "المحفوظات" -> if (favorites.isEmpty()) Text("لم تحفظ أي مكان بعد. احفظ الأماكن من قائمة القريب منك.", color = Color(0xFF7B8794), fontSize = 11.sp)
                            else LazyColumn(Modifier.heightIn(max = 145.dp)) {
                                items(favorites) { place ->
                                    Row(Modifier.fillMaxWidth().clickable { drawRoute(place) }.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("♥", color = Blue); Spacer(Modifier.width(9.dp)); Text(place.name, color = Navy, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1); Text("اذهب", color = Blue, fontSize = 11.sp)
                                    }
                                }
                            }
                        "المسارات" -> Text(if (routeMessage.isBlank()) "اختر مكانًا قريبًا واضغط «اتجاهات» لحساب مسار القيادة." else routeMessage, color = Color(0xFF667085), fontSize = 11.sp)
                        else -> Text("مرحبًا ${accountName.ifBlank { "بك" }}. إعدادات الحساب والمعلومات الشخصية ستظهر هنا.", color = Color(0xFF667085), fontSize = 11.sp)
                    }
                    if (loadingRoute) Text("جارٍ حساب الطريق...", color = Blue, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().graphicsLayer {
                alpha = dashboardAlpha
                translationY = dashboardOffset
            },
            shape = RoundedCornerShape(topStart = 23.dp, topEnd = 23.dp), color = Color.White, shadowElevation = 12.dp
        ) {
            Row(Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 7.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                listOf("الخريطة", "المسارات", "المحفوظات", "حسابي").forEach { item ->
                    Column(Modifier.weight(1f).fillMaxSize().clickable { selectedTab = item }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(when (item) { "الخريطة" -> "⌖"; "المسارات" -> "➤"; "المحفوظات" -> "♡"; else -> "◉" },
                            color = if (selectedTab == item) Blue else Color(0xFF98A2B3), fontSize = 21.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(item, color = if (selectedTab == item) Blue else Color(0xFF667085), fontSize = 10.sp,
                            fontWeight = if (selectedTab == item) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
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
