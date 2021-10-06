package me.draimgoose.draimshop.utils;

import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.plugin.DraimShopLogger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class LangUtils {
    private static String lang;
    private static File langFile;
    private static FileConfiguration langConfig;

    public static void loadLangConfig() {
        Plugin pl = DraimShop.getPlugin();
        lang = pl.getConfig().getString("lang");

        File langFolder = new File(pl.getDataFolder(), "lang");
        File tempResource = new File(pl.getDataFolder(), lang + ".yml");

        langFile = new File(langFolder, lang + ".yml");
        langFile.getParentFile().mkdirs();

        if (pl.getResource(lang + ".yml") != null) {
            pl.saveResource(lang + ".yml", true);
            tempResource.renameTo(langFile);
        }

        langConfig = new YamlConfiguration();
        try {
            langConfig.load(langFile);
            DraimShopLogger.sendMSG("Используется язык: " + lang, LVL.INFO.INFO);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String getString(String node) {
        return langConfig.getString(node);
    }

}
