package com.example.digitaldoctor.models;

public class Symptom {

    String id;
    String label;
    Boolean selected;

    public Symptom(String id, String label) {
        this.id = id;
        this.label = label;
        this.selected = false;
    }

    public void toggleSelected() {
        this.selected = !this.selected;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Boolean getSelected() {
        return selected;
    }
}
