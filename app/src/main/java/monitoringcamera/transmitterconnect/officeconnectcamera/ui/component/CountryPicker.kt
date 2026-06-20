package monitoringcamera.transmitterconnect.officeconnectcamera.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*

data class Country(
    val code: String,
    val dialCode: String,
    val name: String,
    val flag: String
)

fun getCountries(): List<Country> {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return Locale.getISOCountries().map { isoCode ->
        val dialCode = phoneUtil.getCountryCodeForRegion(isoCode)
        val name = Locale("", isoCode).displayCountry
        val flag = isoCodeToFlag(isoCode)
        Country(isoCode, "+$dialCode", name, flag)
    }.filter { it.dialCode != "+0" }.sortedBy { it.name }
}

fun isoCodeToFlag(countryCode: String): String {
    return countryCode
        .uppercase()
        .map { char -> Character.codePointAt(char.toString(), 0) - 0x41 + 0x1F1E6 }
        .map { Character.toChars(it) }
        .joinToString("") { String(it) }
}

@Composable
fun CountryPicker(
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier
        .clickable { showDialog = true }
        .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = selectedCountry.flag, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = selectedCountry.dialCode, color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "▼", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        }
    }

    if (showDialog) {
        CountryPickerDialog(
            onDismissRequest = { showDialog = false },
            onCountrySelected = {
                onCountrySelected(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun CountryPickerDialog(
    onDismissRequest: () -> Unit,
    onCountrySelected: (Country) -> Unit
) {
    val countries = remember { getCountries() }
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isEmpty()) countries
        else countries.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.dialCode.contains(searchQuery)
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF111827)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Country",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country", color = Color.White.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF070A0F),
                        unfocusedContainerColor = Color(0xFF070A0F),
                        focusedBorderColor = Color(0xFF3D9BE9),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(filteredCountries) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountrySelected(country) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = country.flag, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = country.name,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp
                            )
                            Text(
                                text = country.dialCode,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 16.sp
                            )
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}
