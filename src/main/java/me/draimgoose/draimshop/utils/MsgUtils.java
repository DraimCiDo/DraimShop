package me.draimgoose.draimshop.utils;

import me.draimgoose.draimshop.plugin.DraimShop;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.regex.Matcher;

public class MsgUtils {
    private MsgUtils() {
    }

    public static class MSG {
        private String msg;
        private String itemName;
        private boolean hasDisplayName;

        public MSG(String message, String itemName, boolean hasDisplayName) {
            this.msg = message;
            this.itemName = itemName;
            this.hasDisplayName = hasDisplayName;
        }

        public String getMSG() {
            return this.msg;
        }

        public String getItemName() {
            return this.itemName;
        }

        public boolean hasDisplayName() {
            return this.hasDisplayName;
        }
    }

    public static MSG getMSG(String msg, String ownerID, OfflinePlayer viewer, double total, String itemName, boolean hasDisplayName, int amount) {
        String rawMSG = convertMSG(msg, ownerID, viewer, total, amount);
        return new MSG(rawMSG, itemName, hasDisplayName);
    }

    public static MSG getMSG(String msg, String ownerID, OfflinePlayer viewer, double total, ItemStack item, int amount) {
        String rawMSG = convertMSG(msg, ownerID, viewer, total, amount);
        if (item == null) {
            return new MSG(rawMSG, null, false);
        } else {
            ItemMeta meta = item.getItemMeta();
            String itemName = meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString();
            return new MSG(rawMSG, itemName, meta.hasDisplayName());
        }
    }

    private static String convertMSG(String msg, String ownerID, OfflinePlayer viewer, double total, int amount) {
        msg = msg.replaceAll("\\{%customer%\\}", viewer == null ? "" : viewer.getName());
        msg = msg.replaceAll("\\{%owner%\\}", Bukkit.getOfflinePlayer(UUID.fromString(ownerID)).getName());
        msg = msg.replaceAll("\\{%total%\\}", Matcher.quoteReplacement(getReadablePriceTag(total)));
        msg = msg.replaceAll("\\{%amount%\\}", "" + amount);
        return msg;
    }

    public static String getReadablePriceTag(double number) {
        return DraimShop.getPlugin().getEco().format(number);
    }

    @Deprecated
    public static String convertMSG(String msg, String ownerID, Player viewer, double total, ItemStack item, int amount) {
        ItemMeta meta = item.getItemMeta();
        String itemName = item == null ? "" : meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString();
        return convertMSG(msg, ownerID, viewer, total, itemName, amount);
    }

    @Deprecated
    public static String convertSG(String msg, String ownerID, OfflinePlayer viewer, double total, String itemName, int amount) {
        msg = msg.replaceAll("\\{%customer%\\}", viewer == null ? "" : viewer.getName());
        msg = msg.replaceAll("\\{%owner%\\}", Bukkit.getOfflinePlayer(UUID.fromString(ownerID)).getName());
        msg = msg.replaceAll("\\{%total%\\}", Matcher.quoteReplacement(getReadablePriceTag(total)));
        msg = msg.replaceAll("\\{%item%\\}", itemName == null ? "" : itemName);
        msg = msg.replaceAll("\\{%amount%\\}", "" + amount);
        return msg;
    }

    @Deprecated
    public static String getHumanReadableNumber(double number) {
        if (number >= 1000000000) {
            return String.format("%.2fB", number / 1000000000.0);
        }
        if (number >= 1000000) {
            return String.format("%.2fM", number / 1000000.0);
        }
        if (number >= 1000) {
            return String.format("%.2fK", number / 1000.0);
        }
        return String.valueOf(number);
    }
}
