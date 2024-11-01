package dev.gustavoesposar.reminder.repository;

import android.content.Context;

import androidx.room.Room;

import java.util.List;

import dev.gustavoesposar.reminder.database.AniversarianteDao;
import dev.gustavoesposar.reminder.database.AppDatabase;
import dev.gustavoesposar.reminder.model.Aniversariante;

public class AniversarianteRepository {
    private final AniversarianteDao aniversarianteDao;

    public AniversarianteRepository(Context context) {
        AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, "AppDatabase")
                .fallbackToDestructiveMigration()
                .build();
        this.aniversarianteDao = db.aniversarianteDao();
    }

    public List<Aniversariante> getAllAniversariantes() {
        return aniversarianteDao.getAllAniversariantes();
    }

    public void insertAll(List<Aniversariante> aniversariantes) {
        aniversarianteDao.insertAll(aniversariantes);
    }

    public void deleteAll() {
        aniversarianteDao.deleteAll();
    }
}
