package me.draimgoose.draimshop.plugin;

import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.command.CommandSender;

public class Reload extends DSComd {

    public Reload(CommandSender sender) {
        super(sender, null);
    }

    @Override
    public boolean exec() {
        if (!sender.hasPermission("customshop.reload")) {
            sender.sendMessage(LangUtils.getString("command-no-perms"));
        } else {
            DraimShop.getPlugin().reloadConfig();
            LangUtils.loadLangConfig();
            sender.sendMessage("§7(§6DraimShop§7) §aКонфигурация успешно перезагружена.");
        }
        return true;
    }

}
