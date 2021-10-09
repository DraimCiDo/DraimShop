package me.draimgoose.draimshop.plugin;

import org.bukkit.command.CommandSender;

public abstract class DSComd {
    protected CommandSender sender;
    protected String[] args;

    public DSComd(CommandSender sender, String[] args) {
        this.sender = sender;
        this.args = args;
    }

    public abstract boolean exec();
}
