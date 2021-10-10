package me.draimgoose.draimshop.shop;

import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DSComd;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.shop.briefcase.BCRemover;
import me.draimgoose.draimshop.shop.vm.VMRemover;
import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.ShopUtils;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ShopRemoval extends DSComd implements Listener {
    public ShopRemoval() {
        super(null, null);
    }

    public ShopRemoval(CommandSender sender) {
        super(sender, null);
    }

    @EventHandler
    public void onBarrierBreak(BlockDamageEvent evt) {
        Player player = evt.getPlayer();
        Block targetBlock = evt.getBlock();
        ShopRemover remover = getShopRemover(targetBlock, player);
        if (remover != null) {
            if (!player.hasPermission("draimshop.removeshop.break")) {
                return;
            }
            DraimShop.getPlugin().support().blockDamagePacketHandler(evt);
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!evt.isCancelled()) {
                        PlayerState.getPlayerState(player).clearShopInteractions();
                        UUID ownerID = remover.removeShop(true);
                        if (ownerID != null) {
                            targetBlock.getWorld().playSound(targetBlock.getLocation(), Sound.BLOCK_STONE_BREAK, 1.5F,
                                    1.0F);
                            CompletableFuture.runAsync(
                                    () -> DraimShop.getPlugin().getDB().decrementTotalShopsOwned(ownerID));
                        }
                    }
                }
            };
            runnable.runTaskLater(DraimShop.getPlugin(), 36);
        }
    }

    @EventHandler
    public void onBarrierBreak(BlockBreakEvent evt) {
        if (evt.isCancelled() || evt.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            return;
        }
        Player player = evt.getPlayer();
        Block targetBlock = evt.getBlock();
        ShopRemover remover = getShopRemover(targetBlock, player);
        if (remover != null) {
            if (!player.hasPermission("draimshop.removeshop.break")) {
                evt.setCancelled(true);
                return;
            }
            PlayerState.getPlayerState(player).clearShopInteractions();
            UUID ownerID = remover.removeShop(true);
            if (ownerID != null) {
                CompletableFuture
                        .runAsync(() -> DraimShop.getPlugin().getDB().decrementTotalShopsOwned(ownerID));
            } else {
                evt.setCancelled(true);
            }
        } else if (ShopUtils.getArmorStand(targetBlock) != null) {
            evt.setCancelled(true);
        }
    }

    @Override
    public boolean exec() {
        if (!(sender instanceof Player)) {
            return true;
        } else if (!sender.hasPermission("draimshop.removeshop.command")) {
            sender.sendMessage(LangUtils.getString("command-no-perms"));
            return false;
        }
        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null) {
            player.sendMessage(LangUtils.getString("invalid-block"));
            return true;
        }
        ShopRemover remover = getShopRemover(targetBlock, player);
        if (remover != null) {
            UUID ownerID = remover.removeShop(false);
            if (ownerID != null) {
                CompletableFuture
                        .runAsync(() -> DraimShop.getPlugin().getDB().decrementTotalShopsOwned(ownerID));
            }
        } else {
            player.sendMessage(LangUtils.getString("invalid-target"));
        }
        return true;
    }

    private static ShopRemover getShopRemover(Block targetBlock, Player player) {
        ArmorStand armorStand = ShopUtils.getArmorStand(targetBlock);
        if (armorStand == null) {
            return null;
        } else if (PlayerState.getInteractingPlayer(armorStand) != null) {
            player.sendMessage(LangUtils.getString("shop-currently-in-use.shop"));
            return null;
        } else if (!ShopUtils.hasShopPermission(armorStand, player)
                || !DraimShop.getPlugin().support().hasRemovePerms(armorStand.getLocation(), player))
            return null;

        String customName = armorStand.getCustomName();
        ShopRemover result;
        switch (customName) {
            case "§5Торговый автомат":
                result = new VMRemover(targetBlock, armorStand);
                break;
            case "§5Портфель":
                result = new BCRemover(targetBlock, armorStand);
                break;
            default:
                result = null;
                break;
        }
        return result;
    }
}
