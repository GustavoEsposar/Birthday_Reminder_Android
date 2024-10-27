package dev.gustavoesposar.reminder;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AniversarianteDao {
    @Insert
    void insertAll(List<Aniversariante> aniversariantes);

    @Query("SELECT * FROM aniversariantes")
    List<Aniversariante> getAllAniversariantes();

    @Query("DELETE FROM aniversariantes")
    void deleteAll();
}

