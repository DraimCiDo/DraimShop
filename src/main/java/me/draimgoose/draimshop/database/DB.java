package me.draimgoose.draimshop.database;

import me.draimgoose.draimshop.plugin.DraimShop;

public abstract class DB {
    DraimShop plugin;

    public DB(DraimShop instance) {
        plugin = instance;
    }
}
