package me.draimgoose.draimshop.plugin;

// WG
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
// IA
import dev.lone.itemsadder.api.CustomStack;
// ProtocolLib
import com.comphenix.protocol.PacketType.Play.Client;
// NoName
import me.draimgoose.draimshop.plugin.DraimShopLogger.LVL;
import me.draimgoose.draimshop.shop.ShopCreator;
import me.draimgoose.draimshop.shop.briefcase.BCCreator;
import me.draimgoose.draimshop.shop.vm.VMCreator;
import me.draimgoose.draimshop.utils.MsgUtils.MSG;
// Apache
import org.apache.commons.lang.WordUtils;
// Bukkit
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
// LocaleLib
import me.pikamug.localelib.LocaleLib;
import me.pikamug.localelib.LocaleManager;
// NeTTy
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
// Java utils
import java.util.HashMap;
import java.util.Set;

public class ExternalPluginsSupport {
    private DraimShop plugin;
    private final String[] landProtectionPlugins = new String[] { "WorldGuard" };
    private final String[] customItemsPlugins = new String[] { "ItemsAdder" };
    private final String[] externalLibraries = new String[] { "ProtocolLib", "LocaleLib" };

    public ExternalPluginsSupport(DraimShop plugin) {
        this.plugin = plugin;
    }

    public void init() {
        for (String plugin : landProtectionPlugins) {
            if (this.has(plugin))
                DraimShopLogger.sendMessage("Успешный подхват плагина " + plugin, LVL.INFO);
        }
        for (String plugin : customItemsPlugins) {
            if (this.has(plugin))
                DraimShopLogger.sendMessage("Успешный подхват плагина " + plugin, LVL.INFO);
        }
        for (String plugin : externalLibraries) {
            if (this.has(plugin))
                DraimShopLogger.sendMessage("Успешный подхват плагина " + plugin, LVL.SUCCESS);
        }
    }

    private boolean has(String pluginName) {
        return this.plugin.getServer().getPluginManager().getPlugin(pluginName) != null;
    }

    // #######################################################################################################################
    // ItemsAdder
    // #######################################################################################################################

    public HashMap<Integer, ItemStack> getModelDataToShopMapping() {
        if (!has("ItemsAdder"))
            return null;
        HashMap<Integer, ItemStack> map = new HashMap<>();

        Set<String> vm = this.plugin.getConfig().getConfigurationSection("vending-machine").getKeys(false);
        for (String shop : vm) {
            Integer customModelData = this.plugin.getConfig().getInt("vending-machine." + shop + ".model-data");
            ItemStack item = CustomStack.getInstance("draimshop:" + shop + "_vending_machine").getItemStack();
            map.put(customModelData, item);
        }
        Set<String> nb = this.plugin.getConfig().getConfigurationSection("briefcase").getKeys(false);
        for (String shop : nb) {
            Integer customModelData = this.plugin.getConfig().getInt("briefcase." + shop + ".model-data");
            ItemStack item = CustomStack.getInstance("draimshop:" + shop + "_briefcase").getItemStack();
            map.put(customModelData, item);
        }

        Integer defaultVM = this.plugin.getConfig().getInt("defaults.vending-machine");
        ItemStack defaultVMItem = CustomStack.getInstance("draimshop:default_vending_machine").getItemStack();
        map.put(defaultVM, defaultVMItem);
        Integer defaultBriefcase = this.plugin.getConfig().getInt("defaults.briefcase");
        ItemStack defaultBriefcaseItem = CustomStack.getInstance("draimshop:default_briefcase").getItemStack();
        map.put(defaultBriefcase, defaultBriefcaseItem);

        return map;
    }

    public Boolean isDefaultModel(ItemStack item) {
        if (!has("ItemsAdder"))
            return null;
        String id = CustomStack.byItemStack(item).getNamespacedID();
        return id.contains("default");
    }

    public ShopCreator getShopCreator(ItemStack item) {
        if (!has("ItemsAdder"))
            return null;
        String id = CustomStack.byItemStack(item).getNamespacedID();
        if (id.contains("vending_machine"))
            return new VMCreator();
        return new BCCreator();
    }

    // #######################################################################################################################
    // Внешние либы
    // #######################################################################################################################

    public void sendMessage(Player player, MSG message) {
        if (message.hasDisplayName()) {
            player.sendMessage(message.getMessage().replaceAll("\\{%item%\\}", message.getItemName()));
        } else if (message.getItemName() == null) {
            player.sendMessage(message.getMessage().replaceAll("\\{%item%\\}", ""));
        } else if (!has("LocaleLib")) {
            String itemName = WordUtils.capitalize(message.getItemName().toLowerCase().replaceAll("_", " "));
            player.sendMessage(message.getMessage().replaceAll("\\{%item%\\}", itemName));
        } else {
            LocaleManager localeManager = ((LocaleLib) this.plugin.getServer().getPluginManager()
                    .getPlugin("LocaleLib")).getLocaleManager();
            String rawMessage = message.getMessage().replaceAll("\\{%item%\\}", "<item>");
            new BukkitRunnable() {
                @Override
                public void run() {
                    localeManager.sendMessage(player, rawMessage, Material.matchMaterial(message.getItemName()),
                            (short) 0, null);
                }
            }.runTask(this.plugin);
        }
    }

    public void blockDamagePacketHandler(BlockDamageEvent evt) {
        if (!has("ProtocolLib")) {
            createPipeline(evt);
        } else {
            protocolLibHandler(evt);
        }
    }

    private void protocolLibHandler(BlockDamageEvent evt) {
        if (!this.has("ProtocolLib"))
            return;

        try {
            Class<?> ProtocolLibrary = Class.forName("com.comphenix.protocol.ProtocolLibrary");
            Class<?> PacketListener = Class.forName("com.comphenix.protocol.events.PacketListener");
            Object protocolManager;
            protocolManager = ProtocolLibrary.getMethod("getProtocolManager").invoke(null);
            protocolManager.getClass().getMethod("addPacketListener", PacketListener).invoke(protocolManager,
                    new ProtocolLibHandler(DraimShop.getPlugin(), Client.BLOCK_DIG, evt));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

    }

    private void createPipeline(BlockDamageEvent evt) {
        Class<?> PacketPlayInBlockDig, EnumPlayerDigType, CraftPlayer;
        try {
            PacketPlayInBlockDig = getNMSClass("PacketPlayInBlockDig");
            EnumPlayerDigType = PacketPlayInBlockDig.getDeclaredClasses()[0];
            CraftPlayer = getCraftBukkitClass("entity.CraftPlayer");

            Player player = evt.getPlayer();
            Object handle = CraftPlayer.getMethod("getHandle").invoke(player);
            Object channel;
            if (Bukkit.getVersion().contains("1.17")) {
                Object playerConnection = handle.getClass().getField("b").get(handle);
                Object networkManager = playerConnection.getClass().getField("a").get(playerConnection);
                channel = networkManager.getClass().getField("k").get(networkManager);
            } else {
                Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
                Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
                channel = networkManager.getClass().getField("channel").get(networkManager);
            }
            Object pipeline = channel.getClass().getMethod("pipeline").invoke(channel);

            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (PacketPlayInBlockDig.isInstance(msg)) {
                        Object detectedEnum = PacketPlayInBlockDig.getMethod("d").invoke(msg);
                        Object digType = Enum.class.getMethod("valueOf", Class.class, String.class).invoke(null,
                                EnumPlayerDigType, "ABORT_DESTROY_BLOCK");
                        if (detectedEnum.equals(digType)) {
                            evt.setCancelled(true);
                            pipeline.getClass().getMethod("remove", ChannelHandler.class).invoke(pipeline, this);
                        }
                    }
                    super.channelRead(ctx, msg);
                }
            };

            if (pipeline.getClass().getMethod("get", String.class).invoke(pipeline, player.getName()) == null) {
                pipeline.getClass().getMethod("addBefore", String.class, String.class, ChannelHandler.class)
                        .invoke(pipeline, "packet_handler", player.getName(), handler);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private Class<?> getNMSClass(String className) throws ClassNotFoundException {
        if (Bukkit.getVersion().contains("1.17")) {
            return Class.forName("net.minecraft.network.protocol.game." + className);
        } else {
            return Class.forName("net.minecraft.server."
                    + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + className);
        }
    }

    private Class<?> getCraftBukkitClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit."
                + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + className);
    }

    // #######################################################################################################################
    // WorldGuard
    // #######################################################################################################################

    public boolean hasCreatePerms(Location location, Player player) {
        return hasWorldGuardCreatePerms(location, player);
    }

    private boolean hasWorldGuardCreatePerms(Location location, Player player) {
        if (!this.has("WorldGuard"))
            return true;
        if (!DraimShop.getPlugin().getConfig().getBoolean("worldguard-enabled"))
            return true;

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return true;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return query.testState(BukkitAdapter.adapt(location), localPlayer, Flags.BUILD);
    }

    // #######################################################################################################################
    // WorldGuard
    // #######################################################################################################################

    public boolean hasRemovePerms(Location location, Player player) {
        return hasWorldGuardRemovePerms(location, player);
    }

    private boolean hasWorldGuardRemovePerms(Location location, Player player) {
        if (!this.has("WorldGuard"))
            return true;
        if (!DraimShop.getPlugin().getConfig().getBoolean("worldguard-enabled"))
            return true;

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return true;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return query.testState(BukkitAdapter.adapt(location), localPlayer, Flags.BUILD);
    }
}
