package com.example.basementorganizer

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class BoxItemCount(val boxId: Int, val count: Int)

@Dao
interface BoxDao {
    @Query("SELECT * FROM boxes ORDER BY name COLLATE NOCASE")
    fun getAllBoxes(): Flow<List<Box>>

    @Insert
    suspend fun insertBox(box: Box): Long

    @Delete
    suspend fun deleteBox(box: Box)

    @Update
    suspend fun updateBox(box: Box)

    @Query("SELECT * FROM boxes")
    suspend fun getAllBoxesOnce(): List<Box>

    @Insert
    suspend fun insertBoxes(boxes: List<Box>)

    @Query("DELETE FROM boxes")
    suspend fun clearBoxes()
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE boxId = :boxId ORDER BY name COLLATE NOCASE")
    fun getItemsForBox(boxId: Int): Flow<List<Item>>

    @Query("""
        SELECT items.* FROM items
        WHERE items.name LIKE '%' || :query || '%'
    """)
    fun searchItems(query: String): Flow<List<Item>>

    @Insert
    suspend fun insertItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Query("SELECT boxId, COUNT(*) as count FROM items GROUP BY boxId")
    fun getItemCounts(): Flow<List<BoxItemCount>>

    @Query("SELECT * FROM items")
    suspend fun getAllItemsOnce(): List<Item>

    @Insert
    suspend fun insertItems(items: List<Item>)

    @Query("DELETE FROM items")
    suspend fun clearItems()
}