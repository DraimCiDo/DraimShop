package me.draimgoose.draimshop.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import me.draimgoose.draimshop.plugin.DraimShop;

public class SQLite extends DB {
    private String SQLiteCreateTotalShopsOwnedTable = "CREATE TABLE IF NOT EXISTS " + totalShopOwned
            + " (`player` varchar(36) NOT NULL, `total_shops_owned` INTEGER NOT NULL, PRIMARY KEY (`player`));";
    private String SQLiteCreateShopsUnlockedTable = "CREATE TABLE IF NOT EXISTS " + shopsUnlocked
            + " (`player` varchar(36) NOT NULL, `shops_unlocked` INTEGER NOT NULL);";
    private String SQLiteCreatePendingTransactionMessagesTable = "CREATE TABLE IF NOT EXISTS " + pendingTransactions
            + " (`player` varchar(36) NOT NULL, `customer` varchar(36) NOT NULL, `selling` INTEGER NOT NULL, "
            + "`item_name` TEXT NOT NULL, `has_display_name` INTEGER NOT NULL, `amount` INTEGER NOT NULL, "
            + "`total_cost` REAL NOT NULL);";

    public SQLite(DraimShop instance) {
        super(instance);
    }

    @Override
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка записи файла: " + dbname + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Исключение SQLite при инициализации", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "Вам нужна библиотека JDBC SQLite. Погугли это. Поместите его в папку /lib.");
        }
        return null;
    }

    @Override
    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTotalShopsOwnedTable);
            s.executeUpdate(SQLiteCreateShopsUnlockedTable);
            s.executeUpdate(SQLiteCreatePendingTransactionMessagesTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}
