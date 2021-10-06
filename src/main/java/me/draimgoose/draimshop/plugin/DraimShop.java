package me.draimgoose.draimshop.plugin;

import me.draimgoose.draimshop.database.DB;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

public final class DraimShop extends JavaPlugin {
    private static DraimShop plInst;
    private static DB db;
    private Economy eco;

    @Override
    public void onEnable() {
        plInst = this;

    }

    @Override
    public void onDisable() {

    }

    public static DraimShop getPlugin() {
        return plInst;
    }

    public DB getDB() {
        return this.db;
    }

    public Economy getEco() {
        return this.eco;
    }
}
