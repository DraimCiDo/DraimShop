package me.draimgoose.draimshop.shop.briefcase;

import me.draimgoose.draimshop.gui.BriefcaseGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.shop.conversation.SetPriceConvFactory;
import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.ShopUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BCListItem implements Listener {
    @EventHandler
    public void listItem(PlayerInteractEvent evt) {
        EquipmentSlot hand = evt.getHand();
        Player player = evt.getPlayer();
        if (hand == null || !hand.equals(EquipmentSlot.HAND) || !evt.getAction().equals(Action.LEFT_CLICK_BLOCK)
                || !player.isSneaking()) {
            return;
        }
        Block targetBlock = evt.getClickedBlock();
        ArmorStand armorStand = ShopUtils.getArmorStand(targetBlock);
        if (armorStand == null) {
            return;
        }
        if (!armorStand.getCustomName().equals("§5Портфель")) {
            return;
        } else {
            evt.setCancelled(true);
            PlayerState state = PlayerState.getPlayerState(player);
            state.clearShopInteractions();
            if (!ShopUtils.hasShopPermission(armorStand, player)) {
                player.sendMessage(LangUtils.getString("shop-no-perms"));
                return;
            }
            if (PlayerState.getInteractingPlayer(armorStand) != null) {
                player.sendMessage(LangUtils.getString("shop-currently-in-use.briefcase"));
                return;
            }
            BriefcaseGUI ui = new BriefcaseGUI(armorStand, player);
            ItemStack itemInHand = player.getEquipment().getItemInMainHand();
            if (itemInHand.getType().equals(Material.AIR)) {
                ui.openOwnerUI();
                state.setShopGUI(ui);
            } else {
                if (ui.hasItem()) {
                    player.sendMessage(LangUtils.getString("briefcase-already-initialized"));
                } else {
                    Bukkit.getScheduler().runTask(DraimShop.getPlugin(),
                            () -> state.startConversation(new SetPriceConvFactory(itemInHand)));
                    state.setShopGUI(ui);
                }
            }
        }
    }
}
