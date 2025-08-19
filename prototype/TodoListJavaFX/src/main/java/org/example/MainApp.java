package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
        Button deleteButton = new Button("삭제");

        deleteButton.setId("delete-button");

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

        deleteButton.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                items.remove(selected);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("경고");
                alert.setHeaderText(null);
                alert.setContentText("삭제할 항목을 선택하세요!");
                alert.showAndWait();
            }
        });

        HBox inputBox = new HBox(10, inputField, addButton, deleteButton);
        inputBox.setStyle("-fx-padding: 20; -fx-alignment: conter;");

        VBox root = new VBox(10, inputBox, listView);
        root.setStyle("-fx-padding: 20; -fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 450, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("JAVAFX ToDoList - Step 2 (응용: 삭제)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}