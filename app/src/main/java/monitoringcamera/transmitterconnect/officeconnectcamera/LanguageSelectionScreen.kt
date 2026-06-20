package monitoringcamera.transmitterconnect.officeconnectcamera

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val darkBackground = Color(0xFF0E1116)
    val cardBackground = Color(0xFF1B1F26)
    val textGrey = Color(0xFF9CA3AF)
    val blueAccent = Color(0xFF77AEFF)

    val currentLangCode = LocaleHelper.getLanguage(context) ?: "en"
    var selectedLangCode by remember { mutableStateOf(currentLangCode) }
    var searchQuery by remember { mutableStateOf("") }

    val languages = listOf(
        LanguageItem("en", "English", "DEFAULT SYSTEM", "🇺🇸"),
        LanguageItem("es", "Español", "IBERO-AMERICAN", "🇪🇸"),
        LanguageItem("ar", "العربية", "ARABIC", "🇸🇦"),
        LanguageItem("bn", "বাংলা", "BENGALI", "🇧🇩"),
        LanguageItem("de", "Deutsch", "GERMAN", "🇩🇪"),
        LanguageItem("fr", "Français", "FRENCH", "🇫🇷"),
        LanguageItem("hi", "हिन्दी", "HINDI", "🇮🇳"),
        LanguageItem("it", "Italiano", "ITALIAN", "🇮🇹"),
        LanguageItem("ja", "日本語", "JAPANESE", "🇯🇵"),
        LanguageItem("ko", "한국어", "KOREAN", "🇰🇷"),
        LanguageItem("pt", "Português", "PORTUGUESE (BRAZIL)", "🇧🇷"),
        LanguageItem("ru", "Русский", "RUSSIAN", "🇷🇺"),
        LanguageItem("zh", "简体中文", "CHINESE (SIMPLIFIED)", "🇨🇳"),
        LanguageItem("tr", "Türkçe", "TURKISH", "🇹🇷"),
        LanguageItem("gu", "ગુજરાતી", "GUJARATI", "🇮🇳"),
        LanguageItem("nl", "Nederlands", "DUTCH", "🇳🇱"),
        LanguageItem("pl", "Polski", "POLISH", "🇵🇱"),
        LanguageItem("sv", "Svenska", "SWEDISH", "🇸🇪"),
        LanguageItem("no", "Norsk", "NORWEGIAN", "🇳🇴"),
        LanguageItem("da", "Dansk", "DANISH", "🇩🇰"),
        LanguageItem("fi", "Suomi", "FINNISH", "🇫🇮"),
        LanguageItem("el", "Ελληνικά", "GREEK", "🇬🇷"),
        LanguageItem("iw", "עברית", "HEBREW", "🇮🇱"),
        LanguageItem("th", "ไทย", "THAI", "🇹🇭"),
        LanguageItem("vi", "Tiếng Việt", "VIETNAMESE", "🇻🇳"),
        LanguageItem("id", "Bahasa Indonesia", "INDONESIAN", "🇮🇩"),
        LanguageItem("ms", "Bahasa Melayu", "MALAY", "🇲🇾"),
        LanguageItem("cs", "Čeština", "CZECH", "🇨🇿"),
        LanguageItem("hu", "Magyar", "HUNGARIAN", "🇭🇺"),
        LanguageItem("ro", "Română", "ROMANIAN", "🇷🇴"),
        LanguageItem("sk", "Slovenčina", "SLOVAK", "🇸🇰"),
        LanguageItem("uk", "Українська", "UKRAINIAN", "🇺🇦"),
        LanguageItem("zh-rTW", "繁體中文", "CHINESE (TRADITIONAL)", "🇹🇼")
    ).distinctBy { it.code }

    val filteredLanguages = if (searchQuery.isEmpty()) {
        languages
    } else {
        languages.filter {
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Select Language",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "NannyEye: WiFi Camera &amp; Monitor CORE SETTINGS",
                            fontSize = 10.sp,
                            color = textGrey,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        containerColor = darkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Search system language...", color = textGrey, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textGrey) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBackground,
                    unfocusedContainerColor = cardBackground,
                    focusedBorderColor = Color.White.copy(alpha = 0.1f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    item {
                        Text(
                            "RECOMMENDED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textGrey,
                            letterSpacing = 1.sp
                        )
                    }

                    val recommended = languages.find { it.code == currentLangCode } ?: languages[0]
                    item {
                        LanguageCard(
                            item = recommended,
                            isSelected = selectedLangCode == recommended.code,
                            onClick = { selectedLangCode = recommended.code },
                            cardBackground = cardBackground,
                            textGrey = textGrey,
                            blueAccent = blueAccent
                        )
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    item {
                        Text(
                            "ALL LANGUAGES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textGrey,
                            letterSpacing = 1.sp
                        )
                    }
                }

                items(filteredLanguages) { item ->
                    LanguageCard(
                        item = item,
                        isSelected = selectedLangCode == item.code,
                        onClick = { selectedLangCode = item.code },
                        cardBackground = cardBackground,
                        textGrey = textGrey,
                        blueAccent = blueAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    LocaleHelper.setLocale(context, selectedLangCode)
                    navController.popBackStack()
                    activity?.recreate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "CONFIRM SELECTION",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "APPLYING CHANGES WILL RESTART THE SECURITY\nINTERFACE",
                color = textGrey,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 14.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LanguageCard(
    item: LanguageItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardBackground: Color,
    textGrey: Color,
    blueAccent: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isSelected) blueAccent.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.flag, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    item.subtitle,
                    color = textGrey,
                    fontSize = 11.sp
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFE67E22), // Match the orange/peach color from image
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

data class LanguageItem(
    val code: String,
    val name: String,
    val subtitle: String,
    val flag: String
)
