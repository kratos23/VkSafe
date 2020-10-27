package com.pavelkrylov.vsafe.logic.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pavelkrylov.vsafe.logic.db.dao.ProductDao
import com.pavelkrylov.vsafe.logic.db.entities.DbProduct

@Database(entities = [DbProduct::class], version = 1)
abstract class RoomDb : RoomDatabase() {
    abstract fun productsDao(): ProductDao
}