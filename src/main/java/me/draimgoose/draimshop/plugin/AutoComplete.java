package me.draimgoose.draimshop.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class AutoComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String[] array = new String[] { "newshop", "gettotal", "getshopowner" };
            List<String> subCommands = new ArrayList<>(Arrays.asList(array));
            if (sender.hasPermission("draimshop.removeshop.command")) {
                subCommands.add("removeshop");
            }
            if (sender.hasPermission("draimshop.lockall")) {
                subCommands.add("lockall");
            }
            if (sender.hasPermission("draimshop.setcount")) {
                subCommands.add("setcount");
            }
            if (sender.hasPermission("draimshop.reload")) {
                subCommands.add("reload");
            }
            if (sender.hasPermission("draimshop.admin")) {
                subCommands.add("newadminshop");
            }
            return subCommands;
        } else if (args.length == 2 && ((args[0].equals("lockall") && sender.hasPermission("draimshop.lockall"))
                || (args[0].equals("setcount") && sender.hasPermission("draimshop.setcount")))) {
            return DraimShop.getPlugin().getServer().getOnlinePlayers().stream().map(p -> p.getName())
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }
}
