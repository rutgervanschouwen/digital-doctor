package com.example.digitaldoctor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.digitaldoctor.models.Session;
import com.example.digitaldoctor.models.User;

import java.util.List;

@Dao
public interface SessionDao {
    
    public class SessionWithUserInfo extends Session {

        public String name;
        public int age;
        public String gender;

        public SessionWithUserInfo(long userId) {
            super(userId);
        }

        @Override
        public String toString() {
            return super.toString() + "| gender: " + gender + " | age: " + age;
        }
    }

    @Query("select Session.*, User.name, User.age, User.gender from Session left join User on Session.userId = User.id where User.id = :id")
    public SessionWithUserInfo getSessionWithUserInfo(long id);

    @Query("select * from Session")
    List<Session> getAllSessions();

    @Insert
    void addSession(Session session);

    @Query("select * from Session where id = :id")
    Session getSession(String id);

    @Query("select * from Session where id = :id")
    LiveData<Session> getLiveSession(String id);

    @Update
    void updateSession(Session session);

    @Insert
    long addUser(User user);


    @Update
    void updateUser(User user);

    @Query("select * from User left join Session on Session.userId = User.id where Session.id = :sessionId limit 1")
    User getUserBySessionId(String sessionId);

    @Query("select * from User")
    List<User> getAllUsers();
    
}
