package com.example.simple;

import java.io.*;
import java.sql.*;

public class DataImporter {

    // STEP 2 → put here
    public static void importTextFile(String filePath) {
        try (Connection conn = DBConnection.getConnection()) {

            System.out.println("IMPORT STARTED");

            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            int count = 0;

            while ((line = reader.readLine()) != null) {
                count++;

                String[] words = line.toLowerCase()
                        .replaceAll("[^a-z ]", "")
                        .split("\\s+");

                for (int i = 0; i < words.length; i++) {

                    if (words[i].isBlank()) continue;

                    int wordId = getOrInsertWord(conn, words[i]);

                    if (i < words.length - 1 && !words[i + 1].isBlank()) {
                        int nextWordId = getOrInsertWord(conn, words[i + 1]);
                        insertBigram(conn, wordId, nextWordId);
                    }
                }
            }

            System.out.println("✅ Imported lines: " + count);

        } catch (Exception e) {
            System.out.println("❌ IMPORT ERROR:");
            e.printStackTrace();
        }
    }
    //
    private static int getOrInsertWord(Connection conn, String word) throws SQLException {
        String select = "SELECT id FROM word WHERE word_text = ?";
        PreparedStatement stmt = conn.prepareStatement(select);
        stmt.setString(1, word);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) return rs.getInt("id");

        String insert = "INSERT INTO word (word_text, total_count) VALUES (?,1)";
        PreparedStatement ins = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
        ins.setString(1, word);
        ins.executeUpdate();

        ResultSet keys = ins.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }

    private static void insertBigram(Connection conn, int wordId, int nextWordId) throws SQLException {
        String query = """
            INSERT INTO word_follows (word_id, next_word_id, follow_count)
            VALUES (?, ?, 1)
            ON DUPLICATE KEY UPDATE follow_count = follow_count + 1
        """;

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, wordId);
        stmt.setInt(2, nextWordId);
        stmt.executeUpdate();
    }
}