package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {

        TextField inputField = new TextField();
        inputField.setPromptText("할 일을 입력하세요...");

        Button addButton = new Button("추가");
        Button deleteButton = new Button("삭제");

        deleteButton.setId("delete-button");

        ObservableList<ToDoItem> items = FXCollections.observableArrayList();
        ListView<ToDoItem> listView = new ListView<>(items);

        listView.setCellFactory(lv -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final Text text = new Text();

            {
                checkBox.setOnAction(e -> {
                    ToDoItem item = getItem();
                    if (item.getText() != null) {
                        item.setDone(checkBox.isSelected());
                        updateItem(item, false);
                    }
                });
            }

            @Override
            protected void updateItem(ToDoItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    checkBox.setSelected(item.isDone());
                    text.setText(item.getText());

                    if (item.isDone()) {
                        text.setStrikethrough(true);
                        text.setFill(Color.GRAY);
                    } else {
                        text.setStrikethrough(false);
                        text.setFill(Color.BLACK);
                    }

                    HBox cellBox = new HBox(10, checkBox, text);
                    setGraphic(cellBox);
                }
            }
        });

        addButton.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                items.add(new ToDoItem(text));
                inputField.clear();
            }
        });

        inputField.setOnAction(e -> addButton.fire());

        deleteButton.setOnAction(e -> {
            ToDoItem selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                items.remove(selectedItem);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("경고");
                alert.setHeaderText(null);
                alert.setContentText("삭제할 항목을 선택하세요!");
                alert.showAndWait();
            }
        });

        HBox inputBox = new HBox(10, inputField, addButton, deleteButton);
        inputBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        VBox root = new VBox(10, inputBox, listView);
        root.setStyle("-fx-padding: 20; -fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 450, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("JAVAFX ToDoList - Step 3");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}