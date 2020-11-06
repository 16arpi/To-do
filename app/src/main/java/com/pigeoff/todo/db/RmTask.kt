package com.pigeoff.todo.db

import androidx.room.*

@Entity
data class RmTask (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var titre: String? = null,
    var details: String = "",
    var done: Int = 0,
    var emergency: Int = 0,
    var ligne: Int = 1
)