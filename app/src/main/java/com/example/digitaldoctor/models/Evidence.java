package com.example.digitaldoctor.models;

import org.jetbrains.annotations.NotNull;

//@Entity(foreignKeys = {@ForeignKey(entity = Session.class,
//        parentColumns = "id",
//        childColumns = "sessionId",
//        onDelete = ForeignKey.CASCADE
//)})
public class Evidence {

//    @PrimaryKey @NotNull
    public String id;
    public String sessionId;
    public String choice_id;
    public String label;
    public String source;

    public Evidence(String sessionId, String id, String choice_id, String source, String label) {
        this.sessionId = sessionId;
        this.id = id;
        this.choice_id = choice_id;
        this.source = source;
        this.label = label;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", choice_id:'" + choice_id + '\'' +
                ", source:'" + source + '\'' +
                '}';
    }

    public String getLabel() {
        return label;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setChoice_id(String choice_id) {
        this.choice_id = choice_id;
    }

    public String getChoice_id() {
        return choice_id;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
