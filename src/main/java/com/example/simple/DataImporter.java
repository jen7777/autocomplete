package com.example.simple;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;

public class DataImporter {

    public static void importTextFile(String path) {

        try (Connection conn = DBConnection.getConnection()) {

            if (conn == null) {
                System.out.println("DB connection failed. Import stopped.");
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            String prevWord = null;

            while ((line = br.readLine()) != null) {

                String[] words = line.toLowerCase().split("\\W+");

                for (String word : words) {
                    if (word.isEmpty()) continue;

                    int currentId = getOrInsertWord(conn, word);

                    if (prevWord != null) {
                        int prevId = getOrInsertWord(conn, prevWord);
                        insertFollow(conn, prevId, currentId);
                    }

                    prevWord = word;
                }
            }

            System.out.println("✅ Data successfully inserted into MySQL!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getOrInsertWord(Connection conn, String word) throws Exception {

        String select = "SELECT id FROM word WHERE word_text = ?";
        PreparedStatement stmt = conn.prepareStatement(select);
        stmt.setString(1, word);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }

        String insert = "INSERT INTO word (word_text) VALUES (?)";
        PreparedStatement insertStmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, word);
        insertStmt.executeUpdate();

        ResultSet keys = insertStmt.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }

    private static void insertFollow(Connection conn, int w1, int w2) throws Exception {

        String query = """
            INSERT INTO word_follows (word_id, next_word_id, follow_count)
            VALUES (?, ?, 1)
            ON DUPLICATE KEY UPDATE follow_count = follow_count + 1
        """;

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, w1);
        stmt.setInt(2, w2);
        stmt.executeUpdate();
    }
}
