package org.example;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ToDoItem {
    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty done = new SimpleBooleanProperty(false);

    public ToDoItem(String text) {
        this.text.set(text);
    }

    public StringProperty textProperty() {
        return text;
    }

    public BooleanProperty doneProperty() {
        return done;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String value) {
        text.set(value);
    }

    public boolean isDone() {
        return done.get();
    }

    public void setDone(boolean value) {
        done.set(value);
    }
}
