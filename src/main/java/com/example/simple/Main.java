package com.example.simple;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class Main extends Application {

    private TextField inputField;
    private ListView<String> suggestionList;
    private TextArea outputArea;

    @Override
    public void start(Stage stage) {

        // Title
        Label title = new Label("Autocomplete Word and Sentence generation");
        title.setStyle(
                "-fx-text-fill: #e6f1ff;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;"
        );

        //  Section Labels
        Label inputLabel = new Label("Start typing");
        Label suggestionLabel = new Label("Word suggestion");
        Label outputLabel = new Label("Sentence generation");

        String labelStyle =
                "-fx-text-fill: #8892b0;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;";

        inputLabel.setStyle(labelStyle);
        suggestionLabel.setStyle(labelStyle);
        outputLabel.setStyle(labelStyle);

        // Input Field
        inputField = new TextField();
        inputField.setPromptText("Type here...");
        inputField.setStyle(
                "-fx-font-size:16px;" +
                        "-fx-padding:12;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-radius:12;" +
                        "-fx-background-color:#112240;" +
                        "-fx-text-fill:white;" +
                        "-fx-border-color:#1e90ff;" +
                        "-fx-border-width:1.5;"
        );

        //  Suggestion List
        suggestionList = new ListView<>();
        suggestionList.setPrefHeight(140);
        suggestionList.setStyle(
                "-fx-control-inner-background:#112240;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-color:#1e90ff;" +
                        "-fx-text-fill:white;"
        );

        // Output Area
        outputArea = new TextArea();
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(130);
        outputArea.setStyle(
                "-fx-control-inner-background:#112240;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-color:#1e90ff;" +
                        "-fx-text-fill:white;"
        );

        //  Debounce typing
        PauseTransition pause = new PauseTransition(Duration.millis(250));

        inputField.textProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null || newVal.trim().isEmpty()) {
                suggestionList.getItems().clear();
                outputArea.clear();
                return;
            }

            pause.setOnFinished(e -> {

                new Thread(() -> {

                    List<String> results;


                    if (newVal.contains(" ")) {
                        // sentence → next word prediction
                        results = SuggestionService.getNextWordSuggestions(newVal);
                    } else {
                        // single word → prefix autocomplete
                        results = SuggestionService.getPrefixSuggestions(newVal);
                    }

                    Platform.runLater(() ->
                            suggestionList.getItems().setAll(results)
                    );

                }).start();

                //  Sentence generation always runs
                generateSentenceRealtime(newVal);

            });

            pause.playFromStart();
        });

        // Click suggestion
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

        // Layout
        VBox layout = new VBox(10,
                title,
                inputLabel,
                inputField,
                suggestionLabel,
                suggestionList,
                outputLabel,
                outputArea
        );

        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_LEFT);
        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #020c1b, #0a192f, #112240);"
        );

        Scene scene = new Scene(layout, 520, 560);

        stage.setTitle("Autocomplete AI");
        stage.setScene(scene);
        stage.show();
    }

    // Sentence generator
    private void generateSentenceRealtime(String input) {

        new Thread(() -> {

            StringBuilder sentence = new StringBuilder(input.trim());
            String currentWord = input.trim();

            for (int i = 0; i < 10; i++) {

                List<String> nextWords =
                        SuggestionService.getNextWordSuggestions(currentWord);

                if (nextWords.isEmpty()) break;

                String next = nextWords.get(0);

                sentence.append(" ").append(next);
                currentWord = next;
            }

            Platform.runLater(() ->
                    outputArea.setText(sentence.toString())
            );

        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}
