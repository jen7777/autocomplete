package com.example.simple;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private TextField inputField;
    private ListView<String> suggestionList;
    private TextArea outputArea;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {

        DataImporter.importTextFile("C:/Users/jenso/Downloads/dataset.txt");
        System.out.println("Import ran");

        Label title = new Label("Welcome");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        // ✅ UPDATED INPUT FIELD (mid → dark blue)
        inputField = new TextField();
        inputField.setPromptText("Type a word");
        inputField.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8;" +
                        "-fx-background-color: linear-gradient(to right, #2c5364, #203a43);" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #bbbbbb;" +
                        "-fx-border-color: #1e90ff;" +
                        "-fx-border-radius: 10;"
        );

        Button suggestBtn = new Button("Autocomplete Word");
        Button generateBtn = new Button("Generate Sentence");


        suggestBtn.setStyle(buttonStyle());
        generateBtn.setStyle(buttonStyle());

        HBox buttons = new HBox(10, suggestBtn, generateBtn);
        buttons.setAlignment(Pos.CENTER);


        // ✅ Styled ListView
        suggestionList = new ListView<>();
        suggestionList.setPrefHeight(150);
        suggestionList.setStyle(
                "-fx-control-inner-background: #203a43;" +
                        "-fx-background-radius: 10;" +
                        "-fx-text-fill: white;"
        );

        // ✅ Styled Output Area
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(150);
        outputArea.setStyle(
                "-fx-control-inner-background: #203a43;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;"
        );

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: lightgray;");

        VBox layout = new VBox(
                15,
                title,
                inputField,
                buttons,
                new Label("Suggestions:"),
                suggestionList,
                new Label("Output:"),
                outputArea,
                statusLabel

        );


        layout.setPadding(new Insets(20));
        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0f2027, #203a43, #2c5364);" +
                        "-fx-background-radius: white;"
        );

        Scene scene = new Scene(layout, 600, 550);

        // Actions
        suggestBtn.setOnAction(e -> getSuggestions());
        generateBtn.setOnAction(e -> generateSentence());

        suggestionList.setOnMouseClicked(e -> {
            String selected = suggestionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String word = selected.split(" \\(")[0];
                inputField.setText(word);
                getSuggestions();
            }
        });

        stage.setTitle("Sentence Builder");
        stage.setScene(scene);
        stage.show();

        testConnection();
    }

    private String buttonStyle() {
        return "-fx-background-color: #1e90ff;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;";
    }

    private void testConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                statusLabel.setText("Connected to database");
            }
        } catch (Exception e) {
            statusLabel.setText("DB Error");
        }
    }

    private void getSuggestions() {
        String word = inputField.getText().trim().toLowerCase();

        if (word.isEmpty()) {
            statusLabel.setText("Enter a word");
            return;
        }

        String query = """
                SELECT w2.word_text, wf.follow_count
                FROM word_follows wf
                JOIN word w1 ON wf.word_id = w1.id
                JOIN word w2 ON wf.next_word_id = w2.id
                WHERE w1.word_text = ?
                ORDER BY wf.follow_count DESC
                LIMIT 5
                """;

        List<String> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, word);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString("word_text") +
                        " (" + rs.getInt("follow_count") + ")");
            }

            suggestionList.setItems(FXCollections.observableArrayList(results));
            statusLabel.setText("Suggestions loaded");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error fetching suggestions");
        }
    }

    private void generateSentence() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("CALL sp_generate_bigram_sentence(10)")) {

            if (rs.next()) {
                outputArea.setText(rs.getString("generated_sentence"));
                statusLabel.setText("Sentence generated");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error generating sentence");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}