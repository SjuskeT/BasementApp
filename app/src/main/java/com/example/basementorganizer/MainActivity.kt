@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.basementorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.MoreVert

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colorScheme = lightColorScheme(
                primary = Accent,
                onPrimary = AccentText,
                background = PageBackground,
                surface = PageBackground,
                onBackground = TextPrimary,
                onSurface = TextPrimary,
                error = DangerIcon
            )
            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = PageBackground) {
                    AppRoot(viewModel)
                }
            }
        }
    }
}

sealed class Screen {
    object BoxList : Screen()
    data class BoxDetail(val box: Box) : Screen()
}

@Composable
fun AppRoot(viewModel: MainViewModel) {
    var screen by remember { mutableStateOf<Screen>(Screen.BoxList) }

    when (val current = screen) {
        is Screen.BoxList -> BoxListScreen(
            viewModel = viewModel,
            onBoxClick = { box -> screen = Screen.BoxDetail(box) }
        )
        is Screen.BoxDetail -> BoxDetailScreen(
            viewModel = viewModel,
            box = current.box,
            onBack = { screen = Screen.BoxList }
        )
    }
}

@Composable
fun TactileFab(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "fabScale")

    FloatingActionButton(
        onClick = onClick,
        containerColor = Accent,
        contentColor = AccentText,
        interactionSource = interactionSource,
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) { Icon(Icons.Default.Add, contentDescription = "Add") }
}

// A simple line-art box icon, drawn rather than relying on an icon library
@Composable
fun BoxIcon(modifier: Modifier = Modifier, tint: Color = IconTint) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.08f, h * 0.22f),
            size = Size(w * 0.84f, h * 0.62f),
            cornerRadius = CornerRadius(w * 0.06f),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.08f, h * 0.4f),
            end = Offset(w * 0.92f, h * 0.4f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.42f, h * 0.22f),
            end = Offset(w * 0.42f, h * 0.4f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.58f, h * 0.22f),
            end = Offset(w * 0.58f, h * 0.4f),
            strokeWidth = strokeWidth
        )
    }
}

// A small illustrated stack of boxes for the header, standing in for a photo
@Composable
fun StackedBoxesIllustration(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy((-10).dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BoxToneLight)
        )
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BoxToneMid)
        )
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BoxToneDark)
        )
    }
}

@Composable
fun ListHeader(boxCount: Int, itemCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                "BASEMENT\nINVENTORY",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "$boxCount boxes · $itemCount items",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        StackedBoxesIllustration()
    }
}

@Composable
fun BoxListRow(box: Box, itemCount: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(IconCircleBackground),
            contentAlignment = Alignment.Center
        ) { BoxIcon() }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(box.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (box.location.isNotBlank()) {
                Text(box.location, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = TextSecondary)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(PillBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                "$itemCount item${if (itemCount != 1) "s" else ""}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PillText
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text("›", fontSize = 20.sp, color = TextSecondary)
    }
}

@Composable
fun SearchResultRow(item: Item, boxName: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(IconCircleBackground),
            contentAlignment = Alignment.Center
        ) { BoxIcon() }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("In: $boxName · Qty: ${item.quantity}", fontSize = 12.sp, fontStyle = FontStyle.Italic, color = TextSecondary)
        }
    }
}

@Composable
fun DetailItemRow(item: Item, onClick: () -> Unit, onMove: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(IconCircleBackground),
            contentAlignment = Alignment.Center
        ) { BoxIcon() }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Qty: ${item.quantity}", fontSize = 12.sp, color = TextSecondary)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MoveBackground)
                .clickable { onMove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Move item", tint = IconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DangerBackground)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete item", tint = DangerIcon, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun BoxListScreen(viewModel: MainViewModel, onBoxClick: (Box) -> Unit) {
    val boxes by viewModel.boxes.collectAsState()
    val itemCounts by viewModel.itemCounts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showImportConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.exportData(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importData(it) } }

    val statusMessage by viewModel.statusMessage.collectAsState()
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }
    val totalItems = itemCounts.values.sum()

    Scaffold(
        floatingActionButton = { TactileFab(onClick = { showAddDialog = true }) },
        containerColor = PageBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ListHeader(boxCount = boxes.size, itemCount = totalItems)
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 12.dp)) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextPrimary)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Export") },
                            onClick = {
                                showMenu = false
                                exportLauncher.launch("basement_inventory_backup.json")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import") },
                            onClick = {
                                showMenu = false
                                showImportConfirm = true
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(CardSheetBackground)
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search items…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = SearchBorder,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        cursorColor = Accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (searchQuery.isNotBlank()) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(searchResults) { item ->
                            val itemBox = boxes.find { it.id == item.boxId }
                            SearchResultRow(
                                item = item,
                                boxName = itemBox?.name ?: "Unknown box",
                                onClick = { itemBox?.let { onBoxClick(it) } }
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(boxes) { box ->
                            BoxListRow(box = box, itemCount = itemCounts[box.id] ?: 0, onClick = { onBoxClick(box) })
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBoxDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, location ->
                viewModel.addBox(name, location)
                showAddDialog = false
            }
        )
    }
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text("Import backup?") },
            text = { Text("This replaces all boxes and items currently in the app with the contents of the backup file. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    importLauncher.launch(arrayOf("*/*"))
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showImportConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun BoxDetailScreen(viewModel: MainViewModel, box: Box, onBack: () -> Unit) {
    val boxes by viewModel.boxes.collectAsState()
    val currentBox = boxes.find { it.id == box.id } ?: box
    val items by viewModel.itemsForBox(box.id).collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var itemToMove by remember { mutableStateOf<Item?>(null) }

    Scaffold(
        floatingActionButton = { TactileFab(onClick = { showAddDialog = true }) },
        containerColor = PageBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    currentBox.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit box", tint = TextPrimary)
                }
            }
            if (currentBox.location.isNotBlank()) {
                Text(
                    currentBox.location,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 44.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items) { item ->
                    DetailItemRow(
                        item = item,
                        onClick = { itemToEdit = item },
                        onMove = { itemToMove = item },
                        onDelete = { viewModel.deleteItem(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty ->
                viewModel.addItem(name, qty, currentBox.id)
                showAddDialog = false
            }
        )
    }

    if (showRenameDialog) {
        RenameBoxDialog(
            box = currentBox,
            onDismiss = { showRenameDialog = false },
            onConfirm = { name, location ->
                viewModel.updateBox(currentBox.copy(name = name, location = location))
                showRenameDialog = false
            },
            onDelete = {
                viewModel.deleteBox(currentBox)
                showRenameDialog = false
                onBack()
            }
        )
    }

    itemToEdit?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onConfirm = { name, qty ->
                viewModel.updateItem(item.copy(name = name, quantity = qty))
                itemToEdit = null
            }
        )
    }

    itemToMove?.let { item ->
        MoveItemDialog(
            item = item,
            boxes = boxes.filter { it.id != currentBox.id },
            onDismiss = { itemToMove = null },
            onMove = { newBoxId ->
                viewModel.moveItem(item, newBoxId)
                itemToMove = null
            }
        )
    }
}

@Composable
fun AddBoxDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Box") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Box name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, location) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantity") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name, quantity.toIntOrNull() ?: 1)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RenameBoxDialog(box: Box, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(box.name) }
    var location by remember { mutableStateOf(box.location) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Box") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Box name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Delete box",
                    color = DangerIcon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { showDeleteConfirm = true }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, location) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete \"${box.name}\"?") },
            text = { Text("This also deletes every item stored in this box. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("Delete", color = DangerIcon) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun EditItemDialog(item: Item, onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf(item.name) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantity") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name, quantity.toIntOrNull() ?: 1)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MoveItemDialog(item: Item, boxes: List<Box>, onDismiss: () -> Unit, onMove: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move \"${item.name}\" to…") },
        text = {
            if (boxes.isEmpty()) {
                Text("No other boxes available.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(boxes) { targetBox ->
                        Text(
                            "${targetBox.name} — ${targetBox.location}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMove(targetBox.id) }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}