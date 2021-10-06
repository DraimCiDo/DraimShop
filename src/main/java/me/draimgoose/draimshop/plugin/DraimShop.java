package me.draimgoose.draimshop.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class DraimShop extends JavaPlugin {
    private static DraimShop plInst;

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
}
