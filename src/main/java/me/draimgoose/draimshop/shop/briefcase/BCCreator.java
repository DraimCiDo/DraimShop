package me.draimgoose.draimshop.shop.briefcase;

import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.shop.ShopCreator;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class BCCreator extends ShopCreator {
    @Override
    public void createShop(Location location, Player owner, ItemStack item, boolean isAdmin) {
        Boolean nullable = DraimShop.getPlugin().support().isDefaultModel(item);
        if (nullable != null && nullable.booleanValue()) {
            owner.sendMessage(LangUtils.getString("create.briefcase.locked"));
            return;
        }

        if (item.getItemMeta().getCustomModelData() == DraimShop.getPlugin().getConfig()
                .getInt("defaults.briefcase")) {
            owner.sendMessage(LangUtils.getString("create.briefcase.locked"));
            return;
        }
        if (location.getBlock().getType() != Material.AIR) {
            owner.sendMessage(LangUtils.getString("create.briefcase.invalid-block"));
            return;
        }

        location.getBlock().setType(Material.BARRIER);

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setSmall(true);
        armorStand.setCustomName("§5Портфель");
        EntityEquipment armorStandBody = armorStand.getEquipment();
        armorStandBody.setHelmet(item);

        ItemStack placeHolder = new ItemStack(Material.DIRT);
        ItemMeta meta = placeHolder.getItemMeta();

        double price = 0;
        int amount = 0;
        boolean selling = true;

        List<String> lore = Arrays.asList(String.valueOf(price), String.valueOf(amount), String.valueOf(selling));
        meta.setDisplayName(owner.getUniqueId().toString());
        meta.setLore(lore);

        placeHolder.setItemMeta(meta);
        armorStandBody.setChestplate(placeHolder);

        if (isAdmin) {
            armorStandBody.setBoots(new ItemStack(Material.DIRT));
        }

        lockArmorStand(armorStand);

        CompletableFuture.runAsync(() -> {
            DraimShop.getPlugin().getDB().incrementTotalShopsOwned(owner.getUniqueId());
            owner.sendMessage(LangUtils.getString("create.briefcase.success"));
        });
    }
}

