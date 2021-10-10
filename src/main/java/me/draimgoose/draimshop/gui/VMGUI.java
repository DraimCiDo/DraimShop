package me.draimgoose.draimshop.gui;

import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.MsgUtils;
import me.draimgoose.draimshop.utils.MsgUtils.MSG;
import me.draimgoose.draimshop.utils.UIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VMGUI extends ShopGUI {
    private Inventory inventoryView;
    private Inventory inventory;
    private ShulkerBox sourceImage;
    private HashMap<ItemStack, Double> prices;

    public VMGUI(ArmorStand armorStand, Player player) {
        super(player, armorStand, armorStand.getEquipment().getChestplate().getItemMeta().getDisplayName());
        ItemStack block = armorStand.getEquipment().getChestplate();
        BlockStateMeta blockMeta = (BlockStateMeta) block.getItemMeta();
        this.sourceImage = (ShulkerBox) blockMeta.getBlockState();

        inventoryView = Bukkit.createInventory(null, 9 * 4, LangUtils.getString("vending-machine-customer"));
        inventory = Bukkit.createInventory(null, 9 * 3, LangUtils.getString("vending-machine-owner"));

        int[] blackSlots = new int[] { 0, 1, 2, 3, 5, 6, 7, 8 };
        for (int i : blackSlots) {
            UIUtils.createItem(inventoryView, 3, i, Material.BLACK_STAINED_GLASS_PANE, 1, " ");
        }
        UIUtils.createItem(inventoryView, 3, 4, Material.BARRIER, 1, "§c" + LangUtils.getString("icons.close"));

        prices = this.stringListToDoubleArray(blockMeta.getLore(), sourceImage.getInventory());

        Inventory shulkerContent = sourceImage.getInventory();
        for (int i = 0; i < shulkerContent.getSize(); i++) {
            ItemStack item = shulkerContent.getItem(i);
            if (item != null) {
                ItemStack key = item.clone();
                key.setAmount(1);
                inventoryView.setItem(i, UIUtils.setPriceTag(item, prices.get(key)));
                inventory.setItem(i, item);
            }
        }
    }

    public ItemStack getItem(int index) {
        return this.inventory.getItem(index).clone();
    }

    private HashMap<ItemStack, Double> stringListToDoubleArray(List<String> lore, Inventory inventory) {
        HashMap<ItemStack, Double> result = new HashMap<>();
        for (int i = 0; i < lore.size(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                item = item.clone();
                item.setAmount(1);
                if (!result.containsKey(item)) {
                    result.put(item, Double.parseDouble(lore.get(i)));
                }
            }
        }
        return result;
    }

    public List<String> doubleToStringList() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < this.inventory.getSize(); i++) {
            ItemStack item = this.inventory.getItem(i);
            if (item != null) {
                ItemStack key = item.clone();
                key.setAmount(1);
                Double price = this.prices.get(key);
                price = price == null ? 0.0 : price;
                result.add(price.toString());
            } else {
                result.add("0.0");
            }
        }
        return result;
    }

    @Override
    public void saveInventories() {
        if (armorStand != null) {
            for (int i = 0; i < 27; i++) {
                sourceImage.getInventory().setItem(i, inventory.getItem(i));
            }
            ItemStack container = armorStand.getEquipment().getChestplate();
            BlockStateMeta shulkerMeta = (BlockStateMeta) container.getItemMeta();
            shulkerMeta.setLore(this.doubleToStringList());
            shulkerMeta.setBlockState(sourceImage);
            container.setItemMeta(shulkerMeta);
            armorStand.getEquipment().setChestplate(container);
        }
    }

    @Override
    public void purchaseItem(ItemStack item, int amount) {
        if (item == null) {
            viewer.sendMessage("§cПредмет не найден...");
            return;
        } else if (!inventory.containsAtLeast(item, amount) && !this.isAdmin) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-fail-item"), ownerID,
                    viewer, 0, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return;
        }

        ItemStack key = item.clone();
        key.setAmount(1);
        double totalCost = amount * prices.get(key);

        Inventory pInventory = viewer.getInventory();
        if (!UIUtils.hasSpace(pInventory, item, amount)) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-fail-space"), ownerID,
                    viewer, totalCost, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
        } else if (super.ownerSell(amount, totalCost, item)) {
            item.setAmount(amount);
            ItemStack temp = item.clone();
            List<ItemStack> stacks = new ArrayList<>();
            int stackNumber = 0;
            int currentAmount = amount;
            while (currentAmount > item.getMaxStackSize()) {
                ItemStack clone = item.clone();
                clone.setAmount(item.getMaxStackSize());
                stacks.add(clone);
                currentAmount -= item.getMaxStackSize();
                stackNumber++;
            }
            if (currentAmount != 0) {
                item.setAmount(currentAmount);
                stacks.add(item);
                stackNumber++;
            }
            pInventory.addItem(stacks.toArray(new ItemStack[stackNumber]));
            if (!this.isAdmin) {
                inventory.removeItem(temp);
            }
        }
    }

    @Override
    public String listPrice(ItemStack item, double price) {
        if (item == null || item.getType() == Material.AIR) {
            return LangUtils.getString("price-convo-failed-no-item");
        } else {
            ItemStack key = item.clone();
            key.setAmount(1);
            prices.put(key, price);
            return String.format(LangUtils.getString("price-convo-success"),
                    MsgUtils.getReadablePriceTag(price));
        }
    }

    @Override
    public void openUI() {
        viewer.playSound(armorStand.getLocation(), Sound.BLOCK_BARREL_OPEN, 0.5F, 1.0F);
        Bukkit.getScheduler().runTaskLater(DraimShop.getPlugin(), () -> this.viewer.openInventory(inventoryView), 2);
        this.interactingInventory = inventoryView;
        this.isOwnerView = false;
    }

    @Override
    public void openOwnerUI() {
        viewer.playSound(armorStand.getLocation(), Sound.BLOCK_BARREL_OPEN, 0.5F, 1.0F);
        this.viewer.openInventory(inventory);
        this.interactingInventory = inventory;
        this.isOwnerView = true;
    }
}
