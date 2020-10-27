package com.pavelkrylov.vsafe.logic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pavelkrylov.vsafe.logic.db.entities.DbProduct

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(product: DbProduct)

    @Query("SELECT * FROM DbProduct WHERE id=:id")
    fun getProduct(id: String): DbProduct
}