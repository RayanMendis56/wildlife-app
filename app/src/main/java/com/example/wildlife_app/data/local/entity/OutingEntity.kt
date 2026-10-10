package com.example.wildlife_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outings")
data class OutingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: Long                     // epoch millis
)