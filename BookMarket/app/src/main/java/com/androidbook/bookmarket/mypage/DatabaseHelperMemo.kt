package com.androidbook.bookmarket.mypage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelperMemo(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "Memo.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "MemoTABLE"
        const val KEY_ID = "id"
        const val KEY_DATE = "date"
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_DATE TEXT, " +
                "$KEY_TITLE TEXT, " +
                "$KEY_CONTENT TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun memoValues(date: String, title: String, content: String) = ContentValues().apply {
        put(KEY_DATE, date)
        put(KEY_TITLE, title)
        put(KEY_CONTENT, content)
    }

    private fun toMemoList(cursor: Cursor): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    hashMapOf(
                        "id" to it.getInt(0).toString(),
                        "date" to it.getString(1),
                        "title" to it.getString(2),
                        "content" to it.getString(3)
                    )
                )
            }
        }
        return list
    }

    fun insertData(date: String, title: String, content: String) {
        writableDatabase.insert(TABLE_NAME, null, memoValues(date, title, content))
    }

    fun readData(): ArrayList<HashMap<String, String>> {
        val db = writableDatabase
        val list = toMemoList(
            db.rawQuery("SELECT $KEY_ID, $KEY_DATE, $KEY_TITLE, $KEY_CONTENT FROM $TABLE_NAME", null)
        )
        db.close()
        return list
    }

    fun updateData(date: String, title: String, content: String, id: Int): Int =
        writableDatabase.update(TABLE_NAME, memoValues(date, title, content), "$KEY_ID = ?", arrayOf(id.toString()))

    fun deleteData(id: Int) {
        writableDatabase.delete(TABLE_NAME, "$KEY_ID = ?", arrayOf(id.toString()))
    }

    fun deleteAllData() {
        writableDatabase.delete(TABLE_NAME, null, null)
    }

    fun searchReadData(keyword: String): ArrayList<HashMap<String, String>> =
        toMemoList(
            writableDatabase.query(
                TABLE_NAME,
                arrayOf(KEY_ID, KEY_DATE, KEY_TITLE, KEY_CONTENT),
                "$KEY_CONTENT LIKE ?",
                arrayOf("%$keyword%"),
                null, null, null
            )
        )
}
