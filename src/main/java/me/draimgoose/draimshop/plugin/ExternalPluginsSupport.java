package me.draimgoose.draimshop.plugin;

import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import me.draimgoose.draimshop.utils.MsgUtils.MSG;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import me.pikamug.localelib.LocaleLib;
import me.pikamug.localelib.LocaleManager;
import org.bukkit.scheduler.BukkitRunnable;

public class ExternalPluginsSupport {
    private DraimShop pl;
    private final String[] customItemsPl = new String[] { "ItemsAdder" };
    private final String[] externalLib = new String[] { "LocaleLib" };

    public ExternalPluginsSupport(DraimShop pl) {
        this.pl = pl;
    }

    public void init() {
        for (String pl : customItemsPl) {
            if (this.has(pl))
                DraimShopLogger.sendMSG("Успешено взял за яички" + pl, LVL.SUCCESS);
        }
        for (String pl : externalLib) {
            if (this.has(pl))
                DraimShopLogger.sendMSG("Успешено взял за яички " + pl, LVL.SUCCESS);
        }
    }

    private boolean has(String plName) {
        return this.pl.getServer().getPluginManager().getPlugin(plName) != null;
    }

    public void sendMSG(Player player, MSG msg) {
        if (msg.hasDisplayName()) {
            player.sendMessage(msg.getMSG().replaceAll("\\{%item%\\}", msg.getItemName()));
        } else if (msg.getItemName() == null) {
            player.sendMessage(msg.getMSG().replaceAll("\\{%item%\\}", ""));
        } else if (!has("LocaleLib")) {
            String itemName = WordUtils.capitalize(msg.getItemName().toLowerCase().replaceAll("_", " "));
            player.sendMessage(msg.getMSG().replaceAll("\\{%item%\\}", itemName));
        } else {
            LocaleManager localeManager = ((LocaleLib) this.pl.getServer().getPluginManager()
                    .getPlugin("LocaleLib")).getLocaleManager();
            String rawMessage = msg.getMSG().replaceAll("\\{%item%\\}", "<item>");
            new BukkitRunnable() {
                @Override
                public void run() {
                    localeManager.sendMessage(player, rawMessage, Material.matchMaterial(msg.getItemName()),
                            (short) 0, null);
                }
            }.runTask(this.pl);
        }
    }
}
