package com.pigeoff.todo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.pigeoff.todo.db.RmDB


class MainActivity : AppCompatActivity() {

    private lateinit var db: RmDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(
            applicationContext,
            RmDB::class.java, "notes"
        ).allowMainThreadQueries().build()

        if (!getIntroSet()) {
            intent = Intent(this, MainIntroActivity::class.java)
            startActivity(intent)
        }

        val metroId = getMetroID()
        updateChecklistUI(db, metroId)

    }


    fun updateChecklistUI(db: RmDB, id: Int) {
        val checklistFragment = ChecklistFragment(id, db)

        if (supportFragmentManager.findFragmentByTag("checklist") == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, checklistFragment, "checklist")
                .commit()
        }
        else {
            supportFragmentManager.beginTransaction()
                .remove(supportFragmentManager.findFragmentByTag("checklist")!!)
                .replace(android.R.id.content, checklistFragment, "checklist")
                .commit()
        }

        checklistFragment.setOnListChangeListener(object : ChecklistFragment.OnListChangeListener {
            override fun onListChangeListener(metroid: Int) {
                setMetroID(metroid)
                updateChecklistUI(db, metroid)
            }
        })
    }

    fun getMetroID() : Int {
        val prefs: SharedPreferences = this.getSharedPreferences("theme", 0)
        val metroId = prefs.getInt("metroid", 1)
        return metroId
    }

    fun setMetroID(id: Int) {
        val prefs = this.getSharedPreferences("theme", 0)
        prefs.edit().putInt("metroid", id).apply()
    }

    fun getIntroSet() : Boolean {
        val prefs: SharedPreferences = getSharedPreferences("theme", 0)
        val metroId = prefs.getBoolean("introdone", false)
        return metroId
    }
}
