package me.draimgoose.draimshop.shop;

import java.util.UUID;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;

public abstract class ShopRemover {
    protected Block targetBlock;
    protected ArmorStand armorStand;
    protected UUID ownerUUID;
    public abstract UUID removeShop(boolean dropItems);
}
