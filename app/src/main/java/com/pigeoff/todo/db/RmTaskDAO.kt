package com.pigeoff.todo.db

import androidx.room.*

@Dao
interface RmTaskDAO {
    @Query("SELECT * FROM RmTask ORDER BY id")
    fun getAllTasks(): List<RmTask>

    @Query("SELECT * FROM RmTask WHERE ligne IS :ligne ORDER BY id ")
    fun getAllTasks(ligne: Int): List<RmTask>

    @Query("SELECT * FROM RmTask WHERE emergency IS 0 ORDER BY id")
    fun getAllNonEmerencyTasks() : List<RmTask>

    @Query("SELECT * FROM RmTask WHERE emergency IS 1 ORDER BY id")
    fun getAllEmerencyTasks() : List<RmTask>

    @Query("SELECT * FROM RmTask WHERE id LIKE :id")
    fun getTask(id: Int) : RmTask

    @Update
    fun updateNote(note: RmTask?)

    @Insert
    fun newNote(note: RmTask?)

    @Delete
    fun deleteNote(note: RmTask?)

    @Query("DELETE FROM RmTask WHERE done IS 1")
    fun clearTasks()
}