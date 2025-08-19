package org.example;

import java.time.LocalDate;

public class ToDoItem {
    private String text;
    private boolean done;
    private String category;
    private LocalDate deadline;

    public ToDoItem() {}

    public ToDoItem(String text, String category, LocalDate deadline) {
        this.text = text;
        this.category = category;
        this.deadline = deadline;
        this.done = false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
