package com.nexpc.app.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.data.FileItem
import com.nexpc.app.ui.components.GlassCard
import com.nexpc.app.ui.components.HeroCard
import com.nexpc.app.ui.components.BentoTile
import com.nexpc.app.ui.components.SegmentedPill
import com.nexpc.app.ui.components.ActionBubble
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel
import com.nexpc.app.viewmodel.TransferStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SelectedFileInfo(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(viewModel: MainViewModel) {
    var selectedSubTab by remember { mutableIntStateOf(1) } // Default directly to PC Files
    val context = LocalContext.current

    val browseResponse by viewModel.browseResponse.collectAsState()
    val transferHistory by viewModel.transferHistory.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isTransferring by viewModel.isTransferring.collectAsState()
    val transferStatus by viewModel.transferStatus.collectAsState()
    val transferType by viewModel.transferType.collectAsState()
    val transferTitle by viewModel.transferTitle.collectAsState()
    val transferItemName by viewModel.transferItemName.collectAsState()
    val transferProgress by viewModel.transferProgress.collectAsState()
    val transferDetails by viewModel.transferDetails.collectAsState()
    val transferBatchCount by viewModel.transferBatchCount.collectAsState()
    val transferCurrentIndex by viewModel.transferCurrentIndex.collectAsState()

    var selectedFiles by remember { mutableStateOf<List<SelectedFileInfo>>(emptyList()) }

    // Dialog state for File Operations
    var isCreateFolderDialogOpen by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    var renamingItem by remember { mutableStateOf<FileItem?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var deletingItem by remember { mutableStateOf<FileItem?>(null) }

    var movingItem by remember { mutableStateOf<FileItem?>(null) }
    var moveDestPath by remember { mutableStateOf("") }

    var targetUploadDir by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("date") } // "date", "name", "size"

    val displayItems = remember(browseResponse.items, searchQuery, sortOption) {
        val filtered = if (searchQuery.isNotBlank()) {
            browseResponse.items.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
        } else {
            browseResponse.items
        }

        when (sortOption) {
            "name" -> filtered.sortedWith(compareBy({ !it.isDir }, { it.name.lowercase() }))
            "size" -> filtered.sortedWith(compareBy({ !it.isDir }, { -it.sizeBytes }))
            else -> filtered.sortedWith(compareBy({ !it.isDir }, { -it.modified }))
        }
    }

    val multiFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newlySelected = uris.map { uri ->
                var name = "file_${System.currentTimeMillis()}"
                var size = 0L
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) name = cursor.getString(nameIndex)
                        if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                    }
                }
                SelectedFileInfo(uri, name, size)
            }
            if (targetUploadDir != null) {
                viewModel.uploadMultipleFiles(uris, context, targetUploadDir)
                targetUploadDir = null
            } else {
                selectedFiles = (selectedFiles + newlySelected).distinctBy { it.uri.toString() }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (browseResponse.currentPath.isEmpty()) {
            viewModel.browseFiles()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        SegmentedPill(
            options = listOf("Send to PC", "PC Files", "History"),
            selectedIndex = selectedSubTab,
            onSelect = { index ->
                selectedSubTab = index
                if (index == 1 && browseResponse.currentPath.isEmpty()) {
                    viewModel.browseFiles()
                } else if (index == 2) {
                    viewModel.loadTransferHistory()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        when (selectedSubTab) {
            0 -> {
                // Section 1: Send to PC
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CoralBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Upload, contentDescription = null, tint = Coral, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "Upload File ke PC",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Kirim file langsung via Wi-Fi lokal",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                ActionBubble(
                                    icon = Icons.Default.AddPhotoAlternate,
                                    label = if (selectedFiles.isEmpty()) "Pilih File / Foto (Bisa Banyak)" else "+ Tambah File Lagi",
                                    onClick = {
                                        targetUploadDir = null
                                        multiFilePickerLauncher.launch("*/*")
                                    },
                                    color = Coral,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (selectedFiles.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Selection Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${selectedFiles.size} File Dipilih",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            val totalBytes = selectedFiles.sumOf { it.sizeBytes }
                                            Text(
                                                text = "Total ukuran: ${viewModel.formatByteSize(totalBytes)}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        TextButton(
                                            onClick = { selectedFiles = emptyList() },
                                            enabled = !isTransferring
                                        ) {
                                            Text("Hapus Semua", fontSize = 12.sp, color = CrimsonDanger)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // List of selected files
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        selectedFiles.forEachIndexed { idx, fileInfo ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(SurfaceElevated)
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val isImg = fileInfo.name.endsWith(".jpg", true) ||
                                                        fileInfo.name.endsWith(".jpeg", true) ||
                                                        fileInfo.name.endsWith(".png", true) ||
                                                        fileInfo.name.endsWith(".webp", true)
                                                Icon(
                                                    imageVector = if (isImg) Icons.Default.Image else Icons.Default.Description,
                                                    contentDescription = null,
                                                    tint = if (isImg) EmeraldGreen else BlueAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = fileInfo.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = viewModel.formatByteSize(fileInfo.sizeBytes),
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        selectedFiles = selectedFiles.toMutableList().also { it.removeAt(idx) }
                                                    },
                                                    enabled = !isTransferring,
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }

                                     Spacer(modifier = Modifier.height(14.dp))

                                     ActionBubble(
                                         icon = Icons.Default.CloudUpload,
                                         label = if (selectedFiles.size > 1) "Kirim ${selectedFiles.size} File ke PC" else "Kirim ke PC (~/Downloads/NexPC)",
                                         onClick = {
                                             if (!isTransferring) {
                                                 viewModel.uploadMultipleFiles(selectedFiles.map { it.uri }, context)
                                                 selectedFiles = emptyList()
                                             }
                                         },
                                         color = Indigo,
                                         modifier = Modifier.fillMaxWidth()
                                     )
                                 }
                             }
                        }
                    }
                }
            }
            1 -> {
                // Section 2: PC Files Manager
                Column(modifier = Modifier.fillMaxSize()) {
                    // Quick Root Bookmarks Chips
                    if (browseResponse.allowedRoots.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(browseResponse.allowedRoots) { root ->
                                val isSelected = browseResponse.currentPath == root.path
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.browseFiles(root.path) },
                                    label = { Text(root.name, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (root.name.contains("Home")) Icons.Default.Home else Icons.Default.Folder,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BlueSubtle,
                                        selectedLabelColor = BlueAccent,
                                        containerColor = SurfaceCard,
                                        labelColor = TextSecondary
                                    ),
                                    border = BorderStroke(0.5.dp, if (isSelected) BlueAccent else CardBorder)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Navigation Bar & Actions
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = browseResponse.currentPath.replace("/home/wdwi", "~"),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )

                                if (browseResponse.parentPath != null) {
                                    IconButton(
                                        onClick = { viewModel.browseFiles(browseResponse.parentPath) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = BlueAccent, modifier = Modifier.size(18.dp))
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.browseFiles(browseResponse.currentPath) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ActionBubble(
                                    icon = Icons.Default.CreateNewFolder,
                                    label = "+ Folder",
                                    onClick = {
                                        newFolderName = ""
                                        isCreateFolderDialogOpen = true
                                    },
                                    color = Violet,
                                    modifier = Modifier.weight(1f)
                                )

                                ActionBubble(
                                    icon = Icons.Default.Upload,
                                    label = "Upload",
                                    onClick = {
                                        targetUploadDir = browseResponse.currentPath
                                        multiFilePickerLauncher.launch("*/*")
                                    },
                                    color = BlueAccent,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Search Bar & Sorting Chips
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari file di folder ini...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = BlueAccent,
                            unfocusedBorderColor = CardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Urutkan:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)

                        FilterChip(
                            selected = sortOption == "date",
                            onClick = { sortOption = "date" },
                            label = { Text("Terbaru", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp)) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlueSubtle,
                                selectedLabelColor = BlueAccent,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(0.5.dp, if (sortOption == "date") BlueAccent else CardBorder)
                        )

                        FilterChip(
                            selected = sortOption == "name",
                            onClick = { sortOption = "name" },
                            label = { Text("Nama A-Z", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlueSubtle,
                                selectedLabelColor = BlueAccent,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(0.5.dp, if (sortOption == "name") BlueAccent else CardBorder)
                        )

                        FilterChip(
                            selected = sortOption == "size",
                            onClick = { sortOption = "size" },
                            label = { Text("Ukuran", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlueSubtle,
                                selectedLabelColor = BlueAccent,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(0.5.dp, if (sortOption == "size") BlueAccent else CardBorder)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BlueAccent, modifier = Modifier.size(32.dp))
                        }
                    } else if (displayItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Tidak ada file yang cocok dengan \"$searchQuery\"" else "Folder is empty.",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(displayItems) { item ->
                                var menuExpanded by remember { mutableStateOf(false) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceCard)
                                        .clickable {
                                            if (item.isDir) {
                                                viewModel.browseFiles(item.path)
                                            } else {
                                                viewModel.downloadFileToPhone(item, context)
                                            }
                                        }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (item.isDir) AmberSubtle else CoralBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (item.isDir) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                                contentDescription = null,
                                                tint = if (item.isDir) AmberWarning else Coral,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = item.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                            val timeFmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                                            val modTimeStr = remember(item.modified) {
                                                if (item.modified > 0) timeFmt.format(Date(item.modified * 1000L)) else ""
                                            }
                                            val subtitleText = if (item.isDir) {
                                                if (modTimeStr.isNotEmpty()) modTimeStr else "Folder"
                                            } else {
                                                if (modTimeStr.isNotEmpty()) "${item.sizeFormatted} • $modTimeStr" else item.sizeFormatted
                                            }
                                            Text(
                                                text = subtitleText,
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    // Three-dot Options Menu
                                    Box {
                                        IconButton(
                                            onClick = { menuExpanded = true },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }

                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false },
                                            modifier = Modifier.background(SurfaceCard)
                                        ) {
                                            if (!item.isDir) {
                                                DropdownMenuItem(
                                                    text = { Text("Download to Phone", fontSize = 13.sp, color = TextPrimary) },
                                                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp)) },
                                                    onClick = {
                                                        menuExpanded = false
                                                        viewModel.downloadFileToPhone(item, context)
                                                    }
                                                )
                                            }

                                            DropdownMenuItem(
                                                text = { Text("Rename", fontSize = 13.sp, color = TextPrimary) },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    menuExpanded = false
                                                    renamingItem = item
                                                    renameInput = item.name
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = { Text("Move", fontSize = 13.sp, color = TextPrimary) },
                                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    menuExpanded = false
                                                    movingItem = item
                                                    moveDestPath = browseResponse.parentPath ?: "/home/wdwi"
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = { Text("Delete", fontSize = 13.sp, color = CrimsonDanger) },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonDanger, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    menuExpanded = false
                                                    deletingItem = item
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Section 3: Transfer History
                if (transferHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No file transfer history yet.", fontSize = 13.sp, color = TextMuted)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transferHistory) { hist ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (hist.direction == "upload") VioletBg else GreenSubtle),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (hist.direction == "upload") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = if (hist.direction == "upload") Violet else EmeraldGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = hist.filename,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${hist.sizeFormatted} • ${hist.timeFormatted}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (hist.status == "completed") GreenSubtle else CrimsonSubtle)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = hist.status.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hist.status == "completed") EmeraldGreen else CrimsonDanger
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Create Folder Dialog
    if (isCreateFolderDialogOpen) {
        AlertDialog(
            onDismissRequest = { isCreateFolderDialogOpen = false },
            title = { Text("Create New Folder", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueAccent,
                        unfocusedBorderColor = CardBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createNewFolder(newFolderName.trim())
                            isCreateFolderDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isCreateFolderDialogOpen = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 2. Rename Dialog
    if (renamingItem != null) {
        val item = renamingItem!!
        AlertDialog(
            onDismissRequest = { renamingItem = null },
            title = { Text("Rename ${if (item.isDir) "Folder" else "File"}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueAccent,
                        unfocusedBorderColor = CardBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank() && renameInput != item.name) {
                            viewModel.renameFileItem(item.path, renameInput.trim())
                            renamingItem = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Rename", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingItem = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 3. Move Dialog
    if (movingItem != null) {
        val item = movingItem!!
        AlertDialog(
            onDismissRequest = { movingItem = null },
            title = { Text("Move ${item.name}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Destination folder path:", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = moveDestPath,
                        onValueChange = { moveDestPath = it },
                        label = { Text("Target Directory Path") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueAccent,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    Text("Quick Destinations:", fontSize = 11.sp, color = TextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        browseResponse.allowedRoots.take(3).forEach { root ->
                            SuggestionChip(
                                onClick = { moveDestPath = root.path },
                                label = { Text(root.name, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (moveDestPath.isNotBlank()) {
                            viewModel.moveFileItem(item.path, moveDestPath.trim())
                            movingItem = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Move", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { movingItem = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 4. Delete Confirmation Dialog
    if (deletingItem != null) {
        val item = deletingItem!!
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Delete ${if (item.isDir) "Folder" else "File"}?", color = CrimsonDanger, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${item.name}\"?\nThis cannot be undone.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFileItem(item.path)
                        deletingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 5. Real-Time File Transfer Progress Modal (1 - 100%)
    if (isTransferring) {
        val isUpload = transferType == "upload"
        val progressInt = (transferProgress * 100).toInt().coerceIn(0, 100)
        val isFinished = transferStatus == TransferStatus.COMPLETED
        val isCanceled = transferStatus == TransferStatus.CANCELED
        val isFailed = transferStatus == TransferStatus.FAILED
        val isRunning = transferStatus == TransferStatus.TRANSFERRING || transferStatus == TransferStatus.IDLE

        AlertDialog(
            onDismissRequest = {
                if (isRunning) {
                    viewModel.cancelTransfer()
                } else {
                    viewModel.dismissTransferModal()
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (iconBg, iconTint, iconVector) = when {
                        isFinished -> Triple(GreenSubtle, EmeraldGreen, Icons.Default.CheckCircle)
                        isCanceled -> Triple(SurfaceElevated, TextSecondary, Icons.Default.Cancel)
                        isFailed -> Triple(CrimsonSubtle, CrimsonDanger, Icons.Default.ErrorOutline)
                        isUpload -> Triple(BlueSubtle, BlueAccent, Icons.Default.CloudUpload)
                        else -> Triple(GreenSubtle, EmeraldGreen, Icons.Default.CloudDownload)
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = transferTitle.ifEmpty {
                                when {
                                    isFinished -> "Transfer Selesai!"
                                    isCanceled -> "Transfer Dibatalkan"
                                    isFailed -> "Transfer Gagal"
                                    isUpload -> "Mengirim ke PC"
                                    else -> "Mengunduh ke HP"
                                }
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isFinished -> EmeraldGreen
                                isFailed -> CrimsonDanger
                                isCanceled -> TextSecondary
                                else -> TextPrimary
                            }
                        )
                        if (transferBatchCount > 1 && isRunning) {
                            Text(
                                text = "File $transferCurrentIndex dari $transferBatchCount",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        } else if (transferBatchCount > 1 && isFinished) {
                            Text(
                                text = "Total $transferBatchCount file selesai",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    if (transferItemName.isNotBlank() && isRunning) {
                        Text(
                            text = transferItemName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transferDetails.ifEmpty {
                                when {
                                    isFinished -> "Proses selesai 100%"
                                    isCanceled -> "Dibatalkan oleh pengguna"
                                    isFailed -> "Gagal mentransfer file"
                                    else -> "Mentransfer via LAN..."
                                }
                            },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isRunning || isFinished) {
                            Text(
                                text = if (isFinished) "100%" else "$progressInt%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = when {
                                    isFinished -> EmeraldGreen
                                    isUpload -> BlueAccent
                                    else -> EmeraldGreen
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = {
                            when {
                                isFinished -> 1f
                                isFailed || isCanceled -> transferProgress.coerceIn(0f, 1f)
                                else -> transferProgress.coerceIn(0.01f, 1f)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            isFinished -> EmeraldGreen
                            isFailed -> CrimsonDanger
                            isCanceled -> TextMuted
                            isUpload -> BlueAccent
                            else -> EmeraldGreen
                        },
                        trackColor = SurfaceElevated
                    )
                }
            },
            confirmButton = {
                if (isFinished) {
                    Button(
                        onClick = { viewModel.dismissTransferModal() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Selesai", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else if (isCanceled || isFailed) {
                    Button(
                        onClick = { viewModel.dismissTransferModal() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tutup", fontSize = 13.sp, color = TextPrimary)
                    }
                }
            },
            dismissButton = {
                if (isRunning) {
                    OutlinedButton(
                        onClick = { viewModel.cancelTransfer() },
                        border = BorderStroke(1.dp, CrimsonDanger.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDanger)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Batal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(22.dp)
        )
    }
}
