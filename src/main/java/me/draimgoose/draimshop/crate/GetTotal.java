package me.draimgoose.draimshop.crate;

import me.draimgoose.draimshop.plugin.DSComd;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class GetTotal extends DSComd {
    public GetTotal(CommandSender sender) {
        super(sender, null);
    }

    @Override
    public boolean exec() {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            CompletableFuture<Integer> cf = CompletableFuture
                    .supplyAsync(() -> DraimShop.getPlugin().getDB().getTotalShopOwned(player.getUniqueId()));
            cf.thenAccept(
                    total -> player.sendMessage(String.format(LangUtils.getString("total-shop-owned"), total)));
        }
        return false;
    }
}

