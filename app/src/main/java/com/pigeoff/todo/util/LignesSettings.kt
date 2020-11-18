package com.pigeoff.todo.util

import android.content.Context
import android.content.SharedPreferences

class LignesSettings {

    fun setTitle(context: Context, numero: Int, title: String) {
        val prefStr = "l"+numero.toString()
        val pref: SharedPreferences = context.getSharedPreferences("lignes", 0)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString(prefStr, title)
        editor.apply()
    }

    fun getTitle(context: Context, numero: Int) : String {
        val prefStr = "l"+numero.toString()
        val pref: SharedPreferences = context.getSharedPreferences("lignes", 0)
        val title = pref.getString(prefStr, "@null")
        return if (!title.isNullOrEmpty()) title else "@null"
    }
}