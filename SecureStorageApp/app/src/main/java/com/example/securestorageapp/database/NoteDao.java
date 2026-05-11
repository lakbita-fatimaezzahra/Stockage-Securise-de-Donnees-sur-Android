package com.example.securestorage.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNoteById(int id);

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY createdAt DESC")
    List<Note> getNotesByUserId(int userId);

    @Query("SELECT * FROM notes")
    List<Note> getAllNotes();
}