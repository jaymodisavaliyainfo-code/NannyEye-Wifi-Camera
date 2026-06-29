package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDevicesRecordsScreen(
    onBack: () -> Unit,
    viewModel: CameraViewModel
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val tabs = listOf("Record Videos", "Monitor Videos", "Ip Camera Videos", "Snapshots")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    
    val recordVideos by viewModel.recordVideos.observeAsState(emptyList())
    val monitorVideos by viewModel.monitorVideos.observeAsState(emptyList())
    val ipCameraVideos by viewModel.ipCameraVideos.observeAsState(emptyList())
    val snapshots by viewModel.snapshots.observeAsState(emptyList())
    val isLoading by viewModel.isLoadingVideos.observeAsState(false)

    LaunchedEffect(Unit) {
        viewModel.refreshVideoRecords()
    }

    var showDeleteSheet by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    val cardBackground = Color(0xFF1B1F26)
    val textGrey = Color(0xFF9CA3AF)

    Scaffold(
        containerColor = Color(0xFF0E1116),
        topBar = {
            TopAppBar(
                title = { Text("All Devices Records", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF77AEFF),
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = Color(0xFF77AEFF)
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF77AEFF))
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val currentList = when (page) {
                        0 -> recordVideos
                        1 -> monitorVideos
                        2 -> ipCameraVideos
                        else -> snapshots
                    }

                    if (currentList.isEmpty()) {
                        EmptyState(textGrey, cardBackground, density)
                    } else {
                        if (page == 3) {
                            // Snapshots Grid View
                            val columns = 3
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(currentList.chunked(columns)) { rowItems ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        rowItems.forEach { record ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                SnapshotThumbnail(record, Modifier.aspectRatio(1f)) {
                                                    playMedia(context, record.file, "image/*")
                                                }
                                            }
                                        }
                                        repeat(columns - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(dimensionResource(id = R.dimen.spacer_medium))
                            ) {
                                items(currentList) { record ->
                                    VideoRecordItem(record, context, cardBackground, textGrey, density) {
                                        fileToDelete = record.file
                                        showDeleteSheet = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            containerColor = cardBackground,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.card_padding))
                    .padding(bottom = dimensionResource(id = R.dimen.section_spacing))
            ) {
                Text(
                    text = "Recording Options",
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.screen_padding_small))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            fileToDelete?.let { file ->
                                if (file.exists()) {
                                    val deleted = file.delete()
                                    if (deleted) {
                                        android.widget.Toast.makeText(context, "File deleted", android.widget.Toast.LENGTH_SHORT).show()
                                        // Optional: Notify media scanner
                                        android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
                                    } else {
                                        android.widget.Toast.makeText(context, "Delete failed", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            viewModel.refreshVideoRecords()
                            showDeleteSheet = false
                        }
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                        .padding(dimensionResource(id = R.dimen.card_padding)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
                    Text(
                        "Delete Recording",
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun VideoRecordItem(
    record: CameraViewModel.VideoRecord,
    context: android.content.Context,
    cardBackground: Color,
    textGrey: Color,
    density: androidx.compose.ui.unit.Density,
    onMoreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.spacer_small)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.element_spacing)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.thumbnail_size))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                    .background(Color.Black)
                    .clickable {
                        try {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                record.file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "video/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Could not open video", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (record.thumbnail != null) {
                    Image(
                        bitmap = record.thumbnail.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                }
                Icon(
                    Icons.Default.PlayCircle,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.section_spacing)) // 32dp
                )
            }
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.name,
                    color = Color.White,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_micro))) // 4dp
                Text(
                    text = "${record.formattedDate} • ${record.duration}",
                    color = textGrey,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() }
                )
                Text(
                    text = record.size,
                    color = textGrey.copy(alpha = 0.7f),
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() }
                )
            }
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun EmptyState(textGrey: Color, cardBackground: Color, density: androidx.compose.ui.unit.Density) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.screen_padding))
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)))
            .background(cardBackground)
            .padding(dimensionResource(id = R.dimen.section_spacing)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.VideocamOff,
            null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_standard))
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))
        Text(
            "No recordings yet",
            color = Color.White,
            fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() },
            fontWeight = FontWeight.Bold
        )
        Text(
            "Videos will appear here after recording",
            color = textGrey,
            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
            textAlign = TextAlign.Center
        )
    }
}
