
package com.example.simple;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Title text
        Label title = new Label("Welcome");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Subtitle text
        Label subtitle = new Label("Type a word to get autocomplete suggestions");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: lightgray;");

        // Input field
        TextField input = new TextField();
        input.setPromptText("Enter word...");
        input.setStyle("""
            -fx-background-color: #1e3a5f;
            -fx-text-fill: white;
            -fx-prompt-text-fill: gray;
            -fx-background-radius: 8;
        """);

        // Button
        Button btn = new Button("Get Suggestions");
        btn.setStyle("""
            -fx-background-color: #2563eb;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
        """);

        // Output area
        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefHeight(200);
        output.setStyle("""
            -fx-control-inner-background: #0f172a;
            -fx-text-fill: white;
            -fx-background-radius: 8;
        """);

        // Button action
        btn.setOnAction(e -> {
            String word = input.getText().trim();

            if (word.isEmpty()) {
                output.setText("Please enter a word");
                return;
            }

            String result = DBHelper.getSuggestions(word);
            output.setText(result);
        });

        // Layout
        VBox root = new VBox(15, title, subtitle, input, btn, output);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0b1e3a;"); // dark blue background

        Scene scene = new Scene(root, 420, 350, Color.BLACK);

        stage.setTitle("Sentence Builder");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        DBConnection.getConnection();
        launch();
    }
}