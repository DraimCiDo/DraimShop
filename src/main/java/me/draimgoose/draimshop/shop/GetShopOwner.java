package me.draimgoose.draimshop.shop;

import me.draimgoose.draimshop.plugin.DSComd;
import me.draimgoose.draimshop.utils.LangUtils;
import me.draimgoose.draimshop.utils.ShopUtils;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class GetShopOwner extends DSComd {
    public GetShopOwner(CommandSender sender) {
        super(sender, null);
    }

    @Override
    public boolean exec() {
        if (this.sender instanceof Player) {
            Player player = (Player) this.sender;
            Block targetBlock = player.getTargetBlockExact(5);
            ArmorStand armorStand = ShopUtils.getArmorStand(targetBlock);
            if (armorStand != null) {
                player.sendMessage(
                        String.format(LangUtils.getString("shop-owner"), ShopUtils.getOwner(armorStand).getName()));
            } else {
                player.sendMessage(LangUtils.getString("invalid-target"));

            }
        }
        return true;
    }
}
