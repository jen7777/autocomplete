package com.example.simple;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.List;

public class Controller {

    @FXML
    private TextField inputField;

    @FXML
    private ListView<String> suggestionList;

    @FXML
    public void initialize() {

        System.out.println("Controller loaded"); // DEBUG

        // Debounce (prevents too many DB calls)
        PauseTransition pause = new PauseTransition(Duration.millis(200));

        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Typing: " + newVal); // DEBUG

            pause.setOnFinished(e -> fetchSuggestions(newVal));
            pause.playFromStart();
        });

        // Click suggestion
        suggestionList.setOnMouseClicked(e -> {
            String selected = suggestionList.getSelectionModel().getSelectedItem();

            if (selected != null) {
                applySuggestion(selected);
            }
        });
    }

    private void fetchSuggestions(String input) {

        if (input == null || input.trim().isEmpty()) {
            Platform.runLater(() -> suggestionList.getItems().clear());
            return;
        }

        new Thread(() -> {
            List<String> results;

            // 🔤 First word → prefix search
            if (!input.contains(" ")) {
                results = SuggestionService.getPrefixSuggestions(input);
            }
            // Sentence → next-word prediction
            else {
                results = SuggestionService.getNextWordSuggestions(input);
            }

            Platform.runLater(() -> {
                suggestionList.getItems().setAll(results);
            });

        }).start();
    }

    private void applySuggestion(String selected) {
        String current = inputField.getText();

        if (current.contains(" ")) {
            inputField.setText(current + " " + selected);
        } else {
            inputField.setText(selected);
        }

        inputField.positionCaret(inputField.getText().length());
    }
}
