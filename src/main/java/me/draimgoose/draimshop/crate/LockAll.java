package me.draimgoose.draimshop.crate;

import me.draimgoose.draimshop.plugin.DSComd;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class LockAll extends DSComd {

    public LockAll(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public boolean exec() {
        if (!sender.hasPermission("draimshop.lockall")) {
            sender.sendMessage(LangUtils.getString("command-no-perms"));
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage("§cНедопустимое количество аргументов!");
            return false;
        }
        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage("§cНе удается найти указанного игрока!");
            return false;
        }
        CompletableFuture.runAsync(() -> {
            DraimShop.getPlugin().getDB().setUnlockedShops(player, new ArrayList<>());
            sender.sendMessage("§aУспешно заблокированы все пользовательские магазины указанного игрока!");
        });
        return true;
    }
}
