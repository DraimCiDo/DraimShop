package me.draimgoose.draimshop.plugin;

import dev.lone.itemsadder.api.CustomStack;
import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import me.draimgoose.draimshop.shop.briefcase.BCCreator;
import me.draimgoose.draimshop.shop.vm.VMCreator;
import me.draimgoose.draimshop.utils.MsgUtils.MSG;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import me.pikamug.localelib.LocaleLib;
import me.pikamug.localelib.LocaleManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Set;

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

    public HashMap<Integer, ItemStack> getModelDataToShopMapping() {
        if (!has("ItemsAdder"))
            return null;
        HashMap<Integer, ItemStack> map = new HashMap<>();

        Set<String> vm = this.pl.getConfig().getConfigurationSection("vending-machine").getKeys(false);
        for (String shop : vm) {
            Integer customModelData = this.pl.getConfig().getInt("vending-machine." + shop + ".model-data");
            ItemStack item = CustomStack.getInstance("draimshop:" + shop + "_vending_machine").getItemStack();
            map.put(customModelData, item);
        }
        Set<String> nb = this.pl.getConfig().getConfigurationSection("briefcase").getKeys(false);
        for (String shop : nb) {
            Integer customModelData = this.pl.getConfig().getInt("briefcase." + shop + ".model-data");
            ItemStack item = CustomStack.getInstance("draimshop:" + shop + "_briefcase").getItemStack();
            map.put(customModelData, item);
        }

        Integer defaultVM = this.pl.getConfig().getInt("defaults.vending-machine");
        ItemStack defaultVMItem = CustomStack.getInstance("draimshop:default_vending_machine").getItemStack();
        map.put(defaultVM, defaultVMItem);
        Integer defaultBriefcase = this.pl.getConfig().getInt("defaults.briefcase");
        ItemStack defaultBriefcaseItem = CustomStack.getInstance("draimshop:default_briefcase").getItemStack();
        map.put(defaultBriefcase, defaultBriefcaseItem);

        return map;
    }

    public Boolean isDefaultModel(ItemStack item) {
        if (!has("ItemsAdder"))
            return null;
        String id = CustomStack.byItemStack(item).getNamespacedID();
        return id.contains("default");
    }

    public Object getShopCreator(ItemStack item) {
        if (!has("ItemsAdder"))
            return null;
        String id = CustomStack.byItemStack(item).getNamespacedID();
        if (id.contains("vending_machine"))
            return new VMCreator();
        return new BCCreator();
    }
}
