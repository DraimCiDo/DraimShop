package me.draimgoose.draimshop.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class UIUtils {
    private UIUtils() {
    }

    public static ItemStack createItem(Inventory inv, int row, int column, Material material, int amount,
                                       String displayName, String... lore) {
        ItemStack item = new ItemStack(material, amount);
        List<String> loreStrings = new ArrayList<>();
        for (String s : lore) {
            loreStrings.add(s);
        }
        int invSlot = 9 * row + column;

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(loreStrings);
        item.setItemMeta(meta);
        inv.setItem(invSlot, item);
        return item;
    }

    public static ItemStack createItem(Inventory inv, int slot, Material material, int amount, String displayName,
                                       String... lore) {
        ItemStack item = new ItemStack(material, amount);
        List<String> loreStrings = new ArrayList<>();
        for (String s : lore) {
            loreStrings.add(s);
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(loreStrings);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
        return item;
    }

    public static ItemStack createItem(Inventory inv, int slot, Material material, int amount, int customModelID,
                                       String displayName, String... lore) {
        ItemStack item = new ItemStack(material, amount);
        List<String> loreStrings = new ArrayList<>();
        for (String s : lore) {
            loreStrings.add(s);
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(loreStrings);
        meta.setCustomModelData(customModelID);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
        return item;
    }

    public static ItemStack loreItem(ItemStack item, String... lore) {
        ItemStack clone = item.clone();
        ItemMeta meta = item.getItemMeta();
        List<String> loreStrings = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        for (String s : lore) {
            loreStrings.add(s);
        }
        meta.setLore(loreStrings);
        clone.setItemMeta(meta);
        return clone;
    }

    public static ItemStack setPriceTag(ItemStack item, double price) {
        return item == null ? null
                : loreItem(item, "§7--------------------", "§5" + LangUtils.getString("price-tag.price") + ": §e"
                + MsgUtils.getReadablePriceTag(price));
    }

    public static ItemStack setPriceTag(ItemStack item, double price, boolean selling, boolean isAdmin, int stock) {
        String[] additionalLore = new String[] { "§7--------------------",
                "§5" + LangUtils.getString("price-tag.stock") + ": §e"
                        + (isAdmin ? LangUtils.getString("price-tag.unlimited")
                        : String.format("%,.0f", Double.valueOf(stock))),
                "§5" + (selling ? LangUtils.getString("price-tag.selling")
                        : LangUtils.getString("price-tag.buying")),
                "§5" + LangUtils.getString("price-tag.price") + ": §e" + MsgUtils.getReadablePriceTag(price) };
        return item == null ? null : loreItem(item, additionalLore);
    }

    public static boolean hasSpace(Inventory inventory, ItemStack item, int amount) {
        int totalSpace = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack pItem = inventory.getItem(i);
            if (pItem == null) {
                totalSpace += item.getMaxStackSize();
            } else if (pItem.isSimilar(item)) {
                totalSpace += pItem.getMaxStackSize() - pItem.getAmount();
            }
        }
        return totalSpace >= amount;
    }
}
