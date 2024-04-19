package com.example.digitaldoctor;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.digitaldoctor.models.Session;
import com.example.digitaldoctor.models.User;

@Database(entities = {Session.class, User.class}, version = 40)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SessionDao sessionDao();
    static AppDatabase instance;

    static synchronized public AppDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "database-name")
                    .allowMainThreadQueries() // Development only
                    .fallbackToDestructiveMigration() // Development only
                    .build();
        }
        return instance;
    }

}