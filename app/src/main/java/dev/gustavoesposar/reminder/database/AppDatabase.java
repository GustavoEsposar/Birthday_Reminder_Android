package dev.gustavoesposar.reminder.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import dev.gustavoesposar.reminder.model.Aniversariante;

@Database(entities = {Aniversariante.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AniversarianteDao aniversarianteDao();
}
