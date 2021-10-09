package me.draimgoose.draimshop.plugin;

import me.draimgoose.draimshop.crate.GetTotal;
import me.draimgoose.draimshop.crate.GiveHead;
import me.draimgoose.draimshop.crate.LockAll;
import me.draimgoose.draimshop.shop.GetShopOwner;
import me.draimgoose.draimshop.shop.SetShopCount;
import me.draimgoose.draimshop.shop.ShopCreation;
import me.draimgoose.draimshop.shop.ShopRemoval;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DSComdExec implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        } else {
            DSComd comd;
            switch (args[0]) {
                case "gettotal":
                    comd = new GetTotal(sender);
                    break;
                case "lockall":
                    comd = new LockAll(sender, args);
                    break;
                case "removeshop":
                    comd = new ShopRemoval(sender);
                    break;
                case "newshop":
                    comd = new ShopCreation(sender, false);
                    break;
                case "newadminshop":
                    comd = new ShopCreation(sender, true);
                    break;
                case "setcount":
                    comd = new SetShopCount(sender, args);
                    break;
                case "getshopowner":
                    comd = new GetShopOwner(sender);
                    break;
                case "reload":
                    comd = new Reload(sender);
                    break;
                case "givehead":
                    comd = new GiveHead(sender, args);
                    break;
                default:
                    comd = null;
                    break;
            }
            return comd == null ? false : comd.exec();
        }
    }
}
