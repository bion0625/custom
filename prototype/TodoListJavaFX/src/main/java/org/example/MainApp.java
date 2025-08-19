package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {

        TextField inputField = new TextField();
        inputField.setPromptText("할 일을 입력하세요...");

        Button addButton = new Button("추가");

        ObservableList<String> items = FXCollections.observableArrayList();
        ListView<String> listView = new ListView<>(items);

        addButton.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                items.add(text);
                inputField.clear();
            }
        });

        inputField.setOnAction(e -> addButton.fire());

        HBox inputBox = new HBox(10, inputField, addButton);
        inputBox.setStyle("-fx-padding: 20; -fx-background-color: #fafafa;");

        VBox root = new VBox(10, inputBox, listView);
        root.setStyle("-fx-padding: 20; -fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("JAVAFX ToDoList - Step 2");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}