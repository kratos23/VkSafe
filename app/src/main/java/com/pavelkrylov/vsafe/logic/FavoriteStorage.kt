package com.pavelkrylov.vsafe.logic

import android.content.ContentValues
import com.pavelkrylov.vsafe.logic.db.DbHelper

object FavoriteStorage {
    fun addFavorite(groupId: Long, productId: Long) {
        val db = DbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("groupId", groupId)
            put("productId", productId)
        }
        db.insert("favorites", null, cv)
    }

    fun removeFavorite(groupId: Long, productId: Long) {
        val db = DbHelper.writableDatabase
        db.execSQL("DELETE FROM favorites WHERE groupId=$groupId AND productId=$productId")
    }

    fun checkFavorite(groupId: Long, productId: Long): Boolean {
        val db = DbHelper.readableDatabase
        val cursor = db.query(
            "favorites",
            null,
            "groupId=$groupId AND productId=$productId",
            null,
            null,
            null,
            null
        )
        val result = cursor.count > 0
        cursor.close()
        return result
    }
}