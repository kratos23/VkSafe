package com.pavelkrylov.vsafe.logic.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DbProduct(
    @PrimaryKey
    val id: String,
    @ColumnInfo
    val json: String
)