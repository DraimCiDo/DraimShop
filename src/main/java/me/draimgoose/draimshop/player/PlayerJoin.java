package me.draimgoose.draimshop.player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.MsgUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        CompletableFuture.runAsync(() -> {
            DraimShop plugin = DraimShop.getPlugin();
            List<MsgUtils.MSG> messages = plugin.getDB()
                    .getMessages(evt.getPlayer().getUniqueId().toString());
            messages.forEach(e -> plugin.support().sendMSG(evt.getPlayer(), e));
        });
    }
}
