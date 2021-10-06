package me.draimgoose.draimshop.utils;

import me.draimgoose.draimshop.plugin.DraimShopLogger;
import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.UUID;

public class ShopUtils {
    private ShopUtils() {
    }

    private static boolean hasPermA(ArmorStand armorStand, Player player) {
        if (player.hasPermission("draimshop.admin") || player.isOp()) {
            return true;
        }
        EntityEquipment equip = armorStand.getEquipment();

        ItemStack adminItem = equip.getBoots();
        if (adminItem != null & adminItem.getType() != Material.AIR) {
            return false;
        }

        return player.getUniqueId().equals(getOwner(armorStand).getUniqueId());
    }

    public static OfflinePlayer getOwner(ArmorStand armorStand) {
        if (armorStand == null) {
            return null;
        } else {
            EntityEquipment equip = armorStand.getEquipment();
            ItemStack item = equip.getChestplate();
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (!meta.hasDisplayName()) {
                    Location standLoc = armorStand.getLocation();
                    DraimShopLogger.sendMSG("DraimShop без имени владельца, обнаружен на" + standLoc + "!", LVL.FAIL);
                    return null;
                }
                String ownerUUID = meta.getDisplayName();
                return Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID));
            } else {
                Location standLoc = armorStand.getLocation();
                DraimShopLogger.sendMSG("DraimShop без предмета владельца, обнаружен на" + standLoc + "!", LVL.FAIL);
                return null;
            }
        }
    }

    public static ArmorStand getArmorStand(Block targetBlock) {
        if (targetBlock == null) {
            return null;
        }
        Location loc = new Location(targetBlock.getWorld(), targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5);
        Collection<Entity> list = targetBlock.getWorld().getNearbyEntities(loc, 0,0,0);
        if (targetBlock.getType() != Material.BARRIER || list.size() != 1) {
            return null;
        } else {
            Entity shopEntity = (Entity) list.toArray()[0];
            if (shopEntity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) shopEntity;
                String name = armorStand.getCustomName();
                boolean valid;
                switch (name == null ? null : name) {
                    case "§5Торговый автомат":
                        valid = true;
                        break;
                    case "§5Портфель":
                        valid = true;
                        break;
                    default:
                        valid = false;
                        break;
                }
                return valid ? armorStand : null;
            } else {
                return null;
            }
        }
    }
}
