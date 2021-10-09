package me.draimgoose.draimshop.shop.vm;

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
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VMCreator extends ShopCreator {
    @Override
    public void createShop(Location location, Player owner, ItemStack item, boolean isAdmin) {
        Boolean nullable = DraimShop.getPlugin().support().isDefaultModel(item);
        if (nullable != null && nullable.booleanValue()) {
            owner.sendMessage(LangUtils.getString("create.vending-machine.locked"));
            return;
        }

        if (item.getItemMeta().getCustomModelData() == DraimShop.getPlugin().getConfig()
                .getInt("defaults.vending-machine")) {
            owner.sendMessage(LangUtils.getString("create.vending-machine.locked"));
            return;
        }

        Location locationAddOne = location.clone();
        locationAddOne.setY(location.getY() + 1);
        if (!location.getBlock().getType().isAir() || !locationAddOne.getBlock().getType().isAir()) {
            owner.sendMessage(LangUtils.getString("create.vending-machine.invalid-block"));
            return;
        }

        location.getBlock().setType(Material.BARRIER);
        locationAddOne.getBlock().setType(Material.BARRIER);

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setCustomName("§5Торговый автомат");
        EntityEquipment armorStandBody = armorStand.getEquipment();
        armorStandBody.setHelmet(item);

        ItemStack container = new ItemStack(Material.SHULKER_BOX);
        BlockStateMeta blockMeta = (BlockStateMeta) container.getItemMeta();
        List<String> lore = Stream.<String>generate(() -> "0.0").limit(27).collect(Collectors.toList());
        blockMeta.setDisplayName(owner.getUniqueId().toString());
        blockMeta.setLore(lore);

        container.setItemMeta(blockMeta);
        armorStandBody.setChestplate(container);

        if (isAdmin) {
            armorStandBody.setBoots(new ItemStack(Material.DIRT));
        }

        lockArmorStand(armorStand);

        CompletableFuture.runAsync(() -> {
            DraimShop.getPlugin().getDB().incrementTotalShopsOwned(owner.getUniqueId());
            owner.sendMessage(LangUtils.getString("create.vending-machine.success"));
        });
    }
}
