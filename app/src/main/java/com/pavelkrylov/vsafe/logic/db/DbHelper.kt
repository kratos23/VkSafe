package com.pavelkrylov.vsafe.logic.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pavelkrylov.vsafe.App

object DbHelper : SQLiteOpenHelper(App.instance, "db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE favorites (groupId int, productId int); 
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}