package com.example.simple;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class Main extends Application {

    private TextField inputField;
    private ListView<String> suggestionList;
    private TextArea outputArea;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {

        // ❌ KEEP THIS COMMENTED after DB import
        // DataImporter.importTextFile("/Users/jenso/Desktop/dataset.txt");

        Label title = new Label("Welcome");
        title.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;"
        );

        inputField = new TextField();
        inputField.setPromptText("Type a word");
        inputField.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 12px;" +
                        "-fx-background-color: linear-gradient(to right, #2c5364, #203a43);" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #bbbbbb;" +
                        "-fx-border-color: #1e90ff;" +
                        "-fx-border-radius: 20px;"
        );

        Button autoCompleteBtn = new Button("Autocomplete Word");
        Button generateBtn = new Button("Generate Sentence");

        String buttonStyle =
                "-fx-background-color: linear-gradient(to right, #1e90ff, #00bfff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10px 20px;" +
                        "-fx-background-radius: 20px;";

        autoCompleteBtn.setStyle(buttonStyle);
        generateBtn.setStyle(buttonStyle);

        suggestionList = new ListView<>();
        suggestionList.setPrefHeight(150);
        suggestionList.setStyle(
                "-fx-background-color: #1b3c44;" +
                        "-fx-control-inner-background: #1b3c44;" +
                        "-fx-text-fill: white;"
        );

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(150);
        outputArea.setStyle(
                "-fx-control-inner-background: #1b3c44;" +
                        "-fx-text-fill: white;"
        );

        Label suggestionLabel = new Label("Suggestions:");
        suggestionLabel.setStyle("-fx-text-fill: #ff6f61;");

        Label outputLabel = new Label("Output:");
        outputLabel.setStyle("-fx-text-fill: #ff6f61;");

        statusLabel = new Label("Suggestions loaded");
        statusLabel.setStyle("-fx-text-fill: lightgray;");

        // ✅ Button Actions
        autoCompleteBtn.setOnAction(e -> fetchSuggestions());
        inputField.setOnAction(e -> fetchSuggestions());

        generateBtn.setOnAction(e -> generateSentence());

        VBox layout = new VBox(15,
                title,
                inputField,
                autoCompleteBtn,
                generateBtn,
                suggestionLabel,
                suggestionList,
                outputLabel,
                outputArea,
                statusLabel
        );

        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0f2027, #203a43, #2c5364);"
        );

        Scene scene = new Scene(layout, 500, 600);
        stage.setScene(scene);
        stage.setTitle("Sentence Builder");
        stage.show();
    }

    // ✅ Fetch suggestions from DB
    private void fetchSuggestions() {
        String input = inputField.getText().trim();

        if (input.isEmpty()) {
            statusLabel.setText("Enter a word");
            return;
        }

        String result = DBHelper.getSuggestions(input);

        List<String> items = Arrays.asList(result.split("\n"));
        suggestionList.setItems(FXCollections.observableArrayList(items));

        statusLabel.setText("Suggestions loaded");
    }

    // ✅ Generate sentence using DB (basic logic)
    private void generateSentence() {
        String input = inputField.getText().trim();

        if (input.isEmpty()) {
            statusLabel.setText("Enter a word");
            return;
        }

        StringBuilder sentence = new StringBuilder(input);

        String currentWord = input;

        for (int i = 0; i < 10; i++) {
            String next = DBHelper.getTopNextWord(currentWord);

            if (next == null || next.isEmpty()) break;

            sentence.append(" ").append(next);
            currentWord = next;
        }

        outputArea.setText(sentence.toString());
        statusLabel.setText("Sentence generated");
    }

    public static void main(String[] args) {
        launch();
    }
}
