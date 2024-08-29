package com.example.color_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "color_table")
data class ColorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hexColor: String,
    val date: String,
    val sync: Boolean
)
