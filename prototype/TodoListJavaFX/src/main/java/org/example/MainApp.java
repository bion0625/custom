package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Hello, JavaFX!");
        Button button = new Button("Click me");

        button.setOnAction(event -> label.setText("Button Clicked"));

        VBox root = new VBox(10, label, button);
        root.setStyle("-fx-padding: 20; -fx-allignment: center; --fx-background-color: #f0f0f0;");

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("JAVAFX ToDoList - Step 1");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}