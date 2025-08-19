package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        Button btn = new Button("Hello, JavaFX!");
        btn.setOnAction(e -> System.out.println("Button clicked!"));

        StackPane root = new StackPane(btn);
        Scene scene = new Scene(root, 400, 300);

        stage.setScene(scene);
        stage.show();
    }
}