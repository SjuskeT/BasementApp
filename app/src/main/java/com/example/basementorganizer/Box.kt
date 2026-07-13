package com.example.basementorganizer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boxes")
data class Box(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,       // e.g. "Christmas decorations"
    val location: String    // e.g. "Shelf 3, left side"
)