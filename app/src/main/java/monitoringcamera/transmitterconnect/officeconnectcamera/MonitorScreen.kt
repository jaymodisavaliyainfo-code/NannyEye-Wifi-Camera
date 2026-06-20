package monitoringcamera.transmitterconnect.officeconnectcamera

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(onBack: () -> Unit, viewModel: CameraViewModel = viewModel()) {
    val density = LocalDensity.current
    val darkBackground = Color(0xFF0E1116)
    val cardBackground = Color(0xFF1B1F26)
    val textGrey = Color(0xFF9CA3AF)
    
    val deviceName by viewModel.deviceName.observeAsState("${Build.MANUFACTURER} ${Build.MODEL}")
    var tempName by remember(deviceName) { mutableStateOf(deviceName) }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.monitor_settings),
                        color = Color.White,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_title).toSp() },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_content_desc),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (tempName.isBlank()) {
                            isError = true
                        } else {
                            isError = false
                            viewModel.updateDeviceName(tempName)
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                )
            )
        },
        containerColor = darkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.screen_padding_small))
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))

            Text(
                text = stringResource(id = R.string.monitor_name),
                color = if (isError) MaterialTheme.colorScheme.error else textGrey,
                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacer_small))
            )

            TextField(
                value = tempName,
                onValueChange = { 
                    tempName = it
                    if (it.isNotBlank()) isError = false
                },
                isError = isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard))),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = cardBackground,
                    unfocusedContainerColor = cardBackground,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF77AEFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorContainerColor = cardBackground,
                    errorIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                supportingText = if (isError) {
                    {
                        Text(
                            text = "Monitor name cannot be empty",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else null
            )

            if (!isError) {
                Text(
                    text = stringResource(id = R.string.monitor_name_description),
                    color = textGrey,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                    modifier = Modifier.padding(
                        top = dimensionResource(id = R.dimen.spacer_small),
                        bottom = dimensionResource(id = R.dimen.screen_padding)
                    )
                )
            } else {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                colors = CardDefaults.cardColors(containerColor = cardBackground)
            ) {
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.platform),
                            color = textGrey,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() }
                        )
                        Text(
                            text = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                            color = Color.White,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.version),
                            color = textGrey,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() }
                        )
                        Text(
                            text = stringResource(id = R.string.version_value),
                            color = Color.White,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
