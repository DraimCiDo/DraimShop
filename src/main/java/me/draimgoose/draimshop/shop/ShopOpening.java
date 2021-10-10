package me.draimgoose.draimshop.shop;

import me.draimgoose.draimshop.gui.BriefcaseGUI;
import me.draimgoose.draimshop.gui.ShopGUI;
import me.draimgoose.draimshop.gui.VMGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.ShopUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Optional;

public class ShopOpening implements Listener {
    @EventHandler
    public void openShop(PlayerInteractEvent evt) {
        EquipmentSlot hand = evt.getHand();
        Player player = evt.getPlayer();
        Block targetBlock = evt.getClickedBlock();
        ArmorStand armorStand = ShopUtils.getArmorStand(targetBlock);
        if (armorStand == null || hand == null) {
            return;
        } else if (hand.equals(EquipmentSlot.OFF_HAND)) {
            evt.setCancelled(true);
            return;
        } else if (!hand.equals(EquipmentSlot.HAND) || !evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                || player.isSneaking()) {
            return;
        } else {
            evt.setCancelled(true);
            ShopGUI gui = getShopOpener(armorStand, player);
            if (gui != null) {
                PlayerState state = PlayerState.getPlayerState(player);
                state.clearShopInteractions();
                if (PlayerState.getInteractingPlayer(armorStand) != null) {
                    player.sendMessage(LangUtils.getString("shop-currently-in-use.shop"));
                    return;
                } else {
                    gui.openUI();
                    state.setShopGUI(gui);
                }
            }
        }
    }

    private static ShopGUI getShopOpener(ArmorStand armorStand, Player player) {
        String customName = armorStand.getCustomName();
        Optional<ShopGUI> result;
        switch (customName) {
            case "§5Торговый автомат":
                result = Optional.ofNullable(new VMGUI(armorStand, player));
                break;
            case "§5Портфель":
                result = Optional.ofNullable(new BriefcaseGUI(armorStand, player));
                break;
            default:
                result = Optional.empty();
                break;
        }
        return result.orElse(null);
    }
}
