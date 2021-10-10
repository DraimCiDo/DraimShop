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
        private String message;
        private String itemName;
        private boolean hasDisplayName;

        public MSG(String message, String itemName, boolean hasDisplayName) {
            this.message = message;
            this.itemName = itemName;
            this.hasDisplayName = hasDisplayName;
        }

        public String getMessage() {
            return this.message;
        }

        public String getItemName() {
            return this.itemName;
        }

        public boolean hasDisplayName() {
            return this.hasDisplayName;
        }
    }

    public static MSG getMessage(String message, String ownerID, OfflinePlayer viewer, double total,
                                     String itemName, boolean hasDisplayName, int amount) {
        String rawMessage = convertMessage(message, ownerID, viewer, total, amount);
        return new MSG(rawMessage, itemName, hasDisplayName);
    }

    public static MSG getMessage(String message, String ownerID, OfflinePlayer viewer, double total, ItemStack item,
                                     int amount) {
        String rawMessage = convertMessage(message, ownerID, viewer, total, amount);
        if (item == null) {
            return new MSG(rawMessage, null, false);
        } else {
            ItemMeta meta = item.getItemMeta();
            String itemName = meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString();
            return new MSG(rawMessage, itemName, meta.hasDisplayName());
        }
    }

    private static String convertMessage(String message, String ownerID, OfflinePlayer viewer, double total,
                                         int amount) {
        message = message.replaceAll("\\{%customer%\\}", viewer == null ? "" : viewer.getName());
        message = message.replaceAll("\\{%owner%\\}", Bukkit.getOfflinePlayer(UUID.fromString(ownerID)).getName());
        message = message.replaceAll("\\{%total%\\}", Matcher.quoteReplacement(getReadablePriceTag(total)));
        message = message.replaceAll("\\{%amount%\\}", "" + amount);
        return message;
    }

    public static String getReadablePriceTag(double number) {
        return DraimShop.getPlugin().getEco().format(number);
    }

    @Deprecated
    public static String convertMessage(String message, String ownerID, Player viewer, double total, ItemStack item,
                                        int amount) {
        ItemMeta meta = item.getItemMeta();
        String itemName = item == null ? "" : meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString();
        return convertMessage(message, ownerID, viewer, total, itemName, amount);
    }
    @Deprecated
    public static String convertMessage(String message, String ownerID, OfflinePlayer viewer, double total,
                                        String itemName, int amount) {
        message = message.replaceAll("\\{%customer%\\}", viewer == null ? "" : viewer.getName());
        message = message.replaceAll("\\{%owner%\\}", Bukkit.getOfflinePlayer(UUID.fromString(ownerID)).getName());
        message = message.replaceAll("\\{%total%\\}", Matcher.quoteReplacement(getReadablePriceTag(total)));
        message = message.replaceAll("\\{%item%\\}", itemName == null ? "" : itemName);
        message = message.replaceAll("\\{%amount%\\}", "" + amount);
        return message;
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
