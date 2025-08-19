package org.example;

public class ToDoItem {
    private String text;
    private boolean done;

    public ToDoItem() {}

    public ToDoItem(String text) {
        this.text = text;
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
}
