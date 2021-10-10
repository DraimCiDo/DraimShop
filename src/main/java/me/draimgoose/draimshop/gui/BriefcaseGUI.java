package me.draimgoose.draimshop.gui;

import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.plugin.DraimShopLogger;
import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import me.draimgoose.draimshop.utils.MsgUtils.MSG;
import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.MsgUtils;
import me.draimgoose.draimshop.utils.UIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BriefcaseGUI extends ShopGUI {
    private Inventory normalView;
    private Inventory ownerView;
    private double price;
    private boolean selling;
    private int quantity;

    public BriefcaseGUI(ArmorStand armorStand, Player player) {
        super(player, armorStand, armorStand.getEquipment().getChestplate().getItemMeta().getDisplayName());
        EntityEquipment armorStandContent = armorStand.getEquipment();
        ItemStack item = armorStandContent.getLeggings();
        if (item != null && item.getType() != Material.AIR) {
            normalView = Bukkit.createInventory(null, 9 * 4, LangUtils.getString("briefcase-customer"));
            ownerView = Bukkit.createInventory(null, 9 * 4, LangUtils.getString("briefcase-owner"));

            ItemStack placeHolder = armorStandContent.getChestplate();
            List<String> info = placeHolder.getItemMeta().getLore();
            this.price = Double.parseDouble(info.get(0));
            this.quantity = Integer.parseInt(info.get(1));
            this.selling = Boolean.parseBoolean(info.get(2));

            int[] blackSlots = new int[] { 0, 1, 2, 3, 5, 6, 7, 8 };
            for (int i : blackSlots) {
                UIUtils.createItem(normalView, 3, i, Material.BLACK_STAINED_GLASS_PANE, 1, " ");
                UIUtils.createItem(ownerView, 3, i, Material.BLACK_STAINED_GLASS_PANE, 1, " ");
            }
            UIUtils.createItem(normalView, 3, 4, Material.BARRIER, 1, "§c" + LangUtils.getString("icons.close"));

            UIUtils.createItem(ownerView, 3, 2, Material.OAK_SIGN, 1,
                    "§6" + (this.selling ? LangUtils.getString("price-tag.selling")
                            : LangUtils.getString("price-tag.buying")),
                    "§2" + LangUtils.getString("icons.selling-status.lore"));
            UIUtils.createItem(ownerView, 3, 3, Material.NAME_TAG, 1,
                    "§6" + LangUtils.getString("icons.change-price.title"),
                    "§2" + LangUtils.getString("icons.change-price.lore"));
            UIUtils.createItem(ownerView, 3, 4, Material.HOPPER_MINECART, 1,
                    "§6" + LangUtils.getString("icons.add-items.title"),
                    "§2" + LangUtils.getString("icons.add-items.lore"));
            UIUtils.createItem(ownerView, 3, 5, Material.CHEST_MINECART, 1,
                    "§6" + LangUtils.getString("icons.retrieve-items.title"),
                    "§2" + LangUtils.getString("icons.retrieve-items.lore"));
            UIUtils.createItem(ownerView, 3, 6, Material.BARRIER, 1, "§c" + LangUtils.getString("icons.close"));

            normalView.setItem(13, UIUtils.setPriceTag(item, this.price, this.selling, this.isAdmin, this.quantity));
            ownerView.setItem(13, UIUtils.setPriceTag(item, this.price, this.selling, this.isAdmin, this.quantity));
        }
    }

    public ItemStack getItem() {
        return this.armorStand.getEquipment().getLeggings().clone();
    }

    public boolean hasItem() {
        ItemStack item = this.armorStand.getEquipment().getLeggings();
        return item != null && item.getType() != Material.AIR;
    }

    public String listPrice(ItemStack item, double price) {
        if (item == null || item.getType() == Material.AIR) {
            return LangUtils.getString("price-convo-failed-no-item");
        } else {
            EntityEquipment armorStandContent = this.armorStand.getEquipment();
            item.setAmount(1);
            armorStandContent.setLeggings(item);

            this.updatePlaceHolderLore(1, price);
            return String.format(LangUtils.getString("price-convo-success"),
                    MsgUtils.getReadablePriceTag(price));
        }
    }

    public void sellItem(ItemStack item, int amount) {
        if (item == null) {
            viewer.sendMessage("§cПредмет не найден...");
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(amount);
        Inventory pInventory = viewer.getInventory();

        if (!pInventory.containsAtLeast(item, amount)) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-sell-fail-item"), ownerID,
                    viewer, 0, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return;
        }

        int remainingSpace = Integer.MAX_VALUE - this.quantity;
        double totalCost = amount * price;
        if (remainingSpace < amount && !this.isAdmin) {
            viewer.sendMessage(String.format(LangUtils.getString("sell-convo-failed-limit"), remainingSpace));
        } else if (super.ownerBuy(amount, totalCost, item)) {
            if (!this.isAdmin) {
                this.updatePlaceHolderLore(2, this.quantity + amount);
            }
            pInventory.removeItem(clone);
        }
    }

    public boolean isSelling() {
        return this.selling;
    }

    public void setSelling(boolean selling) {
        this.selling = selling;
        this.updatePlaceHolderLore(3, selling);

        UIUtils.createItem(ownerView, 3, 2, Material.OAK_SIGN, 1,
                "§6" + (this.selling ? LangUtils.getString("price-tag.selling")
                        : LangUtils.getString("price-tag.buying")),
                "§2" + LangUtils.getString("icons.selling-status.lore"));

        ItemStack item = ownerView.getItem(13);
        ItemMeta itemMeta = item.getItemMeta();
        List<String> itemLore = itemMeta.getLore();
        itemLore.set(itemLore.size() - 2, "§5" + (this.selling ? LangUtils.getString("price-tag.selling")
                : LangUtils.getString("price-tag.buying")));
        itemMeta.setLore(itemLore);
        item.setItemMeta(itemMeta);
    }

    public void retrieveItem(int amount) {
        if (!this.hasItem()) {
            viewer.sendMessage("§cПредмет не найден...");
            return;
        }
        ItemStack item = this.getItem();
        if (this.quantity < amount) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-fail-item"), ownerID,
                    viewer, 0, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return;
        }
        Inventory pInventory = viewer.getInventory();

        if (!UIUtils.hasSpace(pInventory, item, amount)) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-fail-space"), ownerID,
                    viewer, 0, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
        } else {
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
            if (this.updatePlaceHolderLore(2, this.quantity - amount)
                    && pInventory.addItem(stacks.toArray(new ItemStack[stackNumber])).isEmpty())
                viewer.sendMessage(String.format(LangUtils.getString("retrieve-convo-success"), amount));
        }
    }

    public void addItem(int amount) {
        if (!this.hasItem()) {
            viewer.sendMessage("§cПредмет не найден...");
            return;
        }
        ItemStack item = this.getItem();
        item.setAmount(amount);
        Inventory pInventory = viewer.getInventory();

        if (!pInventory.containsAtLeast(item, amount)) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-sell-fail-item"), ownerID,
                    viewer, 0, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return;
        }

        int remainingSpace = Integer.MAX_VALUE - this.quantity;
        if (remainingSpace < amount) {
            viewer.sendMessage(String.format(LangUtils.getString("add-convo-failed-limit"), remainingSpace));
        } else {
            if (this.updatePlaceHolderLore(2, this.quantity + amount) && pInventory.removeItem(item).isEmpty())
                viewer.sendMessage(String.format(LangUtils.getString("add-convo-success"), amount));
        }
    }

    private boolean updatePlaceHolderLore(int index, Object property) {
        EntityEquipment armorStandContent = this.armorStand.getEquipment();
        ItemStack placeHolder = armorStandContent.getChestplate();
        if (placeHolder == null || placeHolder.getType() == Material.AIR) {
            DraimShopLogger.sendMessage("Портфель без Placheholder, обнаружен на " + this.armorStand.getLocation()
                    + ", не удалось обновить информацию о магазине. Сообщите об этой ошибке!", LVL.FAIL);
            return false;
        }
        ItemMeta meta = placeHolder.getItemMeta();
        if (!meta.hasLore()) {
            DraimShopLogger.sendMessage("Placeholder портфеля без лора был найден на "
                    + this.armorStand.getLocation() + ", не удалось обновить информацию о магазине. Сообщите об этой ошибке!", LVL.FAIL);
            return false;
        }
        List<String> lore = meta.getLore();
        if (lore.size() < 3) {
            DraimShopLogger.sendMessage("Placeholder портфеля с неполным лором, найден на "
                    + this.armorStand.getLocation() + ", не удалось обновить информацию о магазине. Сообщите об этой ошибке!", LVL.FAIL);
            return false;
        } else {
            lore.set(index - 1, property.toString());
            meta.setLore(lore);
            placeHolder.setItemMeta(meta);
            armorStandContent.setChestplate(placeHolder);
            return true;
        }
    }

    @Override
    public void purchaseItem(ItemStack item, int amount) {
        if (item == null) {
            viewer.sendMessage("§cПредмет не найден...");
            return;
        }
        if (this.quantity < amount && !this.isAdmin) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-fail-item"), ownerID,
                    viewer, 0, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return;
        }
        Inventory pInventory = viewer.getInventory();
        double totalCost = amount * price;

        if (!UIUtils.hasSpace(pInventory, item, amount)) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-fail-space"), ownerID,
                    viewer, totalCost, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
        } else if (super.ownerSell(amount, totalCost, item)) {
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
                this.updatePlaceHolderLore(2, this.quantity - amount);
            }
        }
    }

    @Override
    public void openUI() {
        if (normalView == null) {
            viewer.sendMessage(LangUtils.getString("briefcase-not-initialized"));
            Bukkit.getScheduler().runTask(DraimShop.getPlugin(),
                    () -> PlayerState.getPlayerState(viewer).clearShopInteractions());
        } else {
            viewer.playSound(armorStand.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5F, 1.0F);
            Bukkit.getScheduler().runTaskLater(DraimShop.getPlugin(), () -> this.viewer.openInventory(normalView), 2);
            this.interactingInventory = normalView;
            this.isOwnerView = false;
        }
    }

    @Override
    public void openOwnerUI() {
        if (ownerView == null) {
            viewer.sendMessage(LangUtils.getString("briefcase-not-initialized"));
            Bukkit.getScheduler().runTask(DraimShop.getPlugin(),
                    () -> PlayerState.getPlayerState(viewer).clearShopInteractions());
        } else {
            viewer.playSound(armorStand.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5F, 1.0F);
            this.viewer.openInventory(ownerView);
            this.interactingInventory = ownerView;
            this.isOwnerView = true;
        }
    }

    @Override
    public void saveInventories() {
    }
}
