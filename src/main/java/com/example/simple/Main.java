package com.example.simple;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private TextField inputField;
    private ListView<String> suggestionList;
    private TextArea outputArea;

    @Override
    public void start(Stage stage) {

        Label title = new Label("Smart Autocomplete");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        inputField = new TextField();
        inputField.setPromptText("Start typing...");
        inputField.setStyle(
                "-fx-font-size:16px;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-radius:10;" +
                        "-fx-background-color:#1b3c44;" +
                        "-fx-text-fill:white;"
        );

        suggestionList = new ListView<>();
        suggestionList.setPrefHeight(150);
        suggestionList.setStyle(
                "-fx-control-inner-background:#1b3c44;" +
                        "-fx-text-fill:white;"
        );

        Button generateBtn = new Button("Generate Sentence");
        generateBtn.setStyle(
                "-fx-background-color:#1e90ff;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:14px;" +
                        "-fx-background-radius:10;"
        );

        outputArea = new TextArea();
        outputArea.setPromptText("Generated sentence...");
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(120);
        outputArea.setStyle(
                "-fx-control-inner-background:#1b3c44;" +
                        "-fx-text-fill:white;"
        );

        // 🔥 REAL-TIME SUGGESTIONS
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null || newVal.trim().isEmpty()) {
                suggestionList.getItems().clear();
                return;
            }

            new Thread(() -> {
                List<String> results;

                if (newVal.contains(" ")) {
                    results = SuggestionService.getNextWordSuggestions(newVal);
                } else {
                    results = SuggestionService.getPrefixSuggestions(newVal);
                }

                Platform.runLater(() -> suggestionList.getItems().setAll(results));

            }).start();
        });

        // 🔥 CLICK SUGGESTION
        suggestionList.setOnMouseClicked(e -> {
            String selected = suggestionList.getSelectionModel().getSelectedItem();

            if (selected != null) {
                String current = inputField.getText();

                if (current.contains(" ")) {
                    inputField.setText(current + " " + selected);
                } else {
                    inputField.setText(selected);
                }

                inputField.positionCaret(inputField.getText().length());
            }
        });

        // 🔥 SENTENCE GENERATION
        generateBtn.setOnAction(e -> generateSentence());

        VBox layout = new VBox(12,
                title,
                inputField,
                suggestionList,
                generateBtn,
                outputArea
        );

        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0f2027, #203a43, #2c5364);"
        );

        Scene scene = new Scene(layout, 500, 550);

        stage.setTitle("Autocomplete + Sentence Generator");
        stage.setScene(scene);
        stage.show();
    }

    // 🧠 SENTENCE GENERATION LOGIC
    private void generateSentence() {

        String input = inputField.getText();

        if (input == null || input.trim().isEmpty()) {
            outputArea.setText("Enter a word first");
            return;
        }

        new Thread(() -> {

            StringBuilder sentence = new StringBuilder(input.trim());
            String currentWord = input.trim();

            for (int i = 0; i < 10; i++) {

                List<String> nextWords = SuggestionService.getNextWordSuggestions(currentWord);

                if (nextWords.isEmpty()) break;

                String next = nextWords.get(0);

                sentence.append(" ").append(next);
                currentWord = next;
            }

            Platform.runLater(() -> outputArea.setText(sentence.toString()));

        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}
