package me.draimgoose.draimshop.plugin;

import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;

public class ExternalPluginsSupport {
    private DraimShop pl;
    private final String[] customItemsPl = new String[] { "ItemsAdder" };

    public ExternalPluginsSupport(DraimShop pl) {
        this.pl = pl;
    }

    public void init() {
        for (String pl : customItemsPl) {
            if (this.has(pl))
                DraimShopLogger.sendMSG("Успешено взял за яички" + pl, LVL.SUCCESS);
        }
    }

    private boolean has(String plName) {
        return this.pl.getServer().getPluginManager().getPlugin(plName) != null;
    }
}
