package me.draimgoose.draimshop.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerLeave implements Listener {
    @EventHandler
    public void playerKick(PlayerKickEvent evt) {
        PlayerState.getPlayerState(evt.getPlayer()).clearShopInteractions();
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent evt) {
        PlayerState.getPlayerState(evt.getPlayer()).clearShopInteractions();
    }
}
