package me.draimgoose.draimshop.gui;

import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.MsgUtils.MSG;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.MsgUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ShopGUI {
    protected final String ownerID;
    protected final ArmorStand armorStand;
    protected final Player viewer;
    protected final boolean isAdmin;
    protected Inventory interactingInventory;
    protected boolean isOwnerView;

    public ShopGUI(Player player, ArmorStand armorStand, String ownerID) {
        this.armorStand = armorStand;
        this.viewer = player;
        this.ownerID = ownerID;

        ItemStack adminItem = armorStand.getEquipment().getBoots();
        this.isAdmin = adminItem != null && adminItem.getType() != Material.AIR;
    }

    public ArmorStand getArmorStand() {
        return this.armorStand;
    }

    public boolean isOwner() {
        return this.viewer.getUniqueId().toString().equals(this.ownerID);
    }

    protected boolean ownerSell(int amount, double totalCost, ItemStack item) {
        Economy economy = DraimShop.getPlugin().getEco();
        double bal = economy.getBalance(viewer);

        if (bal < totalCost) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-fail-money"), ownerID,
                    viewer, totalCost, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return false;
        } else if (this.isAdmin) {
            economy.withdrawPlayer(viewer, totalCost);
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-success-customer"), ownerID,
                    viewer, totalCost, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return true;
        } else {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(this.ownerID));
            economy.withdrawPlayer(viewer, totalCost);
            economy.depositPlayer(owner, totalCost);
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-buy-success-customer"), ownerID,
                    viewer, totalCost, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);

            if (owner.isOnline()) {
                MSG ownerMessage = MsgUtils.getMessage(LangUtils.getString("customer-buy-success-owner"),
                        ownerID, viewer, totalCost, item, amount);
                DraimShop.getPlugin().support().sendMSG(owner.getPlayer(), ownerMessage);
            } else {
                CompletableFuture.runAsync(() -> DraimShop.getPlugin().getDB().storeMessage(ownerID, viewer,
                        true, item, amount, totalCost));
            }
            return true;
        }
    }

    protected boolean ownerBuy(int amount, double totalCost, ItemStack item) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(this.ownerID));
        Economy economy = DraimShop.getPlugin().getEco();
        double bal = economy.getBalance(owner);

        if (bal < totalCost) {
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-sell-fail-money"), ownerID,
                    viewer, totalCost, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return false;
        } else if (this.isAdmin) {
            economy.depositPlayer(viewer, totalCost);
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-sell-success-customer"),
                    ownerID, viewer, totalCost, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);
            return true;
        } else {
            economy.withdrawPlayer(owner, totalCost);
            economy.depositPlayer(viewer, totalCost);
            MSG message = MsgUtils.getMessage(LangUtils.getString("customer-sell-success-customer"),
                    ownerID, viewer, totalCost, item, amount);
            DraimShop.getPlugin().support().sendMSG(viewer, message);

            if (owner.isOnline()) {
                MSG ownerMessage = MsgUtils.getMessage(LangUtils.getString("customer-sell-success-owner"),
                        ownerID, viewer, totalCost, item, amount);
                DraimShop.getPlugin().support().sendMSG(owner.getPlayer(), ownerMessage);
            } else {
                CompletableFuture.runAsync(() -> DraimShop.getPlugin().getDB().storeMessage(ownerID, viewer,
                        false, item, amount, totalCost));
            }
            return true;
        }
    }

    public Inventory getInteractingInventory() {
        return this.interactingInventory;
    };

    public boolean interactingInventoryIsOwnerView() {
        return this.isOwnerView;
    }

    abstract public String listPrice(ItemStack item, double price);
    abstract public void purchaseItem(ItemStack item, int amount);
    abstract public void openUI();
    abstract public void openOwnerUI();
    abstract public void saveInventories();
}
