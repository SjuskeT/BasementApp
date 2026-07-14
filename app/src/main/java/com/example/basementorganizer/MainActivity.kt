@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.basementorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow

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
                error = DangerText
            )
            MaterialTheme(colorScheme = colorScheme, typography = AppTypography) {
                Surface(
                    modifier = Modifier.fillMaxSize().pegboardTexture(),
                    color = PageBackground
                ) {
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

@Composable
fun BoxListScreen(viewModel: MainViewModel, onBoxClick: (Box) -> Unit) {
    val boxes by viewModel.boxes.collectAsState()
    val itemCounts by viewModel.itemCounts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalItems = itemCounts.values.sum()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Basement Inventory".uppercase())
                        Text(
                            "${boxes.size} boxes · $totalItems items",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppBarText.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBarBackground,
                    titleContentColor = AppBarText
                )
            )
        },
        floatingActionButton = { TactileFab(onClick = { showAddDialog = true }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search items…") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (searchQuery.isNotBlank()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults) { item ->
                        val itemBox = boxes.find { it.id == item.boxId }
                        ItemRow(
                            item = item,
                            subtitle = "In: ${itemBox?.name ?: "Unknown box"}",
                            onClick = { itemBox?.let { onBoxClick(it) } },
                            trailing = null
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(boxes) { box ->
                        BoxTagCard(
                            box = box,
                            itemCount = itemCounts[box.id] ?: 0,
                            onClick = { onBoxClick(box) }
                        )
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
}

@Composable
fun BoxTagCard(box: Box, itemCount: Int, onClick: () -> Unit) {
    val shape = remember { PunchedTagShape() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = shape, clip = false)
            .clip(shape)
            .background(TagBackground)
            .dashedBorder(TagBorder)
            .clickable { onClick() }
            .padding(start = 34.dp, top = 14.dp, end = 12.dp, bottom = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(box.name.uppercase(), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    box.location,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = TextSecondary
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PageBackground)
                    .border(1.dp, TagBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(itemCount.toString(), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("ITEMS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
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
        topBar = {
            TopAppBar(
                title = { Text(currentBox.name.uppercase()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppBarText)
                    }
                },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit box", tint = AppBarText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBarBackground,
                    titleContentColor = AppBarText
                )
            )
        },
        floatingActionButton = { TactileFab(onClick = { showAddDialog = true }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(
                currentBox.location,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    ItemRow(
                        item = item,
                        subtitle = "Qty: ${item.quantity}",
                        onClick = { itemToEdit = item },
                        trailing = {
                            Row {
                                IconButton(onClick = { itemToMove = item }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Move item", tint = TextSecondary)
                                }
                                IconButton(onClick = { viewModel.deleteItem(item) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete item", tint = DangerText)
                                }
                            }
                        }
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
fun ItemRow(item: Item, subtitle: String, onClick: () -> Unit, trailing: (@Composable () -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${item.name} (x${item.quantity})", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        trailing?.invoke()
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
fun RenameBoxDialog(box: Box, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf(box.name) }
    var location by remember { mutableStateOf(box.location) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Box") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Box name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, location) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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