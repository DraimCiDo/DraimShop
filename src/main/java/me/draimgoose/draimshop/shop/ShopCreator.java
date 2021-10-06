package me.draimgoose.draimshop.shop;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ShopCreator {
    public abstract void createShop(Location location, Player owner, ItemStack item, boolean isAdmin);

    protected void lockArmorStand(ArmorStand armorStand) {
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
    }
}
