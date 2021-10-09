package me.draimgoose.draimshop.shop.vm;

import me.draimgoose.draimshop.gui.ShopGUI;
import me.draimgoose.draimshop.gui.VMGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.shop.conversation.PruchaseConvFactory;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VMInteractInv implements Listener {

    @EventHandler
    public void clickShop(InventoryClickEvent evt) {
        if (evt.getCurrentItem() == null) {
            return;
        }
        Player player = (Player) evt.getWhoClicked();
        PlayerState state = PlayerState.getPlayerState(player);
        ShopGUI shopGUI = state.getShopGUI();

        if (shopGUI instanceof VMGUI
                && player.getOpenInventory().getTopInventory().equals(shopGUI.getInteractingInventory())
                && !shopGUI.interactingInventoryIsOwnerView()) {
            evt.setCancelled(true);
            if (!evt.getClickedInventory().equals(shopGUI.getInteractingInventory())) {
                return;
            }
        } else {
            return;
        }

        ItemMeta itemMeta = evt.getCurrentItem().getItemMeta();
        if (itemMeta.hasDisplayName()
                && itemMeta.getDisplayName().equals("§c" + LangUtils.getString("icons.close"))) {
            Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.closeInventory());
        } else if (evt.getSlot() < 27) {
            VMGUI ui = (VMGUI) shopGUI;
            ItemStack item = ui.getItem(evt.getSlot());
            state.startTransaction(item, new PruchaseConvFactory());
            Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> player.closeInventory());
        }

    }
}
