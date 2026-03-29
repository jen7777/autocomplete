
package com.example.simple;

import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        TextField input = new TextField();
        input.setPromptText("Enter word");

        Button btn = new Button("Get Suggestions");
        TextArea output = new TextArea();

        btn.setOnAction(e -> {
            String word = input.getText();
            String result = DBHelper.getSuggestions(word);
            output.setText(result);
        });

        VBox root = new VBox(10, input, btn, output);
        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("Sentence Builder");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        DBConnection.getConnection();
        launch();
    }
}

