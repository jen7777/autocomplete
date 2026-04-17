package com.example.simple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBHelper {
    public static String getTopNextWord(String word) {
        try (Connection conn = DBConnection.getConnection()) {

            String query = """
            SELECT w2.word_text
            FROM word_follows wf
            JOIN word w1 ON wf.word_id = w1.id
            JOIN word w2 ON wf.next_word_id = w2.id
            WHERE w1.word_text = ?
            ORDER BY wf.follow_count DESC
            LIMIT 1
        """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, word.toLowerCase());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("word_text");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String getSuggestions(String word) {
        StringBuilder result = new StringBuilder();

        try (Connection conn = DBConnection.getConnection()) {

            String query = """
                SELECT w2.word_text, wf.follow_count
                FROM word_follows wf
                JOIN word w1 ON wf.word_id = w1.id
                JOIN word w2 ON wf.next_word_id = w2.id
                WHERE w1.word_text = ?
                ORDER BY wf.follow_count DESC
                LIMIT 5
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, word.toLowerCase());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.append(rs.getString("word_text"))
                        .append(" (")
                        .append(rs.getInt("follow_count"))
                        .append(")\n");
            }

            // If no results found
            if (result.length() == 0) {
                return "No suggestions found.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving suggestions.";
        }

        return result.toString();
    }
}