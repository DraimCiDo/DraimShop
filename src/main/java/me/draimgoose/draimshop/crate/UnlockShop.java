package me.draimgoose.draimshop.crate;

import me.draimgoose.draimshop.gui.CreationGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UnlockShop implements Listener {
    @EventHandler
    public void onPlace(BlockPlaceEvent evt) {
        Player player = evt.getPlayer();
        ItemStack item = evt.getItemInHand();
        ItemMeta meta = item.getItemMeta();

        if ((item.getType() != Material.PLAYER_HEAD && item.getType() != Material.PLAYER_WALL_HEAD)
                || !meta.hasDisplayName() || !meta.hasCustomModelData())
            return;

        String displayName = meta.getDisplayName();
        int modelData = meta.getCustomModelData();

        for (int i = 0; i < CreationGUI.noOfItems; i++) {
            if (CreationGUI.modelData.get(i).equals(modelData) && CreationGUI.names.get(i).equals(displayName)) {
                evt.setCancelled(true);
                PlayerState state = PlayerState.getPlayerState(player);
                if (state.getUnlockingShopItem() == null || !state.getUnlockingShopItem().isSimilar(item)) {
                    player.sendMessage(LangUtils.getString("unlock.confirmation"));
                    state.setUnlockingShop(item);
                } else {
                    CompletableFuture<List<Integer>> cf = CompletableFuture
                            .supplyAsync(() -> DraimShop.getPlugin().getDB().getUnlockedShops(player));
                    cf.thenAccept(list -> {
                        BukkitRunnable runnable = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (list.contains(modelData)) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.5F, 1.0F);
                                    player.sendMessage(String.format(LangUtils.getString("unlock.unlocked-already"),
                                            displayName));
                                } else {
                                    list.add(modelData);
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.5F, 1.0F);
                                    CompletableFuture.runAsync(() -> {
                                        DraimShop.getPlugin().getDB().setUnlockedShops(player, list);
                                        player.sendMessage(String.format(LangUtils.getString("unlock.unlocked-new"),
                                                displayName));
                                    });
                                    item.setAmount(item.getAmount() - 1);
                                }
                            }
                        };
                        runnable.runTask(DraimShop.getPlugin());
                    });
                }
                break;
            }
        }
    }
}
