package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
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

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Work", "Personal", "Study");
        categoryBox.setValue("Work");

        DatePicker deadlinePicker = new DatePicker(LocalDate.now());

        Button addButton = new Button("추가");
        Button saveButton = new Button("저장");
        Button deleteButton = new Button("삭제");
        Button themeButton = new Button("\uD83C\uDF19 모드 전환");

        TextField searchField = new TextField();
        searchField.setPromptText("검색...");

        TableView<ToDoItem> tableView = new TableView<>();
        tableView.setEditable(true);
        tableView.getItems().addAll(items);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ToDoItem, Boolean> doneCol = new TableColumn<>("완료");
        doneCol.setCellValueFactory(new PropertyValueFactory<>("done"));
        doneCol.setCellFactory(CheckBoxTableCell.forTableColumn(doneCol));

        TableColumn<ToDoItem, String> textCol = new TableColumn<>("할 일");
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));

        TableColumn<ToDoItem, String> categoryCol = new TableColumn<>("카테고리");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<ToDoItem, LocalDate> deadlineCol = new TableColumn<>("마감일");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        deadlineCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate deadline, boolean empty) {
                super.updateItem(deadline, empty);

                if (empty || deadline == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(deadline.toString());

                    if (deadline.isEqual(LocalDate.now())) {
                        setTextFill(Color.ORANGE);
                        setStyle("-fx-font-weight: bold;");
                    } else if (deadline.isBefore(LocalDate.now())) {
                        setTextFill(Color.RED);
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(Color.BLACK);
                        setStyle("");
                    }
                }
            }
        });

        tableView.getColumns().addAll(doneCol, textCol, categoryCol, deadlineCol);

        FilteredList<ToDoItem> filteredItems = new FilteredList<>(items, p -> true);
        tableView.setItems(filteredItems);

        searchField.textProperty().addListener((obs, old, val) -> {
            String lower = val.toLowerCase();
            filteredItems.setPredicate(item ->
                    item.getText().toLowerCase().contains(lower) ||
                    item.getCategory().toLowerCase().contains(lower));
        });

        deleteButton.setId("delete-button");

        addButton.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                items.add(new ToDoItem(text, categoryBox.getValue(), deadlinePicker.getValue()));
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
            ToDoItem selectedItem = tableView.getSelectionModel().getSelectedItem();
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

        HBox headerBox = new HBox(10, categoryBox, deadlinePicker);
        HBox inputBox = new HBox(10, inputField, addButton, deleteButton, themeButton, saveButton);
        HBox searchBox = new HBox(10, searchField);
        inputBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        VBox root = new VBox(10, headerBox, inputBox, searchBox, tableView);
        root.setStyle("-fx-padding: 20; -fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/light.css").toExternalForm());

        primaryStage.setTitle("JAVAFX ToDoList - Step 6 (확장 기능)");
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