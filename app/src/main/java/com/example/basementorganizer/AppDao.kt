package com.example.basementorganizer

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxDao {
    @Query("SELECT * FROM boxes ORDER BY name")
    fun getAllBoxes(): Flow<List<Box>>

    @Insert
    suspend fun insertBox(box: Box): Long

    @Delete
    suspend fun deleteBox(box: Box)

    @Update
    suspend fun updateBox(box: Box)
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE boxId = :boxId ORDER BY name")
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
}