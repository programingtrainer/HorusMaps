package com.horusmaps.app

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

private val Navy = Color(0xFF062A6B)
private val Blue = Color(0xFF0879D8)
private val Cyan = Color(0xFF25C8E8)
private val Ice = Color(0xFFF4F9FF)

private val nominatimRequestLock = Any()
private var lastNominatimRequestAt = 0L

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
            val deviceLayoutDirection = if (android.text.TextUtils.getLayoutDirectionFromLocale(java.util.Locale.getDefault()) == android.view.View.LAYOUT_DIRECTION_RTL) LayoutDirection.Rtl else LayoutDirection.Ltr

            LaunchedEffect(Unit) {
                delay(1_800)
                val preferences = context.getSharedPreferences("horus_session", android.content.Context.MODE_PRIVATE)
                val savedName = preferences.getString("account_name", "") ?: ""
                accountName = savedName
                currentScreen = if (savedName.isNotBlank()) AppScreen.Dashboard else AppScreen.Welcome
            }

            MaterialTheme(colorScheme = HorusColorScheme) {
                CompositionLocalProvider(LocalLayoutDirection provides deviceLayoutDirection) {
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
                                AppScreen.Dashboard -> DashboardScreen(
                                    accountName = accountName,
                                    onLogout = {
                                        context.getSharedPreferences("horus_session", android.content.Context.MODE_PRIVATE).edit().clear().apply()
                                        accountName = ""
                                        currentScreen = AppScreen.Welcome
                                    }
                                )
                                AppScreen.Login -> LoginScreen(onBack = { currentScreen = AppScreen.Welcome }, onLoginSuccess = { name -> accountName = name; currentScreen = AppScreen.Dashboard }, onCreateAccount = { currentScreen = AppScreen.CreateAccount })
                                AppScreen.Terms -> LegalScreen(title = localizedUiText("شروط الاستخدام", "Terms of Use"), onBack = { currentScreen = AppScreen.CreateAccount })
                                AppScreen.Privacy -> LegalScreen(title = localizedUiText("سياسة الخصوصية", "Privacy Policy"), onBack = { currentScreen = AppScreen.CreateAccount })
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
                    contentDescription = localizedUiText("شعار Horus Maps", "Horus Maps logo"),
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
                    text = localizedUiText("أهلاً بك في Horus Maps", "Welcome to Horus Maps"),
                    color = Navy,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = localizedUiText("استكشف العالم من حولك، واعثر على الأماكن، وخطط رحلتك بسهولة", "Explore the world around you, find places, and plan your journey with ease"),
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
                    Text(localizedUiText("إنشاء حساب", "Create account"), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onLogin,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text(
                        localizedUiText("تسجيل الدخول", "Log in"),
                        color = Navy,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = localizedUiText("بالاستمرار، أنت توافق على شروط الاستخدام وسياسة الخصوصية", "By continuing, you agree to the Terms of Use and Privacy Policy"),
                color = Color(0xFF98A2B3),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class Country(val name: String, val dialCode: String)

private fun localizedCountries(): List<Country> {
    val locale = java.util.Locale.getDefault()
    val arabicLocale = java.util.Locale("ar")
    return countries.map { country ->
        val isoCode = java.util.Locale.getISOCountries().firstOrNull { code ->
            java.util.Locale("", code).getDisplayCountry(arabicLocale).equals(country.name, ignoreCase = true)
        }
        val displayName = isoCode?.let { java.util.Locale("", it).getDisplayCountry(locale) }
            ?.takeIf { it.isNotBlank() } ?: country.name
        Country(displayName, country.dialCode)
    }.distinctBy { it.dialCode + it.name }.sortedBy { it.name }
}

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

private fun localizedUiText(arabic: String, english: String): String =
    if (java.util.Locale.getDefault().language == "ar") arabic else english

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
private fun CountryPicker(selected: Country, availableCountries: List<Country>, onSelect: (Country) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.trim().replace(Regex("\\s+"), "")
    val filteredCountries = if (normalizedQuery.isEmpty()) availableCountries else availableCountries.filter { country ->
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
                        Text(localizedUiText("اختر دولتك", "Choose your country"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Navy)
                        Spacer(Modifier.height(4.dp))
                        Text(localizedUiText("جميع الدول مرتبة أبجديًا", "All countries in alphabetical order"), color = Color(0xFF667085), fontSize = 13.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = localizedUiText("إغلاق قائمة الدول", "Close country list"), tint = Navy)
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
                    placeholder = { Text(localizedUiText("ابحث عن الدولة", "Search for a country")) }
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
    val availableCountries = remember { localizedCountries() }
    var country by remember { mutableStateOf(availableCountries.first { it.dialCode == "+20" }) }
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
    if (showCountryPicker) CountryPicker(country, availableCountries, { country = it }) { showCountryPicker = false }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .graphicsLayer { alpha = pageAlpha.value; translationY = pageOffset.value }
        ) {
            Box(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text(localizedUiText("العودة", "Back"), color = Navy, fontWeight = FontWeight.SemiBold) }
                    Spacer(Modifier.weight(1f))
                    Image(painterResource(R.drawable.horus_logo), null, Modifier.size(44.dp))
                }
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(localizedUiText("أنشئ حسابك", "Create your account"), color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(localizedUiText("خطوات بسيطة، واستكشف العالم من حولك.", "A few simple steps, then explore the world around you."), color = Color(0xFF667085), fontSize = 15.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().height(3.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).fillMaxSize().background(Blue, RoundedCornerShape(8.dp)))
                    Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFFDDE5EF), RoundedCornerShape(8.dp)))
                    Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFFDDE5EF), RoundedCornerShape(8.dp)))
                }
                Spacer(Modifier.height(24.dp))
                Text(localizedUiText("بياناتك الشخصية", "Personal information"), color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = firstName, onValueChange = { firstName = it.take(30) }, label = { Text(localizedUiText("الاسم الأول", "First name")) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = familyName, onValueChange = { familyName = it.take(30) }, label = { Text(localizedUiText("اسم العائلة", "Last name")) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { showCountryPicker = true }, modifier = Modifier.width(92.dp).height(56.dp), shape = RoundedCornerShape(14.dp)) { Text(country.dialCode, color = Navy, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it.filter(Char::isDigit) }, label = { Text(localizedUiText("رقم الهاتف (اختياري)", "Phone number (optional)")) }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                }
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().clickable(onClick = { showCountryPicker = true })) {
                    OutlinedTextField(
                        value = country.name,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text(localizedUiText("البلد", "Country")) },
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
                Text(localizedUiText("حماية حسابك", "Secure your account"), color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(localizedUiText("كلمة المرور", "Password")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showPassword) localizedUiText("إخفاء كلمة المرور", "Hide password") else localizedUiText("إظهار كلمة المرور", "Show password"),
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
                    label = { Text(localizedUiText("تأكيد كلمة المرور", "Confirm password")) },
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
                                contentDescription = if (showConfirmPassword) localizedUiText("إخفاء تأكيد كلمة المرور", "Hide password confirmation") else localizedUiText("إظهار تأكيد كلمة المرور", "Show password confirmation"),
                                tint = Color(0xFF667085)
                            )
                        }
                    }
                )
                if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                    Spacer(Modifier.height(6.dp))
                    Text(localizedUiText("كلمتا المرور غير متطابقتين", "Passwords do not match"), color = Color(0xFFD92D20), fontSize = 13.sp)
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                    Column(Modifier.padding(top = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(localizedUiText("أوافق على ", "I agree to "), color = Navy, fontSize = 13.sp)
                            Text(localizedUiText("شروط الاستخدام", "Terms of Use"), color = Blue, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onTerms))
                            Text(localizedUiText(" و", " and "), color = Navy, fontSize = 13.sp)
                        }
                        Text(localizedUiText("سياسة الخصوصية", "Privacy Policy"), color = Blue, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onPrivacy))
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(onClick = {
                    onNext(AccountDraft(firstName.trim(), familyName.trim(), phone, country.name, country.dialCode, password, confirmPassword))
                }, enabled = canContinue, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue, disabledContainerColor = Color(0xFFD0D5DD))) { Text(localizedUiText("متابعة إلى البريد Horus", "Continue to Horus email"), fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(22.dp))
                TextButton(onClick = onLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(localizedUiText("لديك حساب؟ تسجيل الدخول", "Already have an account? Log in")) }
            }
        }
    }
}

@Composable
private fun PasswordStrength(password: String, score: Int) {
    val label = when { password.isEmpty() -> localizedUiText("أدخل كلمة مرور قوية", "Enter a strong password"); score <= 2 -> localizedUiText("ضعيفة", "Weak"); score == 3 -> localizedUiText("متوسطة", "Medium"); score == 4 -> localizedUiText("قوية", "Strong"); else -> localizedUiText("قوية جدًا", "Very strong") }
    val color = when { score <= 2 -> Color(0xFFD92D20); score == 3 -> Color(0xFFF79009); else -> Color(0xFF12B76A) }
    Text(localizedUiText("قوة كلمة المرور: $label", "Password strength: $label"), color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun HorusEmailScreen(draft: AccountDraft, onContinue: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var handle by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val accountName = "${draft.firstName} ${draft.lastName}"
    var checked by remember { mutableStateOf(false) }
    var available by remember { mutableStateOf(false) }
    var checkingEmail by remember { mutableStateOf(false) }
    var emailCheckFailed by remember { mutableStateOf(false) }
    LaunchedEffect(handle) {
        checked = false
        available = false
        emailCheckFailed = false
        checkingEmail = false
        val candidate = handle.matches(Regex("^[a-zA-Z][a-zA-Z0-9._]{2,19}$")) && handle.lowercase() != "admin"
        if (!candidate) {
            checked = handle.length >= 3
            return@LaunchedEffect
        }
        delay(500)
        checkingEmail = true
        val result = runCatching {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val encodedHandle = java.net.URLEncoder.encode(handle, "UTF-8")
                val connection = (java.net.URL("https://horus-maps.horus-egypt-company.workers.dev/api/email/check?handle=$encodedHandle")
                    .openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }
                try {
                    if (connection.responseCode !in 200..299) throw IllegalStateException("Email availability request failed")
                    org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                } finally {
                    connection.disconnect()
                }
            }
        }
        checkingEmail = false
        checked = true
        if (result.isSuccess) {
            available = result.getOrThrow().optBoolean("available", false)
        } else {
            emailCheckFailed = true
        }
    }
    Box(Modifier.fillMaxSize().background(Color.White).padding(24.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(localizedUiText("اختر بريد Horus Maps", "Choose your Horus Maps email"), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(localizedUiText("سيكون عنوانك الرسمي لحساب $accountName", "This will be the official address for $accountName"), color = Color(0xFF667085), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
            OutlinedTextField(value = handle, onValueChange = { handle = it.lowercase().filter { char -> char.isLetterOrDigit() || char == '.' || char == '_' } }, modifier = Modifier.fillMaxWidth(), singleLine = true, suffix = { Text("@horus.com") }, label = { Text(localizedUiText("اسم البريد", "Email username")) })
            Text(
                localizedUiText("هذا معرّف لتسجيل الدخول داخل Horus Maps، وليس صندوق بريد لاستقبال الرسائل.", "This is an in-app sign-in ID, not a mailbox for receiving email."),
                color = Color(0xFF667085),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 5.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                when {
                    checkingEmail -> localizedUiText("جارٍ التحقق من توفر البريد...", "Checking email availability...")
                    !checked -> ""
                    emailCheckFailed -> localizedUiText("تعذر التحقق من البريد. افحص اتصالك وحاول مجددًا.", "Could not verify this email. Check your connection and try again.")
                    available -> localizedUiText("هذا الاسم متاح", "This username is available")
                    else -> localizedUiText("اسم البريد مستخدم أو غير صالح", "This username is taken or invalid")
                },
                color = if (available) Color(0xFF12B76A) else Color(0xFFD92D20)
            )
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
                                if (result.optBoolean("ok")) {
                                    context.getSharedPreferences("horus_session", android.content.Context.MODE_PRIVATE).edit()
                                        .putString("account_email", "${handle}@horus.com").apply()
                                    onContinue()
                                } else error = when (result.optString("error")) {
                                    "email_taken" -> localizedUiText("اسم البريد مستخدم بالفعل", "This email username is already taken")
                                    "invalid_password" -> localizedUiText("كلمة المرور لا تستوفي مستوى الحماية المطلوب", "The password does not meet the required security level")
                                    else -> localizedUiText("تعذر إنشاء الحساب، حاول مرة أخرى", "Could not create the account. Please try again.")
                                }
                            }
                        }.onFailure {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                loading = false
                                error = localizedUiText("تعذر الاتصال بالخادم. تحقق من الإنترنت ثم حاول مرة أخرى", "Could not connect to the server. Check your internet connection and try again.")
                            }
                        }
                    }.start()
                },
                enabled = available && !loading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (loading) localizedUiText("جارٍ إنشاء الحساب...", "Creating account...") else localizedUiText("إنشاء الحساب", "Create account"), fontWeight = FontWeight.Bold) }
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(localizedUiText("العودة", "Back")) }
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
                Image(painterResource(R.drawable.horus_logo), localizedUiText("شعار Horus Maps", "Horus Maps logo"), Modifier.fillMaxSize().padding(12.dp))
            }
            Spacer(Modifier.height(28.dp))
            Box(Modifier.size(54.dp).background(Color(0xFFE8F7FC), CircleShape), contentAlignment = Alignment.Center) {
                Text("✓", color = Blue, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))
            Text(localizedUiText("تم إنشاء حسابك بنجاح", "Your account was created successfully"), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text(localizedUiText("أنت الآن جاهز لبدء رحلتك معنا، يا $accountName", "You are ready to start your journey, $accountName"), fontSize = 16.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp)) { Text(localizedUiText("استكشف Horus Maps", "Explore Horus Maps"), fontWeight = FontWeight.Bold) }
        }
    }
}

private fun nearbyPlaceIcon(category: String): ImageVector = when (category.lowercase(java.util.Locale.ROOT)) {
    "restaurant", "fast_food", "food_court" -> Icons.Filled.Restaurant
    "cafe", "coffee" -> Icons.Filled.LocalCafe
    "fuel" -> Icons.Filled.LocalGasStation
    "hospital", "clinic", "doctors" -> Icons.Filled.LocalHospital
    "pharmacy" -> Icons.Filled.LocalPharmacy
    "park", "garden", "playground", "nature_reserve" -> Icons.Filled.Park
    "supermarket", "convenience", "mall", "department_store" -> Icons.Filled.ShoppingCart
    "hotel", "hostel", "motel", "guest_house" -> Icons.Filled.Hotel
    "atm", "bank" -> Icons.Filled.AccountBalance
    "place_of_worship", "mosque", "masjid" -> Icons.Filled.AccountBalance
    "station", "railway", "train_station", "halt", "tram_stop" -> Icons.Filled.Train
    else -> Icons.Filled.Place
}

private fun nearbyPlaceColor(category: String): String = when (category.lowercase(java.util.Locale.ROOT)) {
    "restaurant", "fast_food", "food_court", "cafe", "coffee" -> "#E87932"
    "fuel" -> "#0B84C6"
    "hospital", "clinic", "doctors", "pharmacy" -> "#D64545"
    "park", "garden", "playground", "nature_reserve" -> "#26966A"
    "supermarket", "convenience", "mall", "department_store" -> "#3267C8"
    "hotel", "hostel", "motel", "guest_house" -> "#7956B8"
    "atm", "bank" -> "#16877A"
    "place_of_worship", "mosque", "masjid" -> "#B78328"
    "station", "railway", "train_station", "halt", "tram_stop" -> "#526477"
    else -> "#0879D8"
}

private data class NearbyPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String
)

private data class RouteGeocodeResult(
    val point: org.maplibre.android.geometry.LatLng,
    val supportsFlight: Boolean
)

private data class RouteOption(
    val mode: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val geometry: org.maplibre.geojson.LineString,
    val instructions: List<String> = emptyList(),
    val isEstimate: Boolean = false,
    val details: String = ""
)

private fun decodePolyline6(encoded: String): List<org.maplibre.geojson.Point> {
    val points = ArrayList<org.maplibre.geojson.Point>()
    var index = 0
    var lat = 0
    var lon = 0
    while (index < encoded.length) {
        var result = 0
        var shift = 0
        var byte: Int
        do {
            byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
        } while (byte >= 0x20 && index < encoded.length)
        lat += if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        result = 0
        shift = 0
        do {
            byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
        } while (byte >= 0x20 && index < encoded.length)
        lon += if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        points.add(org.maplibre.geojson.Point.fromLngLat(lon / 1_000_000.0, lat / 1_000_000.0))
    }
    return points
}

private fun requestValhallaRoute(
    origin: org.maplibre.android.geometry.LatLng,
    destination: org.maplibre.android.geometry.LatLng,
    costing: String
): RouteOption {
    val request = org.json.JSONObject()
        .put("locations", org.json.JSONArray()
            .put(org.json.JSONObject().put("lat", origin.latitude).put("lon", origin.longitude))
            .put(org.json.JSONObject().put("lat", destination.latitude).put("lon", destination.longitude)))
        .put("costing", costing)
        .put("units", "kilometers")
        .put("language", java.util.Locale.getDefault().toLanguageTag())
        .put("shape_format", "polyline6")
        .put("directions_type", "instructions")
    val connection = java.net.URL("https://valhalla1.openstreetmap.de/route")
        .openConnection() as java.net.HttpURLConnection
    connection.requestMethod = "POST"
    connection.connectTimeout = 15000
    connection.readTimeout = 30000
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
    connection.setRequestProperty("Accept", "application/json")
    connection.setRequestProperty("X-Client-Id", "HorusMaps")
    connection.outputStream.use { it.write(request.toString().toByteArray(Charsets.UTF_8)) }
    val responseCode = connection.responseCode
    val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
    val body = stream.bufferedReader().use { it.readText() }
    if (responseCode !in 200..299) throw IllegalStateException("Routing service error ($responseCode)")
    val root = org.json.JSONObject(body)
    val trip = root.optJSONObject("trip") ?: throw IllegalStateException("No route found")
    val summary = trip.optJSONObject("summary") ?: throw IllegalStateException("No route summary")
    val legs = trip.optJSONArray("legs") ?: throw IllegalStateException("No route geometry")
    val allPoints = ArrayList<org.maplibre.geojson.Point>()
    val instructions = ArrayList<String>()
    for (i in 0 until legs.length()) {
        val leg = legs.getJSONObject(i)
        val shape = leg.optString("shape")
        if (shape.isNotBlank()) allPoints.addAll(decodePolyline6(shape))
        val maneuvers = leg.optJSONArray("maneuvers") ?: org.json.JSONArray()
        for (j in 0 until maneuvers.length()) {
            val instruction = maneuvers.optJSONObject(j)?.optString("instruction").orEmpty()
            if (instruction.isNotBlank()) instructions.add(instruction)
        }
    }
    if (allPoints.size < 2) throw IllegalStateException("Route geometry is unavailable")
    return RouteOption(
        mode = costing,
        distanceMeters = summary.optDouble("length") * 1000.0,
        durationSeconds = summary.optDouble("time"),
        geometry = org.maplibre.geojson.LineString.fromLngLats(allPoints),
        instructions = instructions
    )
}

private fun straightLineMeters(a: org.maplibre.android.geometry.LatLng, b: org.maplibre.android.geometry.LatLng): Double {
    val earthRadius = 6_371_000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val h = kotlin.math.sin(dLat / 2).let { it * it } +
        kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
        kotlin.math.sin(dLon / 2).let { it * it }
    return 2 * earthRadius * kotlin.math.asin(kotlin.math.sqrt(h.coerceIn(0.0, 1.0)))
}


private data class AirportCandidate(
    val iata: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val accessDistanceMeters: Double
)

private data class FlightEstimate(
    val distanceMeters: Double,
    val durationSeconds: Double,
    val geometry: org.maplibre.geojson.LineString,
    val details: String
)


private fun searchGeocodeWithFallback(query: String, languageTag: String): org.json.JSONObject {
    var nominatimFailure: Throwable? = null
    try {
        synchronized(nominatimRequestLock) {
            val elapsed = System.currentTimeMillis() - lastNominatimRequestAt
            if (elapsed < 1_000L) Thread.sleep(1_000L - elapsed)
            lastNominatimRequestAt = System.currentTimeMillis()
        }
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val encodedLanguage = java.net.URLEncoder.encode(languageTag, "UTF-8")
        val connection = (java.net.URL(
            "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=jsonv2&limit=5&addressdetails=1&namedetails=1&accept-language=$encodedLanguage"
        ).openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 12_000
            setRequestProperty("User-Agent", "HorusMaps/1.0 (https://github.com/programingtrainer/HorusMaps)")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Accept-Language", languageTag)
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IllegalStateException("Nominatim HTTP $code")
            val results = org.json.JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            if (results.length() > 0) {
                val source = results.getJSONObject(0)
                return org.json.JSONObject()
                    .put("display_name", source.optString("display_name", query))
                    .put("lat", source.getDouble("lat"))
                    .put("lon", source.getDouble("lon"))
                    .put("type", source.optString("type"))
                    .put("addresstype", source.optString("addresstype"))
                    .put("boundingbox", source.optJSONArray("boundingbox"))
            }
            nominatimFailure = IllegalStateException("No Nominatim results")
        } finally {
            connection.disconnect()
        }
    } catch (error: Throwable) {
        nominatimFailure = error
    }

    var photonConnection: java.net.HttpURLConnection? = null
    try {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val encodedLanguage = java.net.URLEncoder.encode(languageTag.substringBefore('-'), "UTF-8")
        photonConnection = (java.net.URL(
            "https://photon.komoot.io/api/?q=$encodedQuery&limit=5&lang=$encodedLanguage"
        ).openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 12_000
            setRequestProperty("User-Agent", "HorusMaps/1.0 (https://github.com/programingtrainer/HorusMaps)")
            setRequestProperty("Accept", "application/geo+json, application/json")
        }
        val code = photonConnection.responseCode
        if (code !in 200..299) throw IllegalStateException("Photon HTTP $code")
        val root = org.json.JSONObject(photonConnection.inputStream.bufferedReader().use { it.readText() })
        val features = root.optJSONArray("features") ?: org.json.JSONArray()
        if (features.length() == 0) throw IllegalStateException("No matching place found")
        val feature = features.getJSONObject(0)
        val properties = feature.optJSONObject("properties") ?: org.json.JSONObject()
        val coordinates = feature.optJSONObject("geometry")?.optJSONArray("coordinates")
            ?: throw IllegalStateException("Search result has no coordinates")
        if (coordinates.length() < 2) throw IllegalStateException("Search result has invalid coordinates")
        val lat = coordinates.getDouble(1)
        val lon = coordinates.getDouble(0)
        val displayName = listOf(
            properties.optString("name"),
            properties.optString("street"),
            properties.optString("city"),
            properties.optString("state"),
            properties.optString("country")
        ).filter { it.isNotBlank() }.distinct().joinToString(", ").ifBlank { query }
        return org.json.JSONObject()
            .put("display_name", displayName)
            .put("lat", lat)
            .put("lon", lon)
            .put("type", properties.optString("osm_value").ifBlank { properties.optString("type") })
            .put("addresstype", properties.optString("type").ifBlank { properties.optString("osm_value") })
    } catch (photonError: Throwable) {
        throw IllegalStateException(
            "Search providers failed. Nominatim: " + (nominatimFailure?.message ?: "no results") +
                "; Photon: " + (photonError.message ?: "unknown error"),
            photonError
        )
    } finally {
        photonConnection?.disconnect()
    }
}

private fun fetchPlacesFromOverpass(query: String): org.json.JSONObject {
    val endpoints = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://overpass.private.coffee/api/interpreter"
    )
    var lastError: Throwable? = null
    endpoints.forEach { endpoint ->
        var connection: java.net.HttpURLConnection? = null
        try {
            connection = (java.net.URL(endpoint).openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 9_000
                readTimeout = 18_000
                doOutput = true
                setRequestProperty("User-Agent", "HorusMaps/1.0 (https://github.com/programingtrainer/HorusMaps)")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            }
            val payload = "data=" + java.net.URLEncoder.encode(query, "UTF-8")
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299) return org.json.JSONObject(body)
            lastError = IllegalStateException("HTTP $code")
            if (code !in listOf(429, 500, 502, 503, 504)) throw lastError!!
        } catch (error: Throwable) {
            lastError = error
        } finally {
            connection?.disconnect()
        }
    }
    throw IllegalStateException(
        "Nearby places providers are temporarily unavailable",
        lastError
    )
}

private fun getJson(urlText: String, userAgent: String = "HorusMaps/1.0 (https://github.com/programingtrainer/HorusMaps)"): org.json.JSONObject {
    val connection = java.net.URL(urlText).openConnection() as java.net.HttpURLConnection
    connection.connectTimeout = 12000
    connection.readTimeout = 20000
    connection.setRequestProperty("User-Agent", userAgent)
    connection.setRequestProperty("Accept", "application/json")
    try {
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) throw IllegalStateException("HTTP $code")
        return org.json.JSONObject(body)
    } finally {
        connection.disconnect()
    }
}

private fun findNearbyAirports(point: org.maplibre.android.geometry.LatLng): List<AirportCandidate> {
    val query = "[out:json][timeout:20];(node(around:150000,${point.latitude},${point.longitude})[aeroway~\"^(aerodrome|airport)$\"];way(around:150000,${point.latitude},${point.longitude})[aeroway~\"^(aerodrome|airport)$\"];relation(around:150000,${point.latitude},${point.longitude})[aeroway~\"^(aerodrome|airport)$\"];);out center tags 50;"
    val encoded = java.net.URLEncoder.encode(query, "UTF-8")
    val root = getJson("https://overpass-api.de/api/interpreter?data=$encoded")
    val elements = root.optJSONArray("elements") ?: org.json.JSONArray()
    val found = ArrayList<AirportCandidate>()
    for (i in 0 until elements.length()) {
        val item = elements.optJSONObject(i) ?: continue
        val tags = item.optJSONObject("tags") ?: continue
        val iata = (tags.optString("iata").ifBlank { tags.optString("iata:airport") }).uppercase(java.util.Locale.ROOT)
        if (!iata.matches(Regex("[A-Z]{3}"))) continue
        val center = item.optJSONObject("center")
        val lat = if (item.has("lat")) item.optDouble("lat") else center?.optDouble("lat") ?: Double.NaN
        val lon = if (item.has("lon")) item.optDouble("lon") else center?.optDouble("lon") ?: Double.NaN
        if (!lat.isFinite() || !lon.isFinite()) continue
        found.add(AirportCandidate(
            iata = iata,
            name = tags.optString("name").ifBlank { iata },
            latitude = lat,
            longitude = lon,
            accessDistanceMeters = straightLineMeters(point, org.maplibre.android.geometry.LatLng(lat, lon))
        ))
    }
    return found.distinctBy { it.iata }.sortedBy { it.accessDistanceMeters }.take(6)
}

private fun buildFlightEstimate(
    origin: org.maplibre.android.geometry.LatLng,
    destination: org.maplibre.android.geometry.LatLng
): FlightEstimate? {
    // A flight option is not meaningful for nearby streets, districts, or short urban trips.
    if (straightLineMeters(origin, destination) < 100_000.0) return null

    val originAirports = runCatching { findNearbyAirports(origin) }.getOrDefault(emptyList())
    val destinationAirports = runCatching { findNearbyAirports(destination) }.getOrDefault(emptyList())
    var chosenOrigin: AirportCandidate? = originAirports.firstOrNull()
    var chosenDestination: AirportCandidate? = destinationAirports.firstOrNull()
    var scheduledRouteKm: Double? = null
    var hasDirectRouteEvidence = false

    // Prefer a documented nonstop airport pair when the open route dataset contains one.
    for (from in originAirports.take(5)) {
        val airportData = runCatching {
            getJson("https://airportmiles.com/api/v1/airports/${from.iata.lowercase(java.util.Locale.ROOT)}.json")
        }.getOrNull() ?: continue
        val routes = airportData.optJSONArray("routes") ?: continue
        for (to in destinationAirports.take(5)) {
            val matching = (0 until routes.length()).asSequence()
                .mapNotNull { routes.optJSONObject(it) }
                .firstOrNull { it.optJSONObject("to")?.optString("iata") == to.iata }
            if (matching != null) {
                chosenOrigin = from
                chosenDestination = to
                scheduledRouteKm = matching.optDouble("km").takeIf { it > 0.0 }
                hasDirectRouteEvidence = true
                break
            }
        }
        if (scheduledRouteKm != null) break
    }

    val fromAirport = chosenOrigin
    val toAirport = chosenDestination
    // Only offer air travel when both airports are known and the open airport dataset
    // contains evidence for a direct airport pair. This is not proof of a live flight.
    if (fromAirport == null || toAirport == null || !hasDirectRouteEvidence) return null

    val flightDistance = if (scheduledRouteKm != null) scheduledRouteKm!! * 1000.0
        else if (fromAirport != null && toAirport != null) straightLineMeters(
            org.maplibre.android.geometry.LatLng(fromAirport.latitude, fromAirport.longitude),
            org.maplibre.android.geometry.LatLng(toAirport.latitude, toAirport.longitude)
        ) else straightLineMeters(origin, destination)

    // Cruise-speed estimate plus 90 minutes for airport procedures. This is not a timetable.
    val cruiseSeconds = flightDistance / (800.0 * 1000.0 / 3600.0)
    val airportProcedureSeconds = 90.0 * 60.0
    val duration = cruiseSeconds + airportProcedureSeconds
    val points = buildList {
        if (fromAirport != null) add(org.maplibre.geojson.Point.fromLngLat(fromAirport.longitude, fromAirport.latitude))
        else add(org.maplibre.geojson.Point.fromLngLat(origin.longitude, origin.latitude))
        if (toAirport != null) add(org.maplibre.geojson.Point.fromLngLat(toAirport.longitude, toAirport.latitude))
        else add(org.maplibre.geojson.Point.fromLngLat(destination.longitude, destination.latitude))
    }
    val details = localizedUiText(
        "من ${fromAirport.name} (${fromAirport.iata}) إلى ${toAirport.name} (${toAirport.iata}). توجد إشارة لمسار مباشر في بيانات المطارات المفتوحة؛ هذا لا يؤكد تشغيل رحلة حاليًا. الزمن تقديري وليس جدول رحلات.",
        "From ${fromAirport.name} (${fromAirport.iata}) to ${toAirport.name} (${toAirport.iata}). The open airport dataset lists a direct route; this does not confirm a currently operating flight. Duration is an estimate, not a timetable."
    )
    return FlightEstimate(
        distanceMeters = flightDistance,
        durationSeconds = duration,
        geometry = org.maplibre.geojson.LineString.fromLngLats(points),
        details = details
    )
}

internal fun formatRouteDistance(meters: Double): String =
    if (meters < 1000.0) "${meters.toInt()} م" else String.format(java.util.Locale.US, "%.2f كم", meters / 1000.0)

internal fun formatRouteDuration(seconds: Double): String {
    if (!seconds.isFinite() || seconds <= 0.0) return "—"
    val totalMinutes = kotlin.math.ceil(seconds / 60.0).toInt().coerceAtLeast(1)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours} س ${minutes} د" else "${minutes} د"
}

private fun formatDrivingTrafficEstimate(baseSeconds: Double): String {
    val lightTraffic = baseSeconds * 1.15
    val heavyTraffic = baseSeconds * 1.75
    return localizedUiText(
        "مرور خفيف: ${formatRouteDuration(lightTraffic)}  |  زحمة: ${formatRouteDuration(heavyTraffic)}",
        "Light traffic: ${formatRouteDuration(lightTraffic)}  |  Heavy traffic: ${formatRouteDuration(heavyTraffic)}"
    )
}

@Composable
private fun DashboardScreen(accountName: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    var mapViewInstance by remember { mutableStateOf<org.maplibre.android.maps.MapView?>(null) }
    var mapLibreMap by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    var mapStyleLoaded by remember { mutableStateOf(false) }
    val accountEmail = remember { context.getSharedPreferences("horus_session", android.content.Context.MODE_PRIVATE).getString("account_email", "").orEmpty() }
    var searchText by remember { mutableStateOf("") }
    var searchPanelExpanded by remember { mutableStateOf(false) }
    var searching by remember { mutableStateOf(false) }
    var boundaryExpiresAt by remember { mutableStateOf(0L) }
    var continentGeoJson by remember { mutableStateOf<String?>(null) }
    var searchMessage by remember { mutableStateOf("") }
    var darkMap by remember { mutableStateOf(false) }
    var threeDMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("الخريطة") }
    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var locationMessage by remember { mutableStateOf(localizedUiText("نحتاج إذن الموقع لعرض مكانك والأماكن القريبة", "Location permission is needed to show your position and nearby places")) }
    val mapPreferences = remember { context.getSharedPreferences("horus_map_data", android.content.Context.MODE_PRIVATE) }
    var currentLocation by remember {
        mutableStateOf(
            runCatching {
                if (mapPreferences.contains("last_lat") && mapPreferences.contains("last_lon")) {
                    org.maplibre.android.geometry.LatLng(
                        mapPreferences.getFloat("last_lat", 0f).toDouble(),
                        mapPreferences.getFloat("last_lon", 0f).toDouble()
                    )
                } else null
            }.getOrNull()
        )
    }
    var hasFreshLocationFix by remember { mutableStateOf(false) }
    var hasCenteredOnFreshLocation by remember { mutableStateOf(false) }
    var nearbyPlaces by remember { mutableStateOf<List<NearbyPlace>>(emptyList()) }
    var loadingNearby by remember { mutableStateOf(false) }
    var nearbyRequestId by remember { mutableStateOf(0) }
    var selectedPlace by remember { mutableStateOf<NearbyPlace?>(null) }
    var selectedPoiCategory by remember { mutableStateOf("all") }
    var poiSearchMessage by remember { mutableStateOf("") }
    var hasLoadedInitialNearby by remember { mutableStateOf(false) }
    var routeMessage by remember { mutableStateOf("") }
    var loadingRoute by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf<List<NearbyPlace>>(emptyList()) }
    var dashboardVisible by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf<org.maplibre.geojson.LineString?>(null) }
    var routeOriginText by remember { mutableStateOf(localizedUiText("موقعي الحالي", "My current location")) }
    var routeDestinationText by remember { mutableStateOf("") }
    var routeOriginPoint by remember { mutableStateOf<org.maplibre.android.geometry.LatLng?>(null) }
    var routeDestinationPoint by remember { mutableStateOf<org.maplibre.android.geometry.LatLng?>(null) }
    var routeOptions by remember { mutableStateOf<List<RouteOption>>(emptyList()) }
    var selectedRouteMode by remember { mutableStateOf("auto") }
    var activeNavigation by remember { mutableStateOf(false) }
    var navigationStatus by remember { mutableStateOf("") }
    var remainingDistanceMeters by remember { mutableStateOf(0.0) }
    var remainingDurationSeconds by remember { mutableStateOf(0.0) }
    var nextInstruction by remember { mutableStateOf("") }
    var lastRerouteAt by remember { mutableStateOf(0L) }
    val dashboardAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (dashboardVisible) 1f else 0f,
        animationSpec = tween(650, easing = FastOutSlowInEasing), label = "dashboard_fade"
    )
    val dashboardOffset by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (dashboardVisible) 0f else 26f,
        animationSpec = tween(650, easing = FastOutSlowInEasing), label = "dashboard_offset"
    )

    LaunchedEffect(boundaryExpiresAt) {
        val expectedExpiry = boundaryExpiresAt
        if (expectedExpiry > 0L) {
            kotlinx.coroutines.delay(20_000L)
            if (boundaryExpiresAt == expectedExpiry) {
                val source = mapLibreMap?.style?.getSource("horus-country-source")
                    as? org.maplibre.android.style.sources.GeoJsonSource
                source?.setGeoJson(org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf()))
                boundaryExpiresAt = 0L
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapViewInstance) {
        val mapView = mapViewInstance
        var started = false
        var resumed = false
        val observer = mapView?.let { view ->
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> if (!started) {
                        view.onStart()
                        started = true
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        if (!started) {
                            view.onStart()
                            started = true
                        }
                        if (!resumed) {
                            view.onResume()
                            resumed = true
                        }
                    }
                    Lifecycle.Event.ON_PAUSE -> if (resumed) {
                        view.onPause()
                        resumed = false
                    }
                    Lifecycle.Event.ON_STOP -> {
                        if (resumed) {
                            view.onPause()
                            resumed = false
                        }
                        if (started) {
                            view.onStop()
                            started = false
                        }
                    }
                    else -> Unit
                }
            }
        }
        if (observer != null) lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            if (observer != null) lifecycleOwner.lifecycle.removeObserver(observer)
            if (mapView != null) {
                if (resumed) runCatching { mapView.onPause() }
                if (started) runCatching { mapView.onStop() }
                runCatching { mapView.onDestroy() }
            }
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasLocationPermission =
            grants[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationMessage = if (hasLocationPermission) localizedUiText("جارٍ تحديد موقعك...", "Finding your location...") else localizedUiText("إذن الموقع غير متاح. يمكنك تفعيله من إعدادات التطبيق.", "Location permission is unavailable. You can enable it in app settings.")
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("horus_map_data", android.content.Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        favorites = saved.mapNotNull { raw ->
            runCatching {
                val json = org.json.JSONObject(raw)
                NearbyPlace(json.getString("name"), json.getDouble("lat"), json.getDouble("lon"), json.optString("category", localizedUiText("مكان", "Place")))
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

    androidx.compose.runtime.DisposableEffect(hasLocationPermission, activeNavigation, hasFreshLocationFix) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val manager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(location: android.location.Location) {
                    currentLocation = org.maplibre.android.geometry.LatLng(location.latitude, location.longitude)
                    mapPreferences.edit()
                        .putFloat("last_lat", location.latitude.toFloat())
                        .putFloat("last_lon", location.longitude.toFloat())
                        .apply()
                    hasFreshLocationFix = true
                    locationMessage = localizedUiText("تم تحديد موقعك", "Your location was found")
                    if (activeNavigation) {
                        val map = mapLibreMap
                        val bearing = if (location.hasBearing()) location.bearing.toDouble() else map?.cameraPosition?.bearing ?: 0.0
                        map?.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(
                                org.maplibre.android.camera.CameraPosition.Builder()
                                    .target(org.maplibre.android.geometry.LatLng(location.latitude, location.longitude))
                                    .zoom(17.0).tilt(if (threeDMode) 60.0 else 0.0).bearing(bearing).build()
                            ), 700
                        )
                    }
                }
            }
            try {
                val providers = listOf(android.location.LocationManager.NETWORK_PROVIDER, android.location.LocationManager.GPS_PROVIDER)
                providers.forEach { provider ->
                    if (manager.isProviderEnabled(provider)) {
                        manager.getLastKnownLocation(provider)?.let { last ->
                            val ageMillis = System.currentTimeMillis() - last.time
                            if (ageMillis in 0..120_000L && (last.accuracy <= 0f || last.accuracy <= 250f)) {
                                currentLocation = org.maplibre.android.geometry.LatLng(last.latitude, last.longitude)
                                hasFreshLocationFix = true
                            }
                        }
                        // Obtain the first fix promptly; reduce GPS work after that unless turn-by-turn navigation is active.
                        val updateInterval = if (activeNavigation || !hasFreshLocationFix) 1_000L else 5_000L
                        val minimumDistance = if (activeNavigation || !hasFreshLocationFix) 0f else 10f
                        manager.requestLocationUpdates(provider, updateInterval, minimumDistance, listener, android.os.Looper.getMainLooper())
                    }
                }
            } catch (_: SecurityException) {
                locationMessage = localizedUiText("تعذر الوصول إلى الموقع. راجع أذونات التطبيق.", "Could not access your location. Check app permissions.")
            } catch (_: Exception) {
                locationMessage = localizedUiText("خدمة تحديد الموقع غير متاحة حاليًا", "Location services are currently unavailable")
            }
            onDispose { runCatching { manager.removeUpdates(listener) } }
        }
    }

    val styleUrl = if (darkMap) "https://tiles.openfreemap.org/styles/dark"
        else "https://tiles.openfreemap.org/styles/liberty"

    fun loadContinentBoundaries(style: org.maplibre.android.maps.Style) {
        if (continentGeoJson != null) {
            (style.getSource("horus-continent-source") as? org.maplibre.android.style.sources.GeoJsonSource)
                ?.setGeoJson(continentGeoJson!!)
            return
        }
        Thread {
            runCatching {
                val url = java.net.URL(
                    "https://services.arcgis.com/o6oETlrWetREI1A2/ArcGIS/rest/services/Nevis_2D_WebVersion/FeatureServer/29/query?where=1%3D1&outFields=CONTINENT&outSR=4326&returnGeometry=true&f=geojson"
                )
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.setRequestProperty("User-Agent", "HorusMaps/1.0 (https://github.com/programingtrainer/HorusMaps)")
                val root = org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                val features = root.optJSONArray("features")
                    ?: throw IllegalStateException("Continent boundary data is unavailable")
                val language = java.util.Locale.getDefault().language
                for (index in 0 until features.length()) {
                    val feature = features.getJSONObject(index)
                    val properties = feature.optJSONObject("properties") ?: org.json.JSONObject()
                    val englishName = properties.optString("CONTINENT")
                    val localizedName = when (englishName.lowercase(java.util.Locale.ROOT)) {
                        "africa" -> if (language == "ar") "أفريقيا" else "Africa"
                        "antarctica" -> if (language == "ar") "القارة القطبية الجنوبية" else "Antarctica"
                        "asia" -> if (language == "ar") "آسيا" else "Asia"
                        "europe" -> if (language == "ar") "أوروبا" else "Europe"
                        "north america" -> if (language == "ar") "أمريكا الشمالية" else "North America"
                        "south america" -> if (language == "ar") "أمريكا الجنوبية" else "South America"
                        "oceania", "australia" -> if (language == "ar") "أوقيانوسيا" else "Oceania"
                        else -> englishName
                    }
                    properties.put("localized_name", localizedName)
                    feature.put("properties", properties)
                }
                root.toString()
            }.onSuccess { geoJson ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    continentGeoJson = geoJson
                    (mapLibreMap?.style?.getSource("horus-continent-source") as? org.maplibre.android.style.sources.GeoJsonSource)
                        ?.setGeoJson(geoJson)
                }
            }
        }.start()
    }

    fun showContinentSearchResult(continentName: String, geoJson: String): Boolean {
        val root = runCatching { org.json.JSONObject(geoJson) }.getOrNull() ?: return false
        val features = root.optJSONArray("features") ?: return false
        for (index in 0 until features.length()) {
            val feature = features.optJSONObject(index) ?: continue
            val properties = feature.optJSONObject("properties") ?: continue
            if (!properties.optString("CONTINENT").equals(continentName, ignoreCase = true)) continue
            val selectedCollection = org.json.JSONObject()
                .put("type", "FeatureCollection")
                .put("features", org.json.JSONArray().put(feature))
            val boundarySource = mapLibreMap?.style?.getSource("horus-country-source")
                as? org.maplibre.android.style.sources.GeoJsonSource
            boundarySource?.setGeoJson(selectedCollection.toString())
            boundaryExpiresAt = android.os.SystemClock.elapsedRealtime() + 20_000L
            val geometry = feature.optJSONObject("geometry")
            val coordinates = geometry?.optJSONArray("coordinates")
            if (coordinates != null) {
                var south = 90.0
                var north = -90.0
                var west = 180.0
                var east = -180.0
                fun scan(value: Any?) {
                    when (value) {
                        is org.json.JSONArray -> {
                            if (value.length() >= 2 && value.opt(0) is Number && value.opt(1) is Number) {
                                val lon = (value.opt(0) as Number).toDouble()
                                val lat = (value.opt(1) as Number).toDouble()
                                west = minOf(west, lon)
                                east = maxOf(east, lon)
                                south = minOf(south, lat)
                                north = maxOf(north, lat)
                            } else {
                                for (i in 0 until value.length()) scan(value.opt(i))
                            }
                        }
                    }
                }
                scan(coordinates)
                if (south <= north && west <= east) {
                    runCatching {
                        val bounds = org.maplibre.android.geometry.LatLngBounds.Builder()
                            .include(org.maplibre.android.geometry.LatLng(south, west))
                            .include(org.maplibre.android.geometry.LatLng(north, east))
                            .build()
                        mapLibreMap?.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(bounds, 70)
                        )
                    }
                }
            }
            searchMessage = localizedUiText(
                "القارة: ${properties.optString("localized_name", continentName)}",
                "Continent: ${properties.optString("localized_name", continentName)}"
            )
            searching = false
            return true
        }
        return false
    }

    fun updatePointSource(sourceId: String, points: List<NearbyPlace>) {
        val style = mapLibreMap?.style ?: return
        val features = points.map { place ->
            org.maplibre.geojson.Feature.fromGeometry(
                org.maplibre.geojson.Point.fromLngLat(place.longitude, place.latitude)
            ).apply {
                addStringProperty("name", place.name)
                addStringProperty("category", place.category)
                addStringProperty("color", nearbyPlaceColor(place.category))
            }
        }
        val collection = org.maplibre.geojson.FeatureCollection.fromFeatures(features)
        (style.getSource(sourceId) as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(collection)
    }

    fun searchPlace() {
        val query = searchText.trim()
        if (query.isEmpty() || searching) return
        val normalizedContinentQuery = query.lowercase(java.util.Locale.ROOT)
            .replace("continent", "").replace("قارة", "").trim()
        val continentAliases = mapOf(
            "africa" to "Africa", "أفريقيا" to "Africa", "افريقيا" to "Africa", "أفريقا" to "Africa",
            "antarctica" to "Antarctica", "أنتاركتيكا" to "Antarctica", "القارة القطبية الجنوبية" to "Antarctica",
            "asia" to "Asia", "آسيا" to "Asia", "اسيا" to "Asia",
            "europe" to "Europe", "أوروبا" to "Europe", "اوروبا" to "Europe",
            "north america" to "North America", "أمريكا الشمالية" to "North America", "امريكا الشمالية" to "North America",
            "south america" to "South America", "أمريكا الجنوبية" to "South America", "امريكا الجنوبية" to "South America",
            "oceania" to "Oceania", "أوقيانوسيا" to "Oceania", "اوقيانوسيا" to "Oceania",
            "australia continent" to "Oceania", "قارة أستراليا" to "Oceania", "قارة استراليا" to "Oceania"
        )
        val matchedContinent = continentAliases[normalizedContinentQuery]
        if (matchedContinent != null) {
            searching = true
            searchMessage = localizedUiText("جارٍ تحميل حدود القارة...", "Loading continent boundaries...")
            val cached = continentGeoJson
            if (cached != null && showContinentSearchResult(matchedContinent, cached)) return
            Thread {
                runCatching {
                    val url = java.net.URL(
                        "https://services.arcgis.com/o6oETlrWetREI1A2/ArcGIS/rest/services/Nevis_2D_WebVersion/FeatureServer/29/query?where=1%3D1&outFields=CONTINENT&outSR=4326&returnGeometry=true&f=geojson"
                    )
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000
                    connection.setRequestProperty("User-Agent", "HorusMaps/1.0 (https://github.com/programingtrainer/HorusMaps)")
                    val root = org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                    val features = root.optJSONArray("features") ?: throw IllegalStateException("No continent data")
                    val language = java.util.Locale.getDefault().language
                    for (i in 0 until features.length()) {
                        val feature = features.getJSONObject(i)
                        val properties = feature.optJSONObject("properties") ?: org.json.JSONObject()
                        val englishName = properties.optString("CONTINENT")
                        val label = when (englishName.lowercase(java.util.Locale.ROOT)) {
                            "africa" -> if (language == "ar") "أفريقيا" else "Africa"
                            "antarctica" -> if (language == "ar") "القارة القطبية الجنوبية" else "Antarctica"
                            "asia" -> if (language == "ar") "آسيا" else "Asia"
                            "europe" -> if (language == "ar") "أوروبا" else "Europe"
                            "north america" -> if (language == "ar") "أمريكا الشمالية" else "North America"
                            "south america" -> if (language == "ar") "أمريكا الجنوبية" else "South America"
                            "oceania", "australia" -> if (language == "ar") "أوقيانوسيا" else "Oceania"
                            else -> englishName
                        }
                        properties.put("localized_name", label)
                        feature.put("properties", properties)
                    }
                    root.toString()
                }.onSuccess { geoJson ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        continentGeoJson = geoJson
                        (mapLibreMap?.style?.getSource("horus-continent-source") as? org.maplibre.android.style.sources.GeoJsonSource)
                            ?.setGeoJson(geoJson)
                        if (!showContinentSearchResult(matchedContinent, geoJson)) {
                            searching = false
                            searchMessage = localizedUiText("تعذر العثور على حدود القارة", "Could not find continent boundaries")
                        }
                    }
                }.onFailure {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        searching = false
                        searchMessage = localizedUiText("تعذر تحميل حدود القارة", "Could not load continent boundaries")
                    }
                }
            }.start()
            return
        }
        searching = true
        searchMessage = ""
        focusManager.clearFocus()

        // Use the device locale for geocoding results and support free-form
        // queries such as country, governorate, city, district, street and address.
        val deviceLocale = java.util.Locale.getDefault()
        val languageTag = deviceLocale.toLanguageTag()
        Thread {
            runCatching { searchGeocodeWithFallback(query, languageTag) }.onSuccess { result ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    searching = false
                    searchMessage = result.optString("display_name", query)
                    val map = mapLibreMap
                    val addressType = result.optString("addresstype").lowercase(java.util.Locale.ROOT)
                    val osmType = result.optString("type").lowercase(java.util.Locale.ROOT)
                    val isCountry = addressType == "country" || osmType == "country"
                    val isAdministrativeArea = isCountry || addressType in setOf(
                        "state", "province", "region", "county", "state_district",
                        "municipality", "administrative", "district"
                    ) || (osmType == "administrative" && addressType !in setOf("road", "house", "house_number"))

                    val boundarySource = map?.style?.getSource("horus-country-source")
                        as? org.maplibre.android.style.sources.GeoJsonSource
                    val geometry = result.optJSONObject("geojson")
                    val geometryType = geometry?.optString("type").orEmpty()
                    val hasAreaGeometry = geometry != null &&
                        geometryType in setOf("Polygon", "MultiPolygon")

                    if (isAdministrativeArea && hasAreaGeometry) {
                        boundarySource?.setGeoJson(geometry.toString())
                        boundaryExpiresAt = android.os.SystemClock.elapsedRealtime() + 20_000L
                    } else {
                        boundarySource?.setGeoJson(
                            org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf())
                        )
                        boundaryExpiresAt = 0L
                    }

                    val bbox = result.optJSONArray("boundingbox")
                    if (isAdministrativeArea && bbox != null && bbox.length() >= 4) {
                        runCatching {
                            val south = bbox.getString(0).toDouble()
                            val north = bbox.getString(1).toDouble()
                            val west = bbox.getString(2).toDouble()
                            val east = bbox.getString(3).toDouble()
                            val bounds = org.maplibre.android.geometry.LatLngBounds.Builder()
                                .include(org.maplibre.android.geometry.LatLng(south, west))
                                .include(org.maplibre.android.geometry.LatLng(north, east))
                                .build()
                            map?.animateCamera(
                                org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(
                                    bounds, 90
                                )
                            )
                        }.onFailure {
                            map?.animateCamera(
                                org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                    org.maplibre.android.geometry.LatLng(
                                        result.getDouble("lat"), result.getDouble("lon")
                                    ), if (isCountry) 5.0 else 9.0
                                )
                            )
                        }
                    } else {
                        val zoom = when (addressType) {
                            "country" -> 5.0
                            "state", "province", "region", "county", "state_district",
                            "municipality", "administrative" -> 9.5
                            "city", "town", "village", "hamlet" -> 12.5
                            "suburb", "neighbourhood", "quarter", "borough", "district" -> 14.5
                            "road", "street", "pedestrian", "residential", "house",
                            "house_number" -> 17.0
                            else -> if (osmType == "street") 17.0 else 14.5
                        }
                        map?.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                org.maplibre.android.geometry.LatLng(
                                    result.getDouble("lat"), result.getDouble("lon")
                                ), zoom
                            )
                        )
                    }
                }
            }.onFailure { error ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    searching = false
                    searchMessage = error.message ?: if (deviceLocale.language == "ar") {
                        localizedUiText("تعذر البحث عن المكان", "Could not search for this place")
                    } else {
                        "Could not search for this place"
                    }
                }
            }
        }.start()
    }

    fun loadNearbyPlaces(category: String = selectedPoiCategory, searchTerm: String = "") {
        val origin = currentLocation ?: mapLibreMap?.cameraPosition?.target?.takeIf { it.latitude != 0.0 || it.longitude != 0.0 }
        if (origin == null) {
            poiSearchMessage = localizedUiText("اسمح بالوصول إلى موقعك أو حرّك الخريطة إلى منطقتك أولًا", "Allow location access or move the map to your area first")
            if (!hasLocationPermission) permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        // A new request supersedes any older request; slow providers must never lock the UI.
        val requestId = nearbyRequestId + 1
        nearbyRequestId = requestId
        loadingNearby = true
        poiSearchMessage = localizedUiText("جارٍ البحث عن الأماكن القريبة...", "Searching nearby places...")
        Thread {
            runCatching {
                val radiusMeters = if (searchTerm.isBlank()) 5_000 else 8_000
                val lat = origin.latitude
                val lon = origin.longitude
                val categoryFilters = when (category) {
                    "food" -> listOf("""[amenity~"^(restaurant|cafe|fast_food|food_court|ice_cream)$"]""")
                    "fuel" -> listOf("""[amenity="fuel"]""")
                    "health" -> listOf("""[amenity~"^(hospital|clinic|doctors|pharmacy)$"]""")
                    "parks" -> listOf("""[leisure~"^(park|garden|playground|nature_reserve)$"]""")
                    "shopping" -> listOf("""[shop~"^(supermarket|convenience|mall|department_store)$"]""")
                    "stay" -> listOf("""[tourism~"^(hotel|hostel|motel|guest_house)$"]""")
                    "atm" -> listOf("""[amenity~"^(atm|bank)$"]""")
                    "mosques" -> listOf("""[amenity="place_of_worship"][religion="muslim"]""")
                    "stations" -> listOf("""[railway~"^(station|halt|tram_stop)$"]""")
                    else -> listOf(
                        """[amenity~"^(restaurant|cafe|fast_food|hospital|clinic|pharmacy|fuel|atm|bank|place_of_worship)$"]""",
                        """[shop~"^(supermarket|convenience|mall|department_store)$"]""",
                        """[leisure~"^(park|garden|playground|nature_reserve)$"]""",
                        """[tourism~"^(hotel|hostel|motel|guest_house)$"]""",
                        """[railway~"^(station|halt|tram_stop)$"]"""
                    )
                }
                val nameFilter = searchTerm.trim().takeIf { it.isNotBlank() }?.let { raw ->
                    val safe = if (raw.contains("بندة") || raw.contains("بنده")) {
                        "(Panda|بندة|بنده)"
                    } else {
                        raw.trim().split(Regex("\\s+"))
                            .filter { it.isNotBlank() }
                            .joinToString(".*") { word -> word.filter { it.isLetterOrDigit() || it == '-' } }
                    }
                    """[~"^(name(:.*)?|brand(:.*)?|operator(:.*)?)$"~"$safe",i]"""
                } ?: "[name]"
                val selectors = categoryFilters.joinToString("") { filter ->
                    "nwr(around:$radiusMeters,$lat,$lon)$filter$nameFilter;"
                }
                val query = "[out:json][timeout:25];($selectors);out center tags 100;"
                val root = fetchPlacesFromOverpass(query)
                val elements = root.optJSONArray("elements") ?: org.json.JSONArray()
                val places = mutableListOf<Pair<NearbyPlace, Double>>()
                for (index in 0 until elements.length()) {
                    val item = elements.optJSONObject(index) ?: continue
                    val tags = item.optJSONObject("tags") ?: continue
                    val name = tags.optString("name").trim()
                        .ifBlank { tags.optString("name:ar").trim() }
                        .ifBlank { tags.optString("name:en").trim() }
                        .ifBlank { tags.optString("brand").trim() }
                        .ifBlank { tags.optString("brand:ar").trim() }
                        .ifBlank { tags.optString("operator").trim() }
                    if (name.isBlank()) continue
                    val center = item.optJSONObject("center")
                    val placeLat = if (item.has("lat")) item.optDouble("lat") else center?.optDouble("lat") ?: Double.NaN
                    val placeLon = if (item.has("lon")) item.optDouble("lon") else center?.optDouble("lon") ?: Double.NaN
                    if (!placeLat.isFinite() || !placeLon.isFinite()) continue
                    val categoryValue = when {
                        tags.has("amenity") -> tags.optString("amenity")
                        tags.has("shop") -> tags.optString("shop")
                        tags.has("leisure") -> tags.optString("leisure")
                        tags.has("tourism") -> tags.optString("tourism")
                        tags.has("railway") -> "railway"
                        else -> "place"
                    }
                    val place = NearbyPlace(name, placeLat, placeLon, categoryValue)
                    places.add(place to straightLineMeters(origin, org.maplibre.android.geometry.LatLng(placeLat, placeLon)))
                }
                places.distinctBy { "${it.first.name.lowercase(java.util.Locale.ROOT)}:${it.first.latitude}:${it.first.longitude}" }
                    .sortedBy { it.second }
                    .take(60)
                    .map { it.first }
            }.onSuccess { places ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (requestId != nearbyRequestId) return@post
                    nearbyPlaces = places
                    loadingNearby = false
                    poiSearchMessage = if (places.isEmpty()) {
                        localizedUiText("لا توجد نتائج مسجلة ضمن نطاق البحث. جرّب تغيير التصنيف أو تحريك الخريطة.", "No mapped results in this search radius. Try another category or move the map.")
                    } else {
                        localizedUiText("أقرب ${places.size} نتيجة مرتبة حسب المسافة. بيانات الأماكن © OpenStreetMap.", "Showing the ${places.size} closest results, sorted by distance. Place data © OpenStreetMap.")
                    }
                    updatePointSource("horus-nearby-source", places)
                }
            }.onFailure { error ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (requestId != nearbyRequestId) return@post
                    loadingNearby = false
                    poiSearchMessage = localizedUiText(
                        "مصادر بيانات الأماكن مشغولة مؤقتًا. جرّب مرة أخرى بعد لحظات.",
                        "Nearby place providers are temporarily busy. Please try again in a moment."
                    )
                }
            }
        }.start()
    }

    fun searchFromMainBar() {
        val raw = searchText.trim()
        if (raw.isBlank() || searching) return
        val normalized = raw.lowercase(java.util.Locale.ROOT)
        val category = when {
            listOf("سوبر ماركت", "سوبرماركت", "بقالة", "supermarket", "grocery", "panda", "بنده", "بندة", "كارفور", "carrefour", "لولو", "lulu").any { normalized.contains(it) } -> "shopping"
            listOf("مطعم", "مطاعم", "كافيه", "مقهى", "restaurant", "cafe", "coffee").any { normalized.contains(it) } -> "food"
            listOf("بنزين", "محطة وقود", "fuel", "gas station", "petrol").any { normalized.contains(it) } -> "fuel"
            listOf("مستشفى", "مستشفي", "صيدلية", "عيادة", "hospital", "pharmacy", "clinic").any { normalized.contains(it) } -> "health"
            listOf("حديقة", "منتزه", "park", "garden").any { normalized.contains(it) } -> "parks"
            listOf("مسجد", "جامع", "mosque", "masjid").any { normalized.contains(it) } -> "mosques"
            listOf("محطة قطار", "محطه قطار", "قطار", "train station", "railway station").any { normalized.contains(it) } -> "stations"
            listOf("فندق", "hotel", "hostel").any { normalized.contains(it) } -> "stay"
            listOf("صراف", "atm", "بنك", "bank").any { normalized.contains(it) } -> "atm"
            else -> null
        }
        if (category == null) {
            searchPlace()
            return
        }
        val categoryWords = listOf(
            "سوبر ماركت", "سوبرماركت", "بقالة", "supermarket", "grocery",
            "مطعم", "مطاعم", "كافيه", "مقهى", "restaurant", "cafe", "coffee",
            "بنزين", "محطة وقود", "fuel", "gas station", "petrol",
            "مستشفى", "مستشفي", "صيدلية", "عيادة", "hospital", "pharmacy", "clinic",
            "حديقة", "منتزه", "park", "garden", "مسجد", "جامع", "mosque", "masjid",
            "محطة قطار", "محطه قطار", "قطار", "train station", "railway station",
            "فندق", "hotel", "hostel", "صراف", "atm", "بنك", "bank"
        )
        val placeName = categoryWords.fold(raw) { value, word ->
            value.replace(Regex(Regex.escape(word), RegexOption.IGNORE_CASE), " ")
        }.replace(Regex("\\s+"), " ").trim()
        selectedPoiCategory = category
        selectedPlace = null
        routeOptions = emptyList()
        currentRoute = null
        routeMessage = ""
        searchMessage = ""
        focusManager.clearFocus()
        loadNearbyPlaces(category, placeName)
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


    fun geocodeRoutePoint(query: String): RouteGeocodeResult {
        synchronized(nominatimRequestLock) {
            val elapsed = System.currentTimeMillis() - lastNominatimRequestAt
            if (elapsed < 1_000L) Thread.sleep(1_000L - elapsed)
            lastNominatimRequestAt = System.currentTimeMillis()
        }
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val language = java.net.URLEncoder.encode(java.util.Locale.getDefault().toLanguageTag(), "UTF-8")
        val connection = java.net.URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=jsonv2&limit=1&accept-language=$language")
            .openConnection() as java.net.HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 20000
        connection.setRequestProperty("User-Agent", "HorusMaps/1.0 (https://github.com/programingtrainer/HorusMaps)")
        connection.setRequestProperty("Accept-Language", java.util.Locale.getDefault().toLanguageTag())
        val results = org.json.JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
        if (results.length() == 0) throw IllegalStateException(localizedUiText("لم يتم العثور على المكان", "Place not found"))
        val result = results.getJSONObject(0)
        val addressType = result.optString("addresstype").lowercase(java.util.Locale.ROOT)
        val resultType = result.optString("type").lowercase(java.util.Locale.ROOT)
        val flightSuitableTypes = setOf(
            "country", "state", "province", "region", "county", "state_district",
            "municipality", "city", "town", "village"
        )
        val supportsFlight = addressType in flightSuitableTypes ||
            (resultType == "administrative" && addressType !in setOf("road", "house", "house_number", "suburb", "neighbourhood"))
        return RouteGeocodeResult(
            point = org.maplibre.android.geometry.LatLng(result.getDouble("lat"), result.getDouble("lon")),
            supportsFlight = supportsFlight
        )
    }

    fun calculateJourney() {
        if (loadingRoute) return
        if (routeDestinationText.isBlank()) {
            routeMessage = localizedUiText("اكتب مكان الوصول أولًا", "Enter a destination first")
            return
        }
        if (!hasLocationPermission && (routeOriginText.contains("موقعي", true) || routeOriginText.equals("my current location", true) || routeOriginText.equals("current location", true))) {
            routeMessage = localizedUiText("اسمح للتطبيق بالوصول إلى موقعك أو اكتب نقطة البداية", "Allow location access or enter a starting point")
            permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        loadingRoute = true
        activeNavigation = false
        routeOptions = emptyList()
        routeMessage = localizedUiText("جارٍ تحديد النقطتين وحساب الطرق...", "Finding both places and calculating routes...")
        val selectedPlaceDestination = selectedPlace?.takeIf { it.name.equals(routeDestinationText.trim(), ignoreCase = true) }
        Thread {
            runCatching {
                val originResult = if (routeOriginText.contains("موقعي", true) || routeOriginText.equals("current location", true) || routeOriginText.equals("my location", true)) {
                    RouteGeocodeResult(
                        currentLocation ?: throw IllegalStateException(localizedUiText("لم يتم تحديد موقعك بعد", "Your location is not available yet")),
                        supportsFlight = true
                    )
                } else geocodeRoutePoint(routeOriginText)
                val destinationResult = selectedPlaceDestination?.let {
                    RouteGeocodeResult(
                        org.maplibre.android.geometry.LatLng(it.latitude, it.longitude),
                        supportsFlight = false
                    )
                } ?: geocodeRoutePoint(routeDestinationText)
                val origin = originResult.point
                val destination = destinationResult.point
                val options = ArrayList<RouteOption>()
                val failures = ArrayList<String>()
                // Each land mode is attempted for every geocoded destination, including countries and regions.
                try { options.add(requestValhallaRoute(origin, destination, "auto")) }
                catch (_: Exception) { failures.add(localizedUiText("لا يوجد مسار سيارة بري متصل أو تعذر حسابه", "No connected road route was found, or car routing failed")) }
                try { options.add(requestValhallaRoute(origin, destination, "pedestrian")) }
                catch (_: Exception) { failures.add(localizedUiText("لا يوجد مسار مشي بري متصل أو تعذر حسابه", "No connected walking route was found, or pedestrian routing failed")) }

                // Air travel is only offered for intercity-scale trips with a verified airport pair.
                if (originResult.supportsFlight && destinationResult.supportsFlight) {
                    buildFlightEstimate(origin, destination)?.let { flight ->
                    options.add(RouteOption(
                        mode = "flight",
                        distanceMeters = flight.distanceMeters,
                        durationSeconds = flight.durationSeconds,
                        geometry = flight.geometry,
                        isEstimate = true,
                        details = flight.details
                    ))
                    }
                }
                if (options.isEmpty()) throw IllegalStateException(failures.joinToString(" • ").ifBlank { "No routes found" })
                Triple(origin, destination, options)
            }.onSuccess { result ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    routeOriginPoint = result.first
                    routeDestinationPoint = result.second
                    routeOptions = result.third
                    val preferred = result.third.firstOrNull { it.mode == selectedRouteMode } ?: result.third.first()
                    selectedRouteMode = preferred.mode
                    currentRoute = preferred.geometry
                    (mapLibreMap?.style?.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(preferred.geometry)
                    val bounds = org.maplibre.android.geometry.LatLngBounds.Builder().include(result.first).include(result.second).build()
                    runCatching { mapLibreMap?.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(bounds, 100)) }
                    routeMessage = localizedUiText("تم حساب خيارات الرحلة. بعض الوسائل قد لا تتوفر إذا لم يوجد مسار بري متصل أو بيانات نقل.", "Journey options calculated. Some modes may be unavailable when no connected land route or transit data exists.")
                    loadingRoute = false
                    selectedTab = if (selectedPlace != null) "الخريطة" else localizedUiText("المسارات", "Routes")
                }
            }.onFailure { error ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    loadingRoute = false
                    routeMessage = error.message ?: localizedUiText("تعذر حساب الطريق. تحقق من الاتصال وحاول مرة أخرى.", "Could not calculate the route. Check your connection and try again.")
                }
            }
        }.start()
    }

    fun startNavigation() {
        val option = routeOptions.firstOrNull { it.mode == selectedRouteMode } ?: return
        if (option.mode == "flight") {
            navigationStatus = localizedUiText("خط الطيران تقديري ولا يدعم الملاحة الحية.", "The estimated flight line does not support live turn-by-turn navigation.")
            return
        }
        if (!hasLocationPermission) {
            permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
            navigationStatus = localizedUiText("اسمح بالوصول إلى الموقع لبدء الملاحة الحية.", "Allow location access to start live navigation.")
            return
        }
        activeNavigation = true
        currentRoute = option.geometry
        remainingDistanceMeters = option.distanceMeters
        remainingDurationSeconds = option.durationSeconds
        nextInstruction = option.instructions.firstOrNull().orEmpty()
        navigationStatus = localizedUiText("بدأت الملاحة المباشرة", "Live navigation started")
        (mapLibreMap?.style?.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(option.geometry)
        currentLocation?.let { location ->
            mapLibreMap?.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(
                org.maplibre.android.camera.CameraPosition.Builder().target(location).zoom(17.0)
                    .tilt(if (threeDMode) 60.0 else 0.0).build()
            ), 800)
        }
    }

    fun stopNavigation() {
        activeNavigation = false
        currentRoute = null
        routeOptions = emptyList()
        routeOriginPoint = null
        routeDestinationPoint = null
        (mapLibreMap?.style?.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)
            ?.setGeoJson(org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf()))
        nextInstruction = ""
        remainingDistanceMeters = 0.0
        remainingDurationSeconds = 0.0
        navigationStatus = localizedUiText("تم إيقاف الملاحة وإزالة المسار", "Navigation stopped and route cleared")
    }

    fun drawRoute(destination: NearbyPlace) {
        val origin = currentLocation
        if (origin == null || loadingRoute) {
            routeMessage = localizedUiText("فعّل إذن الموقع أولًا لبدء المسار", "Enable location permission before starting a route")
            return
        }
        selectedPlace = destination
        loadingRoute = true
        routeMessage = localizedUiText("جارٍ حساب المسار...", "Calculating route...")
        Thread {
            runCatching {
                val url = java.net.URL(
                    "https://router.project-osrm.org/route/v1/driving/${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?overview=full&geometries=geojson"
                )
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 12000
                connection.readTimeout = 20000
                val root = org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                val routes = root.optJSONArray("routes") ?: throw IllegalStateException(localizedUiText("لم يتم العثور على مسار", "No route was found"))
                if (routes.length() == 0) throw IllegalStateException(localizedUiText("لم يتم العثور على مسار", "No route was found"))
                val route = routes.getJSONObject(0)
                val coordinates = route.getJSONObject("geometry").getJSONArray("coordinates")
                val points = (0 until coordinates.length()).map { index ->
                    val pair = coordinates.getJSONArray(index)
                    org.maplibre.geojson.Point.fromLngLat(pair.getDouble(0), pair.getDouble(1))
                }
                val distanceKm = route.optDouble("distance") / 1000.0
                val durationMin = route.optDouble("duration") / 60.0
                Pair(org.maplibre.geojson.LineString.fromLngLats(points), localizedUiText("المسافة ${"%.1f".format(java.util.Locale.US, distanceKm)} كم • ${durationMin.toInt()} دقيقة تقريبًا", "Distance ${"%.1f".format(java.util.Locale.US, distanceKm)} km • about ${durationMin.toInt()} min"))
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
                    selectedTab = localizedUiText("المسارات", "Routes")
                }
            }.onFailure {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    loadingRoute = false
                    routeMessage = localizedUiText("تعذر حساب المسار الآن. تحقق من الاتصال وحاول مجددًا.", "Could not calculate the route. Check your connection and try again.")
                }
            }
        }.start()
    }

    fun installMapLayers(style: org.maplibre.android.maps.Style) {
        // Only render labels that have a translation for the device language.
        // Do not fall back to the generic OSM name, which may be in another language.
        val language = java.util.Locale.getDefault().language.lowercase(java.util.Locale.ROOT)
        // OpenFreeMap/OpenMapTiles commonly expose both name:<lang> and name_<lang>.
        // Deliberately do not fall back to the local-language "name" field: that
        // would mix languages when a translation is unavailable.
        val localizedNameExpression = org.maplibre.android.style.expressions.Expression.coalesce(
            org.maplibre.android.style.expressions.Expression.get("name:$language"),
            org.maplibre.android.style.expressions.Expression.get("name_$language")
        )
        style.layers.filterIsInstance<org.maplibre.android.style.layers.SymbolLayer>().forEach { symbolLayer ->
            val originalTextExpression = runCatching {
                symbolLayer.getTextField().getExpression()?.toString().orEmpty()
            }.getOrDefault("")
            if (originalTextExpression.contains("name", ignoreCase = true)) {
                symbolLayer.setProperties(
                    org.maplibre.android.style.layers.PropertyFactory.textField(localizedNameExpression)
                )
            }
        }

        // Add a 3D building extrusion layer only when the selected basemap
        // does not already provide one (Liberty may include it by default).
        if (style.layers.none { it is org.maplibre.android.style.layers.FillExtrusionLayer }) {
            val buildingSourceId = when {
                style.getSource("openmaptiles") != null -> "openmaptiles"
                style.getSource("openfreemap") != null -> "openfreemap"
                else -> null
            }
            if (buildingSourceId != null) {
                val buildings3d = org.maplibre.android.style.layers.FillExtrusionLayer(
                    "horus-3d-buildings", buildingSourceId
                ).apply {
                    sourceLayer = "building"
                    minZoom = 15f
                    setFilter(
                        org.maplibre.android.style.expressions.Expression.all(
                            org.maplibre.android.style.expressions.Expression.has("render_height"),
                            org.maplibre.android.style.expressions.Expression.has("render_min_height")
                        )
                    )
                    setProperties(
                        org.maplibre.android.style.layers.PropertyFactory.fillExtrusionColor(
                            android.graphics.Color.parseColor("#C7D0DA")
                        ),
                        org.maplibre.android.style.layers.PropertyFactory.fillExtrusionHeight(
                            org.maplibre.android.style.expressions.Expression.get("render_height")
                        ),
                        org.maplibre.android.style.layers.PropertyFactory.fillExtrusionBase(
                            org.maplibre.android.style.expressions.Expression.get("render_min_height")
                        ),
                        org.maplibre.android.style.layers.PropertyFactory.fillExtrusionOpacity(0.92f)
                    )
                }
                val firstLabelLayer = style.layers
                    .filterIsInstance<org.maplibre.android.style.layers.SymbolLayer>()
                    .firstOrNull()
                if (firstLabelLayer != null) {
                    style.addLayerBelow(buildings3d, firstLabelLayer.id)
                } else {
                    style.addLayer(buildings3d)
                }
            }
        }
        if (style.getSource("horus-continent-source") == null) {
            style.addSource(org.maplibre.android.style.sources.GeoJsonSource(
                "horus-continent-source", org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf())
            ))
            val continentBoundaryLayer = org.maplibre.android.style.layers.LineLayer(
                "horus-continent-boundary-layer", "horus-continent-source"
            ).withProperties(
                org.maplibre.android.style.layers.PropertyFactory.lineColor(android.graphics.Color.parseColor("#667085")),
                org.maplibre.android.style.layers.PropertyFactory.lineWidth(0.9f),
                org.maplibre.android.style.layers.PropertyFactory.lineJoin("round"),
                org.maplibre.android.style.layers.PropertyFactory.lineCap("round")
            )
            continentBoundaryLayer.setMaxZoom(3.8f)
            style.addLayer(continentBoundaryLayer)

            val continentLabelLayer = org.maplibre.android.style.layers.SymbolLayer(
                "horus-continent-label-layer", "horus-continent-source"
            ).withProperties(
                org.maplibre.android.style.layers.PropertyFactory.textField(
                    org.maplibre.android.style.expressions.Expression.get("localized_name")
                ),
                org.maplibre.android.style.layers.PropertyFactory.textSize(12f),
                org.maplibre.android.style.layers.PropertyFactory.textColor(android.graphics.Color.parseColor("#344054")),
                org.maplibre.android.style.layers.PropertyFactory.textHaloColor(android.graphics.Color.WHITE),
                org.maplibre.android.style.layers.PropertyFactory.textHaloWidth(1.4f),
                org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap(false)
            )
            continentLabelLayer.setMaxZoom(3.8f)
            style.addLayer(continentLabelLayer)
        }
        if (continentGeoJson != null) {
            (style.getSource("horus-continent-source") as? org.maplibre.android.style.sources.GeoJsonSource)
                ?.setGeoJson(continentGeoJson!!)
        }
        if (style.getSource("horus-country-source") == null) {
            style.addSource(org.maplibre.android.style.sources.GeoJsonSource(
                "horus-country-source", org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf())
            ))
            style.addLayer(org.maplibre.android.style.layers.LineLayer("horus-country-boundary-layer", "horus-country-source").withProperties(
                org.maplibre.android.style.layers.PropertyFactory.lineColor(android.graphics.Color.parseColor("#F04438")),
                org.maplibre.android.style.layers.PropertyFactory.lineWidth(1.2f),
                org.maplibre.android.style.layers.PropertyFactory.lineJoin("round"),
                org.maplibre.android.style.layers.PropertyFactory.lineCap("round")
            ))
        }
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
                org.maplibre.android.style.layers.PropertyFactory.circleColor(
                    org.maplibre.android.style.expressions.Expression.get("color")
                ),
                org.maplibre.android.style.layers.PropertyFactory.circleRadius(7f),
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
        if (nearbyPlaces.isNotEmpty()) updatePointSource("horus-nearby-source", nearbyPlaces)
        currentRoute?.let { line ->
            (style.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(line)
        }
    }

    LaunchedEffect(currentLocation, mapLibreMap, hasFreshLocationFix, mapStyleLoaded) {
        val location = currentLocation
        val map = mapLibreMap
        if (location != null && map != null && mapStyleLoaded) {
            val point = org.maplibre.geojson.Point.fromLngLat(location.longitude, location.latitude)
            val feature = org.maplibre.geojson.Feature.fromGeometry(point)
            val collection = org.maplibre.geojson.FeatureCollection.fromFeatures(arrayOf(feature))
            (map.style?.getSource("horus-user-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(collection)
            if (hasFreshLocationFix && !hasCenteredOnFreshLocation) {
                map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(location, 15.0), 900)
                hasCenteredOnFreshLocation = true
            }
        }
    }

    LaunchedEffect(currentLocation) {
        if (currentLocation != null && !hasLoadedInitialNearby) {
            hasLoadedInitialNearby = true
            loadNearbyPlaces()
        }
    }

    LaunchedEffect(currentLocation, activeNavigation, routeOptions, selectedRouteMode) {
        val location = currentLocation
        if (activeNavigation && location != null) {
            val option = routeOptions.firstOrNull { it.mode == selectedRouteMode }
            if (option != null) {
                val points = option.geometry.coordinates()
                if (points.size >= 2) {
                    var nearestIndex = 0
                    var nearestDistance = Double.MAX_VALUE
                    points.forEachIndexed { index, point ->
                        val distance = straightLineMeters(location, org.maplibre.android.geometry.LatLng(point.latitude(), point.longitude()))
                        if (distance < nearestDistance) { nearestDistance = distance; nearestIndex = index }
                    }
                    var remaining = 0.0
                    for (i in nearestIndex until points.lastIndex) {
                        remaining += straightLineMeters(
                            org.maplibre.android.geometry.LatLng(points[i].latitude(), points[i].longitude()),
                            org.maplibre.android.geometry.LatLng(points[i + 1].latitude(), points[i + 1].longitude())
                        )
                    }
                    remainingDistanceMeters = remaining
                    val fraction = (remaining / option.distanceMeters.coerceAtLeast(1.0)).coerceIn(0.0, 1.0)
                    remainingDurationSeconds = option.durationSeconds * fraction
                    if (option.instructions.isNotEmpty()) {
                        val progress = (1.0 - fraction).coerceIn(0.0, 1.0)
                        val instructionIndex = (progress * option.instructions.size).toInt()
                            .coerceIn(0, option.instructions.lastIndex)
                        nextInstruction = option.instructions[instructionIndex]
                    }
                    navigationStatus = if (nearestDistance > 90.0)
                        localizedUiText("خرجت عن المسار. جارٍ إعادة الحساب...", "You are off route. Recalculating...")
                    else localizedUiText("أنت على المسار", "You are on route")
                    val destination = routeDestinationPoint
                    val now = System.currentTimeMillis()
                    if (nearestDistance > 90.0 && destination != null && now - lastRerouteAt > 30_000L) {
                        lastRerouteAt = now
                        val mode = selectedRouteMode
                        Thread {
                            runCatching { requestValhallaRoute(location, destination, mode) }
                                .onSuccess { replacement ->
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        routeOptions = routeOptions.map { if (it.mode == mode) replacement else it }
                                        currentRoute = replacement.geometry
                                        (mapLibreMap?.style?.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(replacement.geometry)
                                        navigationStatus = localizedUiText("تم تحديث الطريق تلقائيًا", "Route automatically updated")
                                    }
                                }.onFailure {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        navigationStatus = localizedUiText("تعذرت إعادة حساب الطريق. تحقق من الاتصال.", "Could not recalculate route. Check your connection.")
                                    }
                                }
                        }.start()
                    }
                    if (remaining < 25.0) {
                        activeNavigation = false
                        navigationStatus = localizedUiText("وصلت إلى وجهتك", "You have arrived")
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF3F6F9))) {
        androidx.compose.ui.viewinterop.AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                org.maplibre.android.MapLibre.getInstance(viewContext)
                org.maplibre.android.maps.MapView(viewContext).apply {
                    mapViewInstance = this
                    onCreate(null)
                    getMapAsync { map ->
                        mapLibreMap = map
                        mapStyleLoaded = false
                        map.setStyle(styleUrl) { style ->
                            mapStyleLoaded = true
                            val initialTarget = currentLocation ?: org.maplibre.android.geometry.LatLng(0.0, 0.0)
                            map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                .target(initialTarget).zoom(if (currentLocation != null) 12.5 else 2.0).build()
                            installMapLayers(style)
                            loadContinentBoundaries(style)
                            map.addOnMapClickListener { tappedPoint ->
                                val screenPoint = map.projection.toScreenLocation(tappedPoint)
                                val nativePoiLayers = map.style?.getLayers()
                                    ?.map { it.id }
                                    ?.filter { layerId ->
                                        layerId.contains("poi", ignoreCase = true) ||
                                            layerId.contains("station", ignoreCase = true) ||
                                            layerId.contains("transit", ignoreCase = true)
                                    }.orEmpty()
                                val queryLayers = (listOf("horus-nearby-layer") + nativePoiLayers)
                                    .distinct().toTypedArray()
                                val hit = runCatching {
                                    map.queryRenderedFeatures(screenPoint, *queryLayers).firstOrNull()
                                }.getOrNull()
                                if (hit != null) {
                                    val properties = hit.properties()
                                    val hitName = listOf("name", "name:ar", "name:en", "name:latin")
                                        .firstNotNullOfOrNull { key -> properties?.get(key)?.asString?.takeIf { it.isNotBlank() } }
                                        .orEmpty()
                                    val hitCategory = listOf("category", "amenity", "shop", "leisure", "tourism", "railway", "subclass", "class")
                                        .firstNotNullOfOrNull { key -> properties?.get(key)?.asString?.takeIf { it.isNotBlank() } }
                                        .orEmpty()
                                    selectedPlace = nearbyPlaces.firstOrNull {
                                        it.name == hitName && it.category == hitCategory
                                    } ?: run {
                                        val geometry = hit.geometry() as? org.maplibre.geojson.Point
                                        val point = geometry ?: org.maplibre.geojson.Point.fromLngLat(
                                            tappedPoint.longitude, tappedPoint.latitude
                                        )
                                        NearbyPlace(
                                            hitName.ifBlank { localizedUiText("مكان", "Place") },
                                            point.latitude(),
                                            point.longitude(),
                                            hitCategory
                                        )
                                    }
                                    if (selectedPlace != null) {
                                        routeOptions = emptyList()
                                        routeMessage = ""
                                        map.animateCamera(
                                            org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(tappedPoint, map.cameraPosition.zoom.coerceAtLeast(15.0)),
                                            350
                                        )
                                    }
                                    true
                                } else false
                            }
                            if (threeDMode) {
                                val camera = map.cameraPosition
                                map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                    .target(camera.target)
                                    .zoom(camera.zoom)
                                    .bearing(camera.bearing)
                                    .tilt(60.0)
                                    .build()
                            }
                        }
                    }
                }
            },
            update = { }
        )

        Column(
            Modifier.align(Alignment.TopCenter).fillMaxWidth(0.84f).graphicsLayer {
                alpha = dashboardAlpha
                translationY = -dashboardOffset
            }.padding(top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.98f), shadowElevation = 5.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp).onFocusChanged { state ->
                            if (state.isFocused) searchPanelExpanded = true
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text(localizedUiText("ابحث عن مكان أو خدمة قريبة أو عنوان", "Search nearby places, services, or an address"), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Search, localizedUiText("بحث", "Search"), tint = Blue, modifier = Modifier.size(19.dp)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { searchFromMainBar() })
                    )
                    if (searchPanelExpanded) {
                        IconButton(
                            onClick = {
                                searchPanelExpanded = false
                                searchText = ""
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Outlined.Close, localizedUiText("إغلاق البحث", "Close search"), tint = Color(0xFF667085))
                        }
                    }
                    Spacer(Modifier.width(7.dp))
                    IconButton(
                        onClick = { searchFromMainBar() },
                        enabled = !searching,
                        modifier = Modifier.size(48.dp).background(Blue, RoundedCornerShape(12.dp))
                    ) {
                        Text(if (searching) "…" else "⌕", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            AnimatedVisibility(
                visible = searchPanelExpanded,
                enter = fadeIn(tween(220)) + expandVertically(tween(260, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(160)) + shrinkVertically(tween(190, easing = FastOutSlowInEasing))
            ) {
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.98f), shadowElevation = 7.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp)) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf(
                            Triple("all", localizedUiText("الكل", "All"), nearbyPlaceIcon("all")),
                            Triple("food", localizedUiText("مطاعم وكافيهات", "Food & cafes"), nearbyPlaceIcon("restaurant")),
                            Triple("fuel", localizedUiText("محطات بنزين", "Gas stations"), nearbyPlaceIcon("fuel")),
                            Triple("health", localizedUiText("مستشفيات وصيدليات", "Health"), nearbyPlaceIcon("hospital")),
                            Triple("parks", localizedUiText("منتزهات", "Parks"), nearbyPlaceIcon("park")),
                            Triple("shopping", localizedUiText("تسوق", "Shopping"), nearbyPlaceIcon("supermarket")),
                            Triple("stay", localizedUiText("فنادق", "Hotels"), nearbyPlaceIcon("hotel")),
                            Triple("atm", localizedUiText("بنوك وATM", "Banks & ATMs"), nearbyPlaceIcon("atm")),
                            Triple("mosques", localizedUiText("جوامع", "Mosques"), nearbyPlaceIcon("mosque")),
                            Triple("stations", localizedUiText("محطات قطار", "Train stations"), nearbyPlaceIcon("station"))
                        )
                        categories.forEach { (key, label, icon) ->
                            val selected = selectedPoiCategory == key
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selected) Blue else Color(0xFFF2F4F7),
                                modifier = Modifier.animateContentSize(tween(180)).clickable {
                                    selectedPoiCategory = key
                                    selectedPlace = null
                                    loadNearbyPlaces(key, "")
                                }
                            ) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, tint = if (selected) Color.White else Navy, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text(label, color = if (selected) Color.White else Navy, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    if (poiSearchMessage.isNotBlank()) {
                        Text(poiSearchMessage, color = Color(0xFF667085), fontSize = 9.sp, modifier = Modifier.padding(top = 3.dp), maxLines = 2)
                    }
                    if (nearbyPlaces.isNotEmpty()) {
                        val reference = currentLocation ?: mapLibreMap?.cameraPosition?.target
                        nearbyPlaces.take(3).forEachIndexed { index, place ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(240, delayMillis = index * 60)) +
                                    expandVertically(tween(240, delayMillis = index * 60))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        selectedPlace = place
                                        routeOptions = emptyList()
                                        routeMessage = ""
                                        mapLibreMap?.animateCamera(
                                            org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                                org.maplibre.android.geometry.LatLng(place.latitude, place.longitude), 15.5
                                            ), 420
                                        )
                                    }.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(nearbyPlaceIcon(place.category), contentDescription = null, tint = Blue, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(7.dp))
                                    Text(place.name, color = Navy, fontSize = 10.sp, modifier = Modifier.weight(1f), maxLines = 1)
                                    if (reference != null) {
                                        Text(formatRouteDistance(straightLineMeters(reference, org.maplibre.android.geometry.LatLng(place.latitude, place.longitude))), color = Color(0xFF667085), fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            if (activeNavigation) {
                Surface(shape = RoundedCornerShape(13.dp), color = Color.White.copy(alpha = 0.97f), shadowElevation = 5.dp) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(nextInstruction.ifBlank { navigationStatus }, color = Navy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text("${localizedUiText("متبقي", "Remaining")}: ${formatRouteDistance(remainingDistanceMeters)}  •  ${formatRouteDuration(remainingDurationSeconds)}", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { stopNavigation() }) {
                            Text(localizedUiText("إيقاف", "Stop"), color = Color(0xFFD92D20), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
            if (searchMessage.isNotBlank()) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.94f), shadowElevation = 2.dp) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(searchMessage, color = Color(0xFF667085), fontSize = 10.sp, maxLines = 2)
                        Text(localizedUiText("بيانات البحث © OpenStreetMap contributors", "Search data © OpenStreetMap contributors"), color = Color(0xFF98A2B3), fontSize = 8.sp)
                    }
                }
            }
        }

        // Keep a dedicated compass control clear of the search field.
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 5.dp,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 280.dp)
                .clickable {
                    mapLibreMap?.animateCamera(
                        org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(
                            org.maplibre.android.camera.CameraPosition.Builder()
                                .target(mapLibreMap?.cameraPosition?.target)
                                .zoom(mapLibreMap?.cameraPosition?.zoom ?: 12.0)
                                .tilt(mapLibreMap?.cameraPosition?.tilt ?: 0.0)
                                .bearing(0.0)
                                .build()
                        )
                    )
                }
        ) {
            Icon(Icons.Filled.Explore, localizedUiText("البوصلة", "Compass"), tint = Navy, modifier = Modifier.padding(10.dp).size(22.dp))
        }

        // Request location again from the visible control if permission is not granted.
        if (!hasLocationPermission) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 5.dp,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 280.dp)
                    .clickable {
                        permissionLauncher.launch(arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
            ) {
                Icon(Icons.Filled.MyLocation, localizedUiText("موقعي", "My location"), tint = Blue, modifier = Modifier.padding(11.dp).size(21.dp))
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
                mapStyleLoaded = false
                mapLibreMap?.setStyle(if (darkMap) "https://tiles.openfreemap.org/styles/dark" else "https://tiles.openfreemap.org/styles/liberty") { style ->
                    installMapLayers(style)
                    mapStyleLoaded = true
                }
            }) { Text(if (darkMap) "☀" else "◐", color = Navy, fontSize = 21.sp, modifier = Modifier.padding(13.dp)) }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (threeDMode) Color(0xFFEAF7FC) else Color.White,
                shadowElevation = 5.dp,
                modifier = Modifier.clickable {
                    threeDMode = !threeDMode
                    mapLibreMap?.let { map ->
                        val camera = map.cameraPosition
                        map.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(
                                org.maplibre.android.camera.CameraPosition.Builder()
                                    .target(camera.target)
                                    .zoom(camera.zoom)
                                    .bearing(camera.bearing)
                                    .tilt(if (threeDMode) 60.0 else 0.0)
                                    .build()
                            ),
                            650
                        )
                    }
                }
            ) {
                Text(
                    if (threeDMode) "2D" else "3D",
                    color = if (threeDMode) Blue else Navy,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 13.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = selectedTab == localizedUiText("المحفوظات", "Saved") || selectedTab == localizedUiText("المسارات", "Routes") || selectedTab == localizedUiText("حسابي", "My account"),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 88.dp, start = 15.dp, end = 15.dp),
            enter = fadeIn(tween(220)) +
                expandVertically(tween(300, easing = FastOutSlowInEasing)) +
                androidx.compose.animation.slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it / 4 },
            exit = androidx.compose.animation.fadeOut(tween(150)) +
                androidx.compose.animation.shrinkVertically(tween(190)) +
                androidx.compose.animation.slideOutVertically(tween(190)) { it / 5 }
        ) {
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 5.dp
            ) {
                Column(Modifier.padding(15.dp).heightIn(max = 560.dp).verticalScroll(rememberScrollState())) {
                    Text(when (selectedTab) { localizedUiText("المحفوظات", "Saved") -> localizedUiText("أماكنك المحفوظة", "Your saved places"); localizedUiText("المسارات", "Routes") -> localizedUiText("المسار الحالي", "Current route"); else -> localizedUiText("حسابك في Horus Maps", "Your Horus Maps account") }, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(7.dp))
                    when (selectedTab) {
                        localizedUiText("المحفوظات", "Saved") -> if (favorites.isEmpty()) Text(localizedUiText("لم تحفظ أي مكان بعد.", "You have not saved any places yet."), color = Color(0xFF7B8794), fontSize = 11.sp)
                            else LazyColumn(Modifier.heightIn(max = 145.dp)) {
                                items(favorites) { place ->
                                    Row(Modifier.fillMaxWidth().clickable { drawRoute(place) }.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("♥", color = Blue); Spacer(Modifier.width(9.dp)); Text(place.name, color = Navy, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1); Text(localizedUiText("اذهب", "Go"), color = Blue, fontSize = 11.sp)
                                    }
                                }
                            }
                        localizedUiText("المسارات", "Routes") -> {
                            Text(localizedUiText("خطط رحلتك", "Plan your trip"), color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = routeOriginText, onValueChange = { routeOriginText = it },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                label = { Text(localizedUiText("من", "From")) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        routeOriginText = localizedUiText("موقعي الحالي", "My current location")
                                    }) {
                                        Icon(Icons.Filled.MyLocation, localizedUiText("موقعي الحالي", "My current location"), tint = Blue)
                                    }
                                }
                            )
                            Spacer(Modifier.height(5.dp))
                            OutlinedTextField(
                                value = routeDestinationText, onValueChange = { routeDestinationText = it },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                label = { Text(localizedUiText("إلى", "To")) }
                            )
                            Spacer(Modifier.height(7.dp))
                            Button(onClick = { calculateJourney() }, enabled = !loadingRoute && !activeNavigation,
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text(if (loadingRoute) localizedUiText("جارٍ حساب المسارات...", "Calculating routes...") else localizedUiText("احسب المسافة والوقت", "Calculate distance and time"))
                            }
                            if (routeOptions.isNotEmpty()) {
                                Spacer(Modifier.height(7.dp))
                                routeOptions.forEachIndexed { index, option ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(tween(300, delayMillis = index * 75)) + expandVertically(tween(300, delayMillis = index * 75))
                                    ) {
                                    val label = when (option.mode) {
                                        "auto" -> localizedUiText("سيارة", "Car")
                                        "pedestrian" -> localizedUiText("مشي", "Walk")
                                        else -> localizedUiText("طيران - تقديري", "Flight - estimate")
                                    }
                                    Surface(modifier = Modifier.fillMaxWidth().animateContentSize(tween(280, easing = FastOutSlowInEasing)).clickable {
                                        selectedRouteMode = option.mode
                                        currentRoute = option.geometry
                                        (mapLibreMap?.style?.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(option.geometry)
                                    }.padding(vertical = 2.dp), shape = RoundedCornerShape(10.dp),
                                        color = if (selectedRouteMode == option.mode) Color(0xFFEAF4FF) else Color(0xFFF7F9FC)) {
                                        Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(label, color = Navy, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(formatRouteDistance(option.distanceMeters), color = Blue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                if (option.mode == "auto") {
                                                    Text(formatRouteDuration(option.durationSeconds), color = Color(0xFF667085), fontSize = 11.sp)
                                                    Text(formatDrivingTrafficEstimate(option.durationSeconds), color = Color(0xFF667085), fontSize = 9.sp, textAlign = TextAlign.End)
                                                } else {
                                                    Text(formatRouteDuration(option.durationSeconds), color = Color(0xFF667085), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                    if (option.details.isNotBlank()) {
                                        Text(option.details, color = Color(0xFF667085), fontSize = 9.sp, modifier = Modifier.padding(start = 9.dp, end = 9.dp, bottom = 5.dp))
                                    }
                                    }
                                }
                                Text(localizedUiText("القطار: سيتم عرضه عند توفر مزود بيانات وجداول موثوقة للمنطقة.", "Train: shown when a reliable timetable and data provider are available for the area."),
                                    color = Color(0xFF98A2B3), fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                                Text(localizedUiText("أوقات السيارة والمشي تقديرية من شبكة الطرق ولا تتضمن بيانات ازدحام حيّة. مسارات البر لا يمكن حسابها عبر البحار والمحيطات.", "Car and walking times are estimates from the road network and do not include live traffic. Land routes cannot cross seas or oceans."),
                                    color = Color(0xFF98A2B3), fontSize = 9.sp)
                                if (routeOptions.firstOrNull { it.mode == selectedRouteMode }?.isEstimate == true) {
                                    Text(localizedUiText("زمن الطيران تقديري بسرعة طيران متوسطة مع وقت إجراءات المطار، ولا يمثل حجزًا أو موعد إقلاع فعليًا.", "Flight time is an estimate using average cruise speed and airport procedure time; it is not a booking or actual departure schedule."),
                                        color = Color(0xFFB54708), fontSize = 9.sp)
                                }
                                if (activeNavigation) {
                                    Text(nextInstruction.ifBlank { navigationStatus }, color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    Text("${localizedUiText("المتبقي", "Remaining")}: ${formatRouteDistance(remainingDistanceMeters)} • ${formatRouteDuration(remainingDurationSeconds)}", color = Blue, fontSize = 11.sp)
                                    Text(navigationStatus, color = Color(0xFF667085), fontSize = 10.sp)
                                    OutlinedButton(onClick = { stopNavigation() }, modifier = Modifier.fillMaxWidth()) {
                                        Text(localizedUiText("إيقاف الملاحة", "Stop navigation"))
                                    }
                                } else {
                                    Button(
                                        onClick = { startNavigation() },
                                        enabled = selectedRouteMode != "flight",
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(localizedUiText("ابدأ الملاحة", "Start navigation"))
                                    }
                                }
                            }
                            if (routeMessage.isNotBlank()) Text(routeMessage, color = Color(0xFF667085), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(localizedUiText("مرحبًا ${accountName.ifBlank { "بك" }}", "Welcome ${accountName.ifBlank { "there" }}"), color = Navy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            if (accountEmail.isNotBlank()) Text(accountEmail, color = Color(0xFF667085), fontSize = 11.sp)
                            Text(localizedUiText("إدارة حسابك على هذا الجهاز", "Manage your account on this device"), color = Color(0xFF667085), fontSize = 11.sp)
                            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                                Text(localizedUiText("تسجيل الخروج", "Log out"))
                            }
                        }
                    }
                    if (loadingRoute) Text(localizedUiText("جارٍ حساب الطريق...", "Calculating route..."), color = Blue, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }

        AnimatedVisibility(
            visible = selectedPlace != null,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 88.dp),
            enter = fadeIn(tween(260)) +
                expandVertically(tween(320, easing = FastOutSlowInEasing)) +
                androidx.compose.animation.slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 3 },
            exit = androidx.compose.animation.fadeOut(tween(180)) +
                androidx.compose.animation.shrinkVertically(tween(220)) +
                androidx.compose.animation.slideOutVertically(tween(220)) { it / 4 }
        ) {
            selectedPlace?.let { place ->
                Surface(
                    modifier = Modifier.animateContentSize(tween(320, easing = FastOutSlowInEasing)),
                    shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 14.dp
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(nearbyPlaceIcon(place.category), contentDescription = null, tint = Blue, modifier = Modifier.size(25.dp))
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(place.name, color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(place.category.replace('_', ' '), color = Color(0xFF667085), fontSize = 10.sp)
                            }
                            IconButton(onClick = { selectedPlace = null }) {
                                Icon(Icons.Outlined.Close, localizedUiText("إغلاق", "Close"), tint = Color(0xFF667085))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (routeOptions.isEmpty()) {
                            Button(
                                onClick = {
                                    routeOriginText = localizedUiText("موقعي الحالي", "My current location")
                                    routeDestinationText = place.name
                                    calculateJourney()
                                },
                                enabled = !loadingRoute,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Text(
                                    if (loadingRoute) localizedUiText("جارٍ حساب المسافة...", "Calculating distance...")
                                    else localizedUiText("احسب المسافة", "Calculate distance"),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            val chosenRoute = routeOptions.firstOrNull { it.mode == selectedRouteMode }
                            if (chosenRoute != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(13.dp),
                                    color = Color(0xFFF2F8FF)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(localizedUiText("المسافة والوقت المتوقع", "Distance and estimated time"), color = Color(0xFF667085), fontSize = 10.sp)
                                            Text(
                                                formatRouteDistance(chosenRoute.distanceMeters) + " • " + formatRouteDuration(chosenRoute.durationSeconds),
                                                color = Navy, fontSize = 15.sp, fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            if (selectedRouteMode == "pedestrian") localizedUiText("مشي", "Walk") else localizedUiText("سيارة", "Car"),
                                            color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            routeOptions.filter { it.mode != "flight" }.forEach { option ->
                                val label = if (option.mode == "auto") localizedUiText("سيارة", "Car") else localizedUiText("مشي", "Walk")
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp).clickable {
                                        selectedRouteMode = option.mode
                                        currentRoute = option.geometry
                                        (mapLibreMap?.style?.getSource("horus-route-source") as? org.maplibre.android.style.sources.GeoJsonSource)?.setGeoJson(option.geometry)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedRouteMode == option.mode) Color(0xFFEAF4FF) else Color(0xFFF7F9FC)
                                ) {
                                    Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(label, color = Navy, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                        Text(formatRouteDistance(option.distanceMeters) + " • " + formatRouteDuration(option.durationSeconds), color = Blue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Button(
                                onClick = { startNavigation() },
                                enabled = !loadingRoute && !activeNavigation && selectedRouteMode != "flight",
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(49.dp),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Text(
                                    if (activeNavigation) localizedUiText("الملاحة قيد التشغيل", "Navigation in progress")
                                    else localizedUiText("ابدأ وجهتك", "Start your journey"),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (routeMessage.isNotBlank()) {
                            Text(routeMessage, color = Color(0xFF667085), fontSize = 9.sp, modifier = Modifier.padding(top = 5.dp))
                        }
                    }
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
                listOf("الخريطة", localizedUiText("المسارات", "Routes"), localizedUiText("المحفوظات", "Saved"), localizedUiText("حسابي", "My account")).forEach { item ->
                    Column(Modifier.weight(1f).fillMaxSize().clickable { selectedTab = item }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(when (item) { "الخريطة" -> "⌖"; localizedUiText("المسارات", "Routes") -> "➤"; localizedUiText("المحفوظات", "Saved") -> "♡"; else -> "◉" },
                            color = if (selectedTab == item) Blue else Color(0xFF98A2B3), fontSize = 21.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(when (item) { "الخريطة" -> localizedUiText("الخريطة", "Map"); localizedUiText("المسارات", "Routes") -> localizedUiText("المسارات", "Routes"); localizedUiText("المحفوظات", "Saved") -> localizedUiText("المحفوظات", "Saved"); else -> localizedUiText("حسابي", "Account") }, color = if (selectedTab == item) Blue else Color(0xFF667085), fontSize = 10.sp,
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
                TextButton(onClick = onBack) { Text(localizedUiText("العودة", "Back"), color = Navy, fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.weight(1f))
                Image(painterResource(R.drawable.horus_logo), null, Modifier.size(44.dp))
            }
            Spacer(Modifier.height(38.dp))
            Text(localizedUiText("تسجيل الدخول", "Log in"), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(6.dp))
            Text(localizedUiText("أدخل بريد Horus Maps وكلمة مرورك للمتابعة.", "Enter your Horus Maps email and password to continue."), color = Color(0xFF667085), fontSize = 15.sp)
            Spacer(Modifier.height(30.dp))
            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it.lowercase().filter { char -> char.isLetterOrDigit() || char == '.' || char == '_' } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                suffix = { Text("@horus.com", color = Color(0xFF667085)) },
                label = { Text(localizedUiText("بريد Horus Maps", "Horus Maps email")) }
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
                label = { Text(localizedUiText("كلمة المرور", "Password")) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (showPassword) localizedUiText("إخفاء كلمة المرور", "Hide password") else localizedUiText("إظهار كلمة المرور", "Show password"), tint = Color(0xFF667085))
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
                                } else error = when (result.optString("error")) { "invalid_credentials" -> localizedUiText("البريد أو كلمة المرور غير صحيحة", "Incorrect email or password") else -> localizedUiText("تعذر تسجيل الدخول، حاول مرة أخرى", "Could not log in. Please try again.") }
                            }
                        }.onFailure {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { loading = false; error = localizedUiText("تعذر الاتصال بالخادم", "Could not connect to the server") }
                        }
                    }.start()
                },
                enabled = !loading && handle.length >= 3 && password.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (loading) localizedUiText("جارٍ تسجيل الدخول...", "Logging in...") else localizedUiText("تسجيل الدخول", "Log in"), fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(localizedUiText("ليس لديك حساب؟ ", "Don't have an account? "), color = Color(0xFF667085))
                Text(localizedUiText("إنشاء حساب", "Create account"), color = Blue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onCreateAccount))
            }
        }
    }
}

@Composable
private fun LegalScreen(title: String, onBack: () -> Unit) {
    val isPrivacy = title == localizedUiText("سياسة الخصوصية", "Privacy Policy")
    val sections = if (isPrivacy) {
        listOf(
            localizedUiText("بيانات الحساب", "Account information") to localizedUiText(
                "عند إنشاء الحساب، نعالج الاسم الأول والأخير ورقم الهاتف والبلد ورمز الاتصال ومعرّف Horus المستخدم لتسجيل الدخول، وهو ليس صندوق بريد. لا تُخزَّن كلمة المرور كنص صريح؛ يُنشأ لها تحقق باستخدام PBKDF2 مع قيمة salt فريدة.",
                "When you register, the service processes your first and last name, phone number, country, dial code, and Horus sign-in ID. It is not an email inbox. Passwords are not stored as plain text; a PBKDF2 verifier is stored with a unique salt."
            ),
            localizedUiText("الموقع والبحث", "Location and search") to localizedUiText(
                "تستخدم الخريطة إذن الموقع لتحديد مكانك. تُحفظ آخر إحداثيات والمفضلات على جهازك. عند البحث عن الأماكن أو العناوين أو المسارات، قد تُرسل إحداثياتك أو نص البحث إلى خدمات OpenStreetMap وOverpass وNominatim وPhoton وValhalla وArcGIS لتقديم النتائج.",
                "The map uses location permission to find your position. Your last coordinates and favorites are stored on this device. Place, address, and route searches may send coordinates or search text to OpenStreetMap, Overpass, Nominatim, Photon, Valhalla, and ArcGIS to provide results."
            ),
            localizedUiText("أماكن تخزين البيانات", "Where data is stored") to localizedUiText(
                "تُحفظ بيانات الحساب في قاعدة بيانات Cloudflare D1. ويُحفظ اسم الحساب والبريد وآخر موقع والمفضلات محليًا في تفضيلات التطبيق. لا تتوفر حاليًا ميزة حذف الحساب من داخل التطبيق.",
                "Account records are stored in Cloudflare D1. The account display name, email, last location, and favorites are stored locally in app preferences. In-app account deletion is not currently available."
            ),
            localizedUiText("الخدمات الخارجية", "Third-party services") to localizedUiText(
                "مزودو الخرائط والبحث والمسارات خدمات مستقلة، وقد تخضع البيانات التي تصل إليهم لسياسات الخصوصية الخاصة بهم. لا يتحكم Horus Maps في ممارساتهم.",
                "Map, search, and routing providers are independent services. Data sent to them may be subject to their own privacy policies, which Horus Maps does not control."
            )
        )
    } else {
        listOf(
            localizedUiText("دقة البيانات", "Data accuracy") to localizedUiText(
                "تعتمد الأماكن وأسماؤها على بيانات خرائط مفتوحة قد تكون ناقصة أو قديمة. تحقق من معلومات المكان قبل الاعتماد عليها.",
                "Places and names depend on open map data that may be incomplete or outdated. Verify place information before relying on it."
            ),
            localizedUiText("المسارات وأوقات الرحلات", "Routes and travel times") to localizedUiText(
                "المسارات وأوقات القيادة والمشي تقديرية وتعتمد على مزودي بيانات خارجيين. لا تمثل أوقات ازدحام مباشرة أو مواعيد نقل مؤكدة أو حجزًا.",
                "Routes and driving or walking times are estimates from third-party providers. They are not live traffic predictions, confirmed transit schedules, or bookings."
            ),
            localizedUiText("الاستخدام الآمن", "Safe use") to localizedUiText(
                "استخدم التطبيق مع مراعاة قوانين المرور والسلامة. لا تتعامل مع معلومات الخريطة أو التوجيه باعتبارها بديلًا عن اللافتات الرسمية أو خدمات الطوارئ.",
                "Use the app in accordance with traffic laws and safety requirements. Do not treat map or navigation information as a substitute for official road signs or emergency services."
            )
        )
    }
    Column(
        Modifier.fillMaxSize().background(Color.White).padding(24.dp).verticalScroll(rememberScrollState())
    ) {
        Text(title, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text(
            localizedUiText("معلومات واضحة عن طريقة عمل التطبيق وحدود خدماته.", "A plain-language summary of how the app works and the limits of its services."),
            modifier = Modifier.padding(top = 8.dp, bottom = 18.dp),
            color = Color(0xFF667085),
            fontSize = 12.sp
        )
        sections.forEach { (heading, body) ->
            Text(heading, color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 5.dp))
            Text(body, color = Color(0xFF667085), fontSize = 12.sp, style = androidx.compose.ui.text.TextStyle(lineHeight = 19.sp))
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text(localizedUiText("العودة", "Back"))
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
