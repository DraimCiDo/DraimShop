package me.draimgoose.draimshop.crate;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.draimgoose.draimshop.gui.CreationGUI;
import me.draimgoose.draimshop.plugin.DSComd;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GiveHead extends DSComd {
    public GiveHead(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public boolean exec() {
        if (sender instanceof ConsoleCommandSender) {
            ItemStack reward;
            if (args.length < 3) {
                return false;
            } else if (args.length == 3) {
                reward = getSkull(
                        "http://textures.minecraft.net/texture/c6e69b1c7e69bcd49ed974f5ac36ea275efabb8c649cb2b1fe9d6ea6166ec3");
            } else {
                reward = getSkull(args[3]);
            }
            ItemMeta meta = reward.getItemMeta();
            int model = Integer.parseInt(args[2]);
            meta.setCustomModelData(model);
            meta.setDisplayName(getNamefromModelData(model));
            reward.setItemMeta(meta);
            Player player = Bukkit.getPlayer(args[1]);
            if (!player.getInventory().addItem(reward).isEmpty()) {
                player.sendMessage("§6Недостаточно места в инвентаре, награда падает на землю...");
                player.getLocation().getWorld().dropItem(player.getLocation(), reward);
            }
            return true;
        }
        return false;
    }

    private ItemStack getSkull(String url) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);

        if (url == null || url.isEmpty())
            return null;

        ItemMeta skullMeta = skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder()
                .encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;

        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }

        profileField.setAccessible(true);

        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        skull.setItemMeta(skullMeta);
        return skull;
    }

    private String getNamefromModelData(int model) {
        return CreationGUI.names.get(CreationGUI.modelData.indexOf(model));
    }
}

