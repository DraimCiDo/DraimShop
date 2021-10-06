package me.draimgoose.draimshop.plugin;

import me.draimgoose.draimshop.database.DB;
import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class DraimShop extends JavaPlugin {
    private static DraimShop plInst;
    private static DB db;
    private Economy eco;
    private ExternalPluginsSupport support;

    @Override
    public void onEnable() {
        plInst = this;
        if (!setUpEco()) {
            DraimShopLogger.sendMSG("Опа, а где Vault сука? Выключаю твою тарахтелку..", LVL.FAIL);
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            DraimShopLogger.sendMSG("Схватил за яички Vault", LVL.SUCCESS);
        }

        this.support = new ExternalPluginsSupport(this);
        this.support.init();

    }

    @Override
    public void onDisable() {
    }

    private boolean setUpEco() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                eco = rsp.getProvider();
                return eco != null;
            }
        }
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
