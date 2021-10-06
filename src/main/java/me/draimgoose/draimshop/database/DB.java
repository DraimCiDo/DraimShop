package me.draimgoose.draimshop.database;

import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.plugin.DraimShopLogger;
import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.MsgUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class DB {
    DraimShop plugin;
    Connection connection;
    static String dbname = "player_data";
    static String totalShopOwned = "total_shops_owned";
    static String shopsUnlocked = "shops_unlocked";
    static String pendingTransactions = "pending_transaction_messages";

    public DB(DraimShop instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize() {
        connection = getSQLConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + totalShopOwned);
            ResultSet rs = ps.executeQuery();
            close(ps, rs);
            ps = connection.prepareStatement("SELECT * FROM " + shopsUnlocked);
            rs = ps.executeQuery();
            close(ps, rs);
            ps = connection.prepareStatement("SELECT * FROM " + pendingTransactions);
            rs = ps.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();
            close(ps, rs);
            if (columnCount <= 6) {
                DraimShopLogger.sendMSG(
                        "Отсутствует обязательная колонка в " + pendingTransactions + " таблица, воссоздание таблицы...", LVL.WARN);
                ps = connection.prepareStatement("DROP table " + pendingTransactions);
                ps.executeUpdate();
                close(ps, connection);
                this.load();
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось восстановить соединение", ex);
        }
    }

    public Integer getTotalShopOwned(UUID playerID) {
        String string = playerID.toString();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Integer result = 0;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + totalShopOwned + " WHERE player = '" + string + "';");
            rs = ps.executeQuery();
            if (rs.next())
                result = rs.getInt("total_shops_owned");
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
        }
        return result;
    }

    public void setShopsOwned(UUID playerID, int number) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + totalShopOwned + " (player,total_shops_owned) VALUES('"
                    + playerID.toString() + "', " + number + ")");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
        }
    }

    public void decrementTotalShopsOwned(UUID playerID) {
        Integer previousTotal = getTotalShopOwned(playerID);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + totalShopOwned + " (player,total_shops_owned) VALUES('"
                    + playerID.toString() + "', " + (previousTotal - 1) + ")");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
        }
    }

    public void incrementTotalShopsOwned(UUID playerID) {
        Integer previousTotal = getTotalShopOwned(playerID);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + totalShopOwned + " (player,total_shops_owned) VALUES('"
                    + playerID.toString() + "', " + (previousTotal + 1) + ")");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
        }
    }

    public void setUnlockedShops(Player player, List<Integer> unlockedShops) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(
                    "DELETE FROM " + shopsUnlocked + " WHERE player = '" + player.getUniqueId().toString() + "';");
            ps.executeUpdate();
            for (Integer e : unlockedShops) {
                ps = conn.prepareStatement("INSERT INTO " + shopsUnlocked + " (player,shops_unlocked) VALUES('"
                        + player.getUniqueId().toString() + "'," + e + ");");
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
        }
    }

    public void storeMessage(String ownerID, Player customer, boolean selling, ItemStack item, int amount, double totalCost) {
        Connection conn = null;
        PreparedStatement ps = null;
        int sell = selling ? 1 : 0;
        ItemMeta meta = item.getItemMeta();
        int hasDisplayName = meta.hasDisplayName() ? 1 : 0;
        String itemName = meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString();

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO " + pendingTransactions
                    + " (player,customer,selling,item_name,has_display_name,amount,total_cost) VALUES('" + ownerID
                    + "', '" + customer.getUniqueId().toString() + "'," + sell + ", '" + itemName + "', "
                    + hasDisplayName + ", " + amount + ", " + totalCost + ");");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
        }
    }

    public List<MsgUtils.MSG> getMessages(String ownerID) {
        List<MsgUtils.MSG> messages = new ArrayList<>();
        String sellMessage = LangUtils.getString("customer-buy-success-owner");
        String buyMessage = LangUtils.getString("customer-sell-success-owner");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + pendingTransactions + " WHERE player = '" + ownerID + "';");
            rs = ps.executeQuery();
            while (rs.next()) {
                OfflinePlayer customer = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("customer")));
                String itemName = rs.getString("item_name");
                int amount = rs.getInt("amount");
                double totalCost = rs.getDouble("total_cost");
                boolean hasDisplayName = rs.getBoolean("has_display_name");
                MsgUtils.MSG message;
                if (rs.getBoolean("selling")) {
                    message = MsgUtils.getMSG(sellMessage, ownerID, customer, totalCost, itemName,
                            hasDisplayName, amount);
                } else {
                    message = MsgUtils.getMSG(buyMessage, ownerID, customer, totalCost, itemName,
                            hasDisplayName, amount);
                }
                messages.add(message);
            }
            close(ps, rs);
            ps = conn.prepareStatement("DELETE FROM " + pendingTransactions + " WHERE player = '" + ownerID + "';");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
        }
        return messages;
    }

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
        }
    }

    public void close(PreparedStatement ps, Connection conn) {
        try {
            if (ps != null)
                ps.close();
            if (conn != null)
                conn.close();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
        }
    }
}
