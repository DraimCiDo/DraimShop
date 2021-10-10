package me.draimgoose.draimshop.shop.briefcase;

import me.draimgoose.draimshop.plugin.DraimShopLogger;
import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import me.draimgoose.draimshop.shop.ShopRemover;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class BCRemover extends ShopRemover {

    private Location location;
    private ItemStack item;

    public BCRemover(Block targetBlock, ArmorStand armorStand) {
        this.targetBlock = targetBlock;
        this.armorStand = armorStand;
        this.location = armorStand.getLocation();

        item = armorStand.getEquipment().getLeggings();
        ItemStack placeHolder = armorStand.getEquipment().getChestplate();

        if (placeHolder == null || placeHolder.getType() == Material.AIR) {
            DraimShopLogger.sendMessage(
                    "Попытка удалить портфель на " + location + " с пропавшим предметом из сундука! Сообщите об этой ошибке!",
                    LVL.FAIL);
        } else {
            ItemMeta meta = placeHolder.getItemMeta();
            if (meta.hasDisplayName()) {
                this.ownerUUID = UUID.fromString(meta.getDisplayName());
            } else {
                DraimShopLogger.sendMessage("Попытка удалить портфель на " + location
                        + " с сундуком с отсутствующим именем! Сообщите об этой ошибке!!", LVL.FAIL);
                return;
            }

            List<String> info = meta.getLore();
            int amount = Integer.parseInt(info.get(1));

            if (amount == 0) {
                this.item = null;
            } else if (item != null) {
                this.item.setAmount(amount);
            }
        }
    }

    @Override
    public UUID removeShop(boolean dropItems) {
        if (this.item != null && dropItems) {
            Bukkit.getPlayer(this.ownerUUID).sendMessage(LangUtils.getString("remove.contain-items"));
            return null;
        } else {
            location.getBlock().setType(Material.AIR);
            armorStand.remove();
            return this.ownerUUID;
        }
    }
}