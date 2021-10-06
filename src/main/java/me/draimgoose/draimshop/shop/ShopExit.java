package me.draimgoose.draimshop.shop;

import me.draimgoose.draimshop.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ShopExit implements Listener {
    @EventHandler
    public void closeShop(InventoryCloseEvent evt) {
        if (!((Player) evt.getPlayer()).isConversing()) {
            PlayerState.getPlayerState((Player) evt.getPlayer()).clearShopInteractions();
        }
    }
}
