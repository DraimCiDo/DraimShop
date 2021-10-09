package me.draimgoose.draimshop.shop.vm;

import me.draimgoose.draimshop.plugin.DraimShopLogger;
import me.draimgoose.draimshop.shop.ShopRemover;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.UUID;

public class VMRemover extends ShopRemover {
    private Location bottom;
    private Location top;
    private BlockStateMeta meta;

    public VMRemover(Block targetBlock, ArmorStand armorStand) {
        this.targetBlock = targetBlock;
        this.armorStand = armorStand;

        bottom = armorStand.getLocation();
        top = armorStand.getLocation();
        top.setY(top.getY() + 1);

        ItemStack chestItem = armorStand.getEquipment().getChestplate();
        if (chestItem == null || !(chestItem.getItemMeta() instanceof BlockStateMeta)
                || !(((BlockStateMeta) chestItem.getItemMeta()).getBlockState() instanceof ShulkerBox)) {
            DraimShopLogger.sendMSG("Попытка удалить торговый автомат в " + bottom
                    + " с пропавшим шалекром! Сообщите об этой ошибке!", DraimShopLogger.LVL.FAIL);
        } else {
            meta = (BlockStateMeta) chestItem.getItemMeta();
            if (meta.hasDisplayName()) {
                this.ownerUUID = UUID.fromString(meta.getDisplayName());
            } else {
                DraimShopLogger.sendMSG("Попытка удалить торговый автомат в " + bottom
                        + " с шалкером сотсутствующим отображаемым именем! Сообщите об этой ошибке!", DraimShopLogger.LVL.FAIL);
            }
        }
    }

    @Override
    public UUID removeShop(boolean dropItems) {
        bottom.getBlock().setType(Material.AIR);
        top.getBlock().setType(Material.AIR);

        if (dropItems) {
            ShulkerBox shulker = (ShulkerBox) this.meta.getBlockState();
            shulker.getInventory().forEach(item -> {
                if (item != null)
                    armorStand.getWorld().dropItem(armorStand.getLocation(), item);
            });
        }

        armorStand.remove();
        return this.ownerUUID;
    }
}
