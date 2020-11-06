package com.pigeoff.todo.db

import androidx.room.*

@Database(entities = arrayOf(RmTask::class), version = 1)
abstract class RmDB : RoomDatabase() {
    abstract fun tasksDAO(): RmTaskDAO
}