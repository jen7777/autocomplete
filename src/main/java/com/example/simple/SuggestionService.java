package com.example.simple;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SuggestionService {

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/sentence_builder";
    private static final String USER = "root";
    private static final String PASS = "Holybible";

    // PREFIX AUTOCOMPLETE
    public static List<String> getPrefixSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();

        String query = "SELECT word_text FROM word WHERE word_text LIKE ? LIMIT 5";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, input.toLowerCase() + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suggestions.add(rs.getString("word_text"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return suggestions;
    }

    // NEXT WORD PREDICTION
    public static List<String> getNextWordSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();

        if (input == null || input.trim().isEmpty()) {
            return suggestions;
        }

        String[] words = input.trim().split("\\s+");
        String lastWord = words[words.length - 1].toLowerCase();

        String query = """
            SELECT w2.word_text
            FROM word_follows wf
            JOIN word w1 ON wf.word_id = w1.id
            JOIN word w2 ON wf.next_word_id = w2.id
            WHERE w1.word_text = ?
            ORDER BY wf.follow_count DESC
            LIMIT 5
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, lastWord);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suggestions.add(rs.getString("word_text"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return suggestions;
    }

    public static List<String> getFilteredNextWordSuggestions(String prevWord, String prefix) {
        List<String> suggestions = new ArrayList<>();

        String query = """
        SELECT w2.word_text
        FROM word_follows wf
        JOIN word w1 ON wf.word_id = w1.id
        JOIN word w2 ON wf.next_word_id = w2.id
        WHERE LOWER(w1.word_text) = ?
        AND LOWER(w2.word_text) LIKE ?
        ORDER BY wf.follow_count DESC
        LIMIT 5
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, prevWord.toLowerCase());
            stmt.setString(2, prefix.toLowerCase() + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suggestions.add(rs.getString("word_text"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return suggestions;
    }

}
