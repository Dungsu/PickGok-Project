package com.pickgok.common;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/pick_gok?serverTimezone=UTC&useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root"; // 또는 music_dev
    private static final String PASS = "0000"; // ★ 최종 비밀번호 확인!

    public static Connection getConnection() throws Exception {
        Class.forName(DRIVER);
        return DriverManager.getConnection(URL, USER, PASS);
    }
}