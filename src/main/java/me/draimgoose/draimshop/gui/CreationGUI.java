package me.draimgoose.draimshop.gui;

import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.plugin.DraimShopLogger;
import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.UIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CreationGUI {
    public static int noOfItems;
    public static LinkedList<String> names;
    public static LinkedList<Integer> modelData;
    private static int noOfPages;
    private static List<Integer> defaults;

    private List<Integer> unlockedShops;
    private Inventory[] pages;
    private int currentPage;
    private boolean isAdmin;
    private Player player;

    public CreationGUI(Player player, boolean isAdmin) {
        this.isAdmin = isAdmin;
        this.currentPage = 0;
        if (!isAdmin && !DraimShop.getPlugin().getConfig().getBoolean("unlock-all")) {
            unlockedShops = DraimShop.getPlugin().getDB().getUnlockedShops(player);
        }
        this.setUpGUI(player);
        this.player = player;
    }

    public static void initialize() {
        names = new LinkedList<>();
        modelData = new LinkedList<>();
        defaults = new ArrayList<>();

        int defaultVM = DraimShop.getPlugin().getConfig().getInt("defaults.vending-machine");
        Set<String> vm = DraimShop.getPlugin().getConfig().getConfigurationSection("vending-machine").getKeys(false);
        for (String e : vm) {
            String customModelName = DraimShop.getPlugin().getConfig().getString("vending-machine." + e + ".name");
            if (customModelName == null) {
                DraimShopLogger.sendMSG(
                        "Имя не установлено, по крайней мере для одного из торговых автоматов! Отключение плагина...", DraimShopLogger.LVL.FAIL);
                Bukkit.getPluginManager().disablePlugin(DraimShop.getPlugin());
            }
            names.add(customModelName);
            Integer customModelData = DraimShop.getPlugin().getConfig().getInt("vending-machine." + e + ".model-data");
            if (customModelData == 0) {
                DraimShopLogger.sendMSG(
                        "Отсутствуют CMD или установлено значение 0, по крайней мере для одного из торговых автоматов! Отключение плагина...",
                        DraimShopLogger.LVL.FAIL);
                Bukkit.getPluginManager().disablePlugin(DraimShop.getPlugin());
            }
            modelData.add(customModelData);
            defaults.add(defaultVM);
        }

        int defaultBriefcase = DraimShop.getPlugin().getConfig().getInt("defaults.briefcase");
        Set<String> bc = DraimShop.getPlugin().getConfig().getConfigurationSection("briefcase").getKeys(false);
        for (String e : bc) {
            String customModelName = DraimShop.getPlugin().getConfig().getString("briefcase." + e + ".name");
            if (customModelName == null) {
                DraimShopLogger.sendMSG("Имя не установлено, по крайней мере для одного из портфелей! Отключение плагина...",
                        DraimShopLogger.LVL.FAIL);
                Bukkit.getPluginManager().disablePlugin(DraimShop.getPlugin());
            }
            names.add(customModelName);
            Integer customModelData = DraimShop.getPlugin().getConfig().getInt("briefcase." + e + ".model-data");
            if (customModelData == 0) {
                DraimShopLogger.sendMSG(
                        "Отсутствуют CMD или установлено значение 0, по крайней мере для одного из портфелей! Отключение плагина...",
                        DraimShopLogger.LVL.FAIL);
                Bukkit.getPluginManager().disablePlugin(DraimShop.getPlugin());
            }
            modelData.add(customModelData);
            defaults.add(defaultBriefcase);
        }

        noOfItems = names.size();
        noOfPages = ((Double) Math.ceil(noOfItems / 27.0)).intValue();
    }

    public void setUpGUI(Player player) {
        @SuppressWarnings("unchecked")
        LinkedList<String> iterNames = (LinkedList<String>) names.clone();
        @SuppressWarnings("unchecked")
        LinkedList<Integer> iterModelData = (LinkedList<Integer>) modelData.clone();
        if (unlockedShops != null) {
            iterModelData.replaceAll(e -> unlockedShops.contains(e) ? e : getDefault(e));
        }
        pages = new Inventory[noOfPages];

        int item = 0;
        for (int i = 0; i < noOfPages; i++) {
            pages[i] = Bukkit.createInventory(null, 9 * 4, isAdmin ? LangUtils.getString("admin-shop-creation")
                    : LangUtils.getString("shop-creation"));

            int[] blackSlots = new int[] { 0, 1, 2, 6, 7, 8 };
            for (int j : blackSlots) {
                UIUtils.createItem(pages[i], 3, j, Material.BLACK_STAINED_GLASS_PANE, 1, " ");
            }
            UIUtils.createItem(pages[i], 3, 3, Material.ARROW, 1, "§e" + LangUtils.getString("icons.previous"));
            UIUtils.createItem(pages[i], 3, 4, Material.BARRIER, 1, "§c" + LangUtils.getString("icons.close"));
            UIUtils.createItem(pages[i], 3, 5, Material.ARROW, 1, "§e" + LangUtils.getString("icons.next"));

            HashMap<Integer, ItemStack> map = DraimShop.getPlugin().support().getModelDataToShopMapping();
            for (int j = 0; j < 27; j++) {
                if (i == noOfPages - 1 && item == noOfItems)
                    break;
                if (map != null) {
                    ItemStack itemsAdderItem = map.get(iterModelData.poll());
                    ItemMeta meta = itemsAdderItem.getItemMeta();
                    meta.setDisplayName(iterNames.poll());
                    itemsAdderItem.setItemMeta(meta);
                    pages[i].setItem(j, itemsAdderItem);
                } else {
                    UIUtils.createItem(pages[i], j, Material.PAPER, 1, iterModelData.poll(), iterNames.poll());
                }
                item++;
            }
        }
    }

    public Inventory currentInventory() {
        return this.pages[this.currentPage];
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public void openFirstPage() {
        currentPage = 0;
        Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.openInventory(pages[currentPage]));
    }

    public void nextPage() {
        if (currentPage != pages.length - 1) {
            currentPage++;
            Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> {
                player.openInventory(pages[currentPage]);
                PlayerState.getPlayerState(player).setCreationGUI(this);
            });
        }
    }

    public void previousPage() {
        if (currentPage != 0) {
            currentPage--;
            Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> {
                player.openInventory(pages[currentPage]);
                PlayerState.getPlayerState(player).setCreationGUI(this);
            });
        }
    }

    private static Integer getDefault(Integer model) {
        int index = modelData.indexOf(model);
        return defaults.get(index);
    }
}
