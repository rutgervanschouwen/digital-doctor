package com.example.digitaldoctor.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(foreignKeys = {@ForeignKey(entity = User.class,
    parentColumns = "id",
    childColumns = "userId",
    onDelete = ForeignKey.CASCADE)
})
public class Session {

    @PrimaryKey @NotNull
    public String id;

    public long userId;

    @TypeConverters(EvidenceListConverter.class)
    public List<Evidence> evidenceList;

    public Session(long userId) {
        this.id = UUID.randomUUID().toString();
        this.evidenceList = new ArrayList<Evidence>();
        this.userId = userId;
    }

//    @Override
    public String toString() {
        return  "id:" + id +
                ", userId:" + userId +
                ", evidenceList: " + evidenceList;
    }

    public void deleteEvidenceById(Evidence evidence) {
        evidenceList.remove(evidence);
    }

    // TypeConverter for the List<Evidence>
    public static class EvidenceListConverter {
        @TypeConverter
        public static List<Evidence> fromString(String value) {
            // Convert the JSON string to a List<Evidence>
            Type listType = new TypeToken<List<Evidence>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String toString(List<Evidence> list) {
            // Convert the List<Evidence> to a JSON string
            return new Gson().toJson(list);
        }
    }

}
