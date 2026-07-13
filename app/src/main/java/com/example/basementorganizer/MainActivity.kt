@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.basementorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(viewModel)
                }
            }
        }
    }
}

// Simple screen state: either the box list, or a specific box's detail view
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
fun BoxListScreen(viewModel: MainViewModel, onBoxClick: (Box) -> Unit) {
    val boxes by viewModel.boxes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Basement Boxes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Text("+") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search items…") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (searchQuery.isNotBlank()) {
                Text("Results:", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(searchResults) { item ->
                        val boxName = boxes.find { it.id == item.boxId }?.name ?: "Unknown box"
                        ListItem(
                            headlineContent = { Text("${item.name} (x${item.quantity})") },
                            supportingContent = { Text("In: $boxName") }
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(boxes) { box ->
                        ListItem(
                            headlineContent = { Text(box.name) },
                            supportingContent = { Text(box.location) },
                            modifier = Modifier.clickable { onBoxClick(box) } // requires foundation import
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
fun BoxDetailScreen(viewModel: MainViewModel, box: Box, onBack: () -> Unit) {
    val items by viewModel.itemsForBox(box.id).collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(box.name) },
                navigationIcon = { TextButton(onClick = onBack) { Text("< Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Text("+") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(box.location, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(items) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text("Qty: ${item.quantity}") },
                        trailingContent = {
                            TextButton(onClick = { viewModel.deleteItem(item) }) { Text("Delete") }
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
                viewModel.addItem(name, qty, box.id)
                showAddDialog = false
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