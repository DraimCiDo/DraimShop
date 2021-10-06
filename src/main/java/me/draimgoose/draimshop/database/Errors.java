package me.draimgoose.draimshop.database;

public class Errors {
    public static String sqlConnectionExecute() {
        return "Не удалось выполнить инструкцию SQL: ";
    }

    public static String sqlConnectionClose() {
        return "Не удалось закрыть соединение SQL: ";
    }
}
