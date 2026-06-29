package monitoringcamera.transmitterconnect.officeconnectcamera

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.webrtc.SurfaceViewRenderer
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorWallScreen(
    navController: NavController,
    viewModel: MonitorWallViewModel = viewModel()
) {
    val savedDevices by viewModel.savedDevices.collectAsState()
    val onlineMonitors by viewModel.onlineMonitors.observeAsState(emptyMap())
    val gridSize by viewModel.gridSize.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterOnlineOnly by viewModel.filterOnlineOnly.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    val filteredDevices = remember(savedDevices, searchQuery, filterOnlineOnly, sortBy, onlineMonitors) {
        savedDevices.filter { device ->
            val matchesSearch = device.name.contains(searchQuery, ignoreCase = true)
            val isOnline = onlineMonitors.containsKey(device.deviceId)
            val matchesFilter = if (filterOnlineOnly) isOnline else true
            matchesSearch && matchesFilter
        }.sortedWith { a, b ->
            if (sortBy == "Name") a.name.compareTo(b.name, ignoreCase = true)
            else b.timestamp.compareTo(a.timestamp)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitor Wall", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    SortSelector(sortBy, onSortSelected = { viewModel.setSortBy(it) })
                    GridSizeSelector(gridSize, onSizeSelected = { viewModel.setGridSize(it) })
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1116))
            )
        },
        containerColor = Color(0xFF0E1116)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search and Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search cameras...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF77AEFF),
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filterOnlineOnly,
                    onClick = { viewModel.toggleFilterOnlineOnly() },
                    label = { Text("Online Only") },
                    leadingIcon = if (filterOnlineOnly) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1B263B),
                        selectedLabelColor = Color(0xFF77AEFF),
                        labelColor = Color.Gray
                    )
                )
            }

            // Grid
            val columns = when (gridSize) {
                1 -> 1
                2 -> 2
                4 -> 2
                6 -> 2
                9 -> 3
                12 -> 3
                16 -> 4
                else -> 2
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDevices, key = { it.deviceId }) { device ->
                    val onlineInfo = onlineMonitors[device.deviceId]
                    val isOnline = onlineInfo != null
                    val currentSessionId = onlineInfo?.sessionId ?: device.sessionId

                    CameraTile(
                        device = device,
                        isOnline = isOnline,
                        sessionId = currentSessionId,
                        viewModel = viewModel,
                        onClick = {
                            if (isOnline) {
                                navController.navigate("viewer/${URLEncoder.encode(currentSessionId, "UTF-8")}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CameraTile(
    device: PairedDevice,
    isOnline: Boolean,
    sessionId: String,
    viewModel: MonitorWallViewModel,
    onClick: () -> Unit
) {
    val connectionState by viewModel.getSessionConnectionState(sessionId).observeAsState(false)
    val context = LocalContext.current
    var renderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    // Connect logic based on visibility and online status
    LaunchedEffect(isOnline, sessionId, renderer) {
        if (isOnline && renderer != null) {
            viewModel.connectCamera(sessionId, renderer!!)
        }
    }

    DisposableEffect(sessionId) {
        onDispose {
            // Keep connection alive to support concurrent viewing and seamless navigation
            // viewModel.disconnectCamera(sessionId)
        }
    }

    Card(
        modifier = Modifier
            .aspectRatio(16f / 9f)
            .clickable(enabled = isOnline) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isOnline) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            viewModel.initRenderer(this)
                            renderer = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlays
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // LIVE Badge
                    Surface(
                        color = if (connectionState) Color.Red else Color.Gray,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = if (connectionState) "LIVE" else "CONNECTING",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // Device Name
                    Text(
                        text = device.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            } else {
                // Offline State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.VideocamOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("OFFLINE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(device.name, color = Color.Gray.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SortSelector(currentSort: String, onSortSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Name", "Last Connected")

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort By", tint = Color.White)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1B1F26))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(option, color = if (option == currentSort) Color(0xFF77AEFF) else Color.White)
                            if (option == currentSort) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF77AEFF), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    onClick = {
                        onSortSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun GridSizeSelector(currentSize: Int, onSizeSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val sizes = listOf(1, 2, 4, 6, 9, 12, 16)

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.GridView, contentDescription = "Grid Size", tint = Color.White)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1B1F26))
        ) {
            sizes.forEach { size ->
                DropdownMenuItem(
                    text = { Text("$size Camera${if (size > 1) "s" else ""}", color = Color.White) },
                    onClick = {
                        onSizeSelected(size)
                        expanded = false
                    }
                )
            }
        }
    }
}
