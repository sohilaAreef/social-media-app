package com.socialmedia.app;

import com.socialmedia.config.DatabaseConfig;

import java.sql.ResultSet;
import java.sql.Statement;

public class DbPing {
    public static void main(String[] args) {
        try (var con = DatabaseConfig.getConnection()) {
            System.out.println("✅ Connected to: " + con.getMetaData().getURL());

            // اختبار بسيط: هات عدد اليوزرز
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM user")) {

                if (rs.next()) {
                    System.out.println("✅ users count = " + rs.getInt("cnt"));
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Connection failed");
            e.printStackTrace();
        }
    }
}