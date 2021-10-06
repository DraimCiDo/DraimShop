package me.draimgoose.draimshop.plugin;

import me.draimgoose.draimshop.database.DB;
import org.bukkit.plugin.java.JavaPlugin;

public final class DraimShop extends JavaPlugin {
    private static DraimShop plInst;
    private static DB db;

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
}
