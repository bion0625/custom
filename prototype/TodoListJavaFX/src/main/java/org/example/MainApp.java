package org.example;

import javafx.animation.FadeTransition;
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
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class MainApp extends Application {

    private final ObservableList<ToDoItem> items = FXCollections.observableArrayList();
    private boolean darkMode = false;

    @Override
    public void start(Stage primaryStage) {
        try {
            List<ToDoItem> loaded = TodoStorage.load();
            items.addAll(loaded);
        } catch (IOException e) {
            showAlert("불러오기 오류", "데이터를 불러올 수 없습니다", Alert.AlertType.WARNING);
        }

        TextField inputField = new TextField();
        inputField.setPromptText("할 일을 입력하세요...");

        Button addButton = new Button("추가");
        Button saveButton = new Button("저장");
        Button deleteButton = new Button("삭제");
        Button themeButton = new Button("\uD83C\uDF19 모드 전환");

        deleteButton.setId("delete-button");

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

                        if (item.isDone()) {
                            FadeTransition ft = new FadeTransition(Duration.millis(500), text);
                            ft.setFromValue(1.0);
                            ft.setToValue(0.5);
                            ft.play();
                        } else {
                            FadeTransition ft = new FadeTransition(Duration.millis(300), text);
                            ft.setFromValue(text.getOpacity());
                            ft.setToValue(1.0);
                            ft.play();
                        }
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

        saveButton.setOnAction(e -> {
            try {
                TodoStorage.save(items);
                showAlert("저장 완료", "데이터가 저장되었습니다.", Alert.AlertType.INFORMATION);
            } catch (IOException ex) {
                showAlert("저장 오류", "데이터 저장에 실패했습니다.", Alert.AlertType.ERROR);
            }
        });

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

        themeButton.setOnAction(e -> {
            darkMode = !darkMode;
            if (darkMode) {
                primaryStage.getScene().getStylesheets()
                        .setAll(getClass().getResource("/dark.css").toExternalForm());
                themeButton.setText("☀ 라이트 모드");
            } else {
                primaryStage.getScene().getStylesheets()
                        .setAll(getClass().getResource("/light.css").toExternalForm());
                themeButton.setText("\uD83C\uDF19 다크 모드");
            }
        });

        HBox inputBox = new HBox(10, inputField, addButton, deleteButton, themeButton, saveButton);
        inputBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        VBox root = new VBox(10, inputBox, listView);
        root.setStyle("-fx-padding: 20; -fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/light.css").toExternalForm());

        primaryStage.setTitle("JAVAFX ToDoList - Step 5 (스타일링 & UX)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}