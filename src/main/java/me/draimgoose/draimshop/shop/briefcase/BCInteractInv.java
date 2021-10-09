package me.draimgoose.draimshop.shop.briefcase;

import me.draimgoose.draimshop.gui.BriefcaseGUI;
import me.draimgoose.draimshop.gui.ShopGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.shop.conversation.*;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BCInteractInv implements Listener {
    @EventHandler
    public void clickShop(InventoryClickEvent evt) {
        if (evt.getCurrentItem() == null) {
            return;
        }
        Player player = (Player) evt.getWhoClicked();
        PlayerState state = PlayerState.getPlayerState(player);
        ShopGUI shopGUI = state.getShopGUI();

        if (shopGUI instanceof BriefcaseGUI
                && player.getOpenInventory().getTopInventory().equals(shopGUI.getInteractingInventory())) {
            evt.setCancelled(true);
            if (!evt.getClickedInventory().equals(shopGUI.getInteractingInventory())) {
                return;
            }
        } else {
            return;
        }
        BriefcaseGUI ui = (BriefcaseGUI) shopGUI;
        if (!ui.interactingInventoryIsOwnerView()) {
            ItemMeta itemMeta = evt.getCurrentItem().getItemMeta();
            if (evt.getSlot() >= 27 && itemMeta.hasDisplayName()) {
                String displayName = itemMeta.getDisplayName();
                if (displayName.equals("§c" + LangUtils.getString("icons.close")))
                    Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.closeInventory());
            } else if (evt.getSlot() < 27) {
                ItemStack item = ui.getItem();
                if (ui.isSelling()) {
                    state.startTransaction(item, new PruchaseConvFactory());
                } else {
                    state.startTransaction(item, new SellConvFactory());
                }
                Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.closeInventory());
            }
        } else if (ui.interactingInventoryIsOwnerView()) {
            ItemMeta itemMeta = evt.getCurrentItem().getItemMeta();
            if (evt.getSlot() >= 27 && itemMeta.hasDisplayName()) {
                String displayName = itemMeta.getDisplayName();
                if (displayName.equals("§c" + LangUtils.getString("icons.close"))) {
                    Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.closeInventory());
                } else if (displayName.equals("§6" + LangUtils.getString("price-tag.selling"))) {
                    ui.setSelling(false);
                } else if (displayName.equals("§6" + LangUtils.getString("price-tag.buying"))) {
                    ui.setSelling(true);
                } else if (displayName.equals("§6" + LangUtils.getString("icons.change-price.title"))) {
                    state.startConversation(new SetPriceConvFactory(ui.getItem()));
                    Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.closeInventory());
                } else if (displayName.equals("§6" + LangUtils.getString("icons.add-items.title"))) {
                    state.startConversation(new AddConvFactory());
                    Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.closeInventory());
                } else if (displayName.equals("§6" + LangUtils.getString("icons.retrieve-items.title"))) {
                    state.startConversation(new RetriveConvFactory());
                    Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.closeInventory());
                }
            }
        }
    }
}
