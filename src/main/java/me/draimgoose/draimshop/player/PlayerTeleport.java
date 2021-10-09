package me.draimgoose.draimshop.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;


public class PlayerTeleport implements Listener {
    @EventHandler
    public void onTeleport(PlayerTeleportEvent evt) {
        PlayerState.getPlayerState(evt.getPlayer()).clearShopInteractions();
    }
}
