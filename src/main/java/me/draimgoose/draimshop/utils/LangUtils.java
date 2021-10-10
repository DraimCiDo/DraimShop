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
    private static String language;
    private static File languageFile;
    private static FileConfiguration languageConfiguration;

    public static void loadLangConfig() {
        Plugin plugin = DraimShop.getPlugin();
        language = plugin.getConfig().getString("lang");

        File languageFolder = new File(plugin.getDataFolder(), "lang");
        File tempResource = new File(plugin.getDataFolder(), language + ".yml");

        languageFile = new File(languageFolder, language + ".yml");
        languageFile.getParentFile().mkdirs();

        if (plugin.getResource(language + ".yml") != null) {
            plugin.saveResource(language + ".yml", true);
            tempResource.renameTo(languageFile);
        }

        languageConfiguration = new YamlConfiguration();
        try {
            languageConfiguration.load(languageFile);
            DraimShopLogger.sendMessage("Используется язык: " + language, LVL.INFO);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String getString(String node) {
        return languageConfiguration.getString(node);
    }
}
