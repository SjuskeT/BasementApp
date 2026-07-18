package com.example.basementorganizer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.net.Uri
import com.google.gson.Gson

data class ExportPayload(val boxes: List<Box>, val items: List<Item>)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val boxDao = db.boxDao()
    private val itemDao = db.itemDao()

    val boxes: StateFlow<List<Box>> = boxDao.getAllBoxes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<Item>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else itemDao.searchItems(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itemCounts: StateFlow<Map<Int, Int>> = itemDao.getItemCounts()
        .map { list -> list.associate { it.boxId to it.count } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun itemsForBox(boxId: Int): StateFlow<List<Item>> =
        itemDao.getItemsForBox(boxId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBox(name: String, location: String) {
        viewModelScope.launch { boxDao.insertBox(Box(name = name, location = location)) }
    }

    fun deleteBox(box: Box) {
        viewModelScope.launch { boxDao.deleteBox(box) }
    }

    fun addItem(name: String, quantity: Int, boxId: Int) {
        viewModelScope.launch { itemDao.insertItem(Item(name = name, quantity = quantity, boxId = boxId)) }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch { itemDao.deleteItem(item) }
    }

    fun updateBox(box: Box) {
        viewModelScope.launch { boxDao.updateBox(box) }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch { itemDao.updateItem(item) }
    }

    fun moveItem(item: Item, newBoxId: Int) {
        viewModelScope.launch { itemDao.updateItem(item.copy(boxId = newBoxId)) }
    }

    private val gson = Gson()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val payload = ExportPayload(boxDao.getAllBoxesOnce(), itemDao.getAllItemsOnce())
                val json = gson.toJson(payload)
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(json.toByteArray())
                }
                _statusMessage.value = "Export complete"
            } catch (e: Exception) {
                _statusMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().readText()
                } ?: throw Exception("Could not read file")
                val payload = gson.fromJson(json, ExportPayload::class.java)
                itemDao.clearItems()
                boxDao.clearBoxes()
                boxDao.insertBoxes(payload.boxes)
                itemDao.insertItems(payload.items)
                _statusMessage.value = "Import complete"
            } catch (e: Exception) {
                _statusMessage.value = "Import failed: ${e.message}"
            }
        }
    }
}