package dev.gustavoesposar.reminder.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "aniversariantes")
public class Aniversariante {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String _id;
    private String name;
    private String date;

    public Aniversariante() {}

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date.substring(0, 10);
    }

    public String getFormattedDate() {
        String ano = this.date.substring(0, 4);
        String mes = this.date.substring(5, 7);
        String dia = this.date.substring(8, 10);
        return dia + "/" + mes + "/" + ano;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_id() {
        return this._id;
    }

    public int getId() {
        return this.id;
    }
}
