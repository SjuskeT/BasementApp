package com.example.basementorganizer

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Box::class,
            parentColumns = ["id"],
            childColumns = ["boxId"],
            onDelete = ForeignKey.CASCADE // deleting a box deletes its items too
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Int = 1,
    val boxId: Int // links this item to a Box.id
)