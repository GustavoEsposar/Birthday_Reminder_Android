package dev.gustavoesposar.reminder.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import dev.gustavoesposar.reminder.model.Aniversariante;

@Dao
public interface AniversarianteDao {
    @Insert
    void insertAll(List<Aniversariante> aniversariantes);

    @Query("SELECT * FROM aniversariantes")
    List<Aniversariante> getAllAniversariantes();

    @Query("DELETE FROM aniversariantes")
    void deleteAll();

    @Delete
    void delete(Aniversariante aniversariante);
}

