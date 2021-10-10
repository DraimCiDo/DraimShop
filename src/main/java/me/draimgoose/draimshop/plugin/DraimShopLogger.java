package me.draimgoose.draimshop.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class DraimShopLogger {
    public enum LVL {
        FAIL(ChatColor.RED), INFO(ChatColor.AQUA), WARN(ChatColor.YELLOW), SUCCESS(ChatColor.GREEN);

        private ChatColor color;

        LVL(ChatColor color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return this.color.toString();
        }
    }

    private static ConsoleCommandSender getLogger() {
        return DraimShop.getPlugin().getServer().getConsoleSender();
    }

    public static void sendMessage(String message, LVL type) {
        getLogger().sendMessage(type + "[DraimShop] " + message);
    }
}
