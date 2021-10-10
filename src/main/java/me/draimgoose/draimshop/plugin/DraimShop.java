package me.draimgoose.draimshop.plugin;

import me.draimgoose.draimshop.crate.UnlockShop;
import me.draimgoose.draimshop.database.DB;
import me.draimgoose.draimshop.database.SQLite;
import me.draimgoose.draimshop.gui.CreationGUI;
import me.draimgoose.draimshop.player.*;
import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import me.draimgoose.draimshop.shop.ShopCreation;
import me.draimgoose.draimshop.shop.ShopExit;
import me.draimgoose.draimshop.shop.ShopOpening;
import me.draimgoose.draimshop.shop.ShopRemoval;
import me.draimgoose.draimshop.shop.briefcase.BCInteractInv;
import me.draimgoose.draimshop.shop.briefcase.BCListItem;
import me.draimgoose.draimshop.shop.vm.VMInteractInv;
import me.draimgoose.draimshop.shop.vm.VMListItem;
import me.draimgoose.draimshop.utils.LangUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class DraimShop extends JavaPlugin {
    private static DraimShop pluginInstance;
    private Economy economy;
    private DB database;
    private ExternalPluginsSupport support;

    @Override
    public void onEnable() {
        pluginInstance = this;

        if (!setUpEco()) {
            DraimShopLogger.sendMessage("Подхват Vault не сработал! Отключение плагина...", LVL.FAIL);
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            DraimShopLogger.sendMessage("Успешный подхват Vault.", LVL.SUCCESS);
        }

        this.support = new ExternalPluginsSupport(this);
        this.support.init();

        if (!this.getDataFolder().exists()) {
            try {
                this.getDataFolder().mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ShopOpening(), this);
        pluginManager.registerEvents(new ShopExit(), this);
        pluginManager.registerEvents(new VMInteractInv(), this);
        pluginManager.registerEvents(new VMListItem(), this);
        pluginManager.registerEvents(new BCInteractInv(), this);
        pluginManager.registerEvents(new BCListItem(), this);
        pluginManager.registerEvents(new ShopCreation(), this);
        pluginManager.registerEvents(new ShopRemoval(), this);
        pluginManager.registerEvents(new UnlockShop(), this);
        pluginManager.registerEvents(new PlayerTeleport(), this);
        pluginManager.registerEvents(new PlayerMove(), this);
        pluginManager.registerEvents(new PlayerLeave(), this);
        pluginManager.registerEvents(new PlayerJoin(), this);
        PluginCommand mainCommand = getCommand("draimshop");
        mainCommand.setExecutor(new DSComdExec());
        mainCommand.setTabCompleter(new AutoComplete());

        this.database = new SQLite(this);
        this.database.load();

        saveDefaultConfig();
        LangUtils.loadLangConfig();
        CreationGUI.initialize();
    }

    @Override
    public void onDisable() {
        PlayerState.clearAllShopInteractions();
        super.onDisable();
    }

    private boolean setUpEco() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                economy = rsp.getProvider();
                return economy != null;
            }
        }
    }

    public static DraimShop getPlugin() {
        return pluginInstance;
    }

    public Economy getEco() {
        return this.economy;
    }

    public DB getDB() {
        return this.database;
    }

    public ExternalPluginsSupport support() {
        return this.support;
    }
}
