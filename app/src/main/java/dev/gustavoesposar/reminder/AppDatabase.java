package dev.gustavoesposar.reminder;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Aniversariante.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AniversarianteDao aniversarianteDao();
}
