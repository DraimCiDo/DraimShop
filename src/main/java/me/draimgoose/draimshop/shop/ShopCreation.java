package me.draimgoose.draimshop.shop;

import me.draimgoose.draimshop.gui.CreationGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DSComd;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.shop.briefcase.BCCreator;
import me.draimgoose.draimshop.shop.vm.VMCreator;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ShopCreation extends DSComd implements Listener {
    private boolean isAdmin;

    public ShopCreation() {
        super(null, null);
    }

    public ShopCreation(CommandSender sender, boolean isAdmin) {
        super(sender, null);
        this.isAdmin = isAdmin;
    }

    @Override
    public boolean exec() {
        if (!(sender instanceof Player)) {
            return false;
        } else if (!sender.hasPermission("draimshop.createshop")) {
            sender.sendMessage(LangUtils.getString("command-no-perms"));
            return false;
        } else if (!sender.hasPermission("draimshop.admin") && this.isAdmin) {
            sender.sendMessage(LangUtils.getString("command-no-perms"));
            return false;
        }
        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null) {
            player.sendMessage(LangUtils.getString("create.invalid-block"));
            return false;
        }
        CompletableFuture.runAsync(() -> {
            PlayerState state = PlayerState.getPlayerState(player);
            state.clearShopInteractions();
            state.createCreationGUI(this.isAdmin).openFirstPage();
        }).whenComplete((result, throwable) -> Optional.ofNullable(throwable).ifPresent(e -> e.printStackTrace()));
        return false;
    }

    @EventHandler
    public void createShop(InventoryClickEvent evt) {
        ItemStack item = evt.getCurrentItem();
        if (item == null) {
            return;
        }
        Player player = (Player) evt.getWhoClicked();
        PlayerState state = PlayerState.getPlayerState(player);
        CreationGUI gui = state.getCreationGUI();

        if (gui == null) {
            return;
        } else {
            evt.setCancelled(true);
        }

        Inventory interactingInventory = gui.currentInventory();

        if (evt.getClickedInventory().equals(interactingInventory)) {
            this.isAdmin = gui.isAdmin();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta.hasDisplayName()
                    && itemMeta.getDisplayName().equals("§c" + LangUtils.getString("icons.close"))) {
                state.closeCreationGUI();
            } else if (itemMeta.hasDisplayName()
                    && itemMeta.getDisplayName().equals("§e" + LangUtils.getString("icons.previous"))) {
                gui.previousPage();
            } else if (itemMeta.hasDisplayName()
                    && itemMeta.getDisplayName().equals("§e" + LangUtils.getString("icons.next"))) {
                gui.nextPage();
            } else if (evt.getSlot() < 27) {
                Block targetBlock = player.getTargetBlockExact(5);
                int maxShops = getMaxShops(player);
                CompletableFuture<Integer> numbercf = CompletableFuture.supplyAsync(
                        () -> DraimShop.getPlugin().getDB().getTotalShopOwned(player.getUniqueId()));
                numbercf.thenAccept(number -> {
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            state.closeCreationGUI();
                            if (number.intValue() >= maxShops) {
                                player.sendMessage(LangUtils.getString("create.reached-max"));
                                return;
                            }
                            if (targetBlock == null) {
                                player.sendMessage(LangUtils.getString("create.invalid-block"));
                                return;
                            }
                            Location location = getCreationLocation(targetBlock, player);

                            if (!DraimShop.getPlugin().support().hasCreatePerms(location, player)) {
                                player.sendMessage(LangUtils.getString("create.no-perms"));
                                return;
                            }

                            ShopCreator creator = getShopCreator(item);
                            creator.createShop(location, player, item, isAdmin);
                        }
                    };
                    runnable.runTask(DraimShop.getPlugin());
                });
            }

        }
    }

    private static Location getCreationLocation(Block targetBlock, Player player) {
        Location result = targetBlock.getLocation().clone();
        result.add(0.5, 1, 0.5);
        int yaw = ((Float) (player.getLocation().getYaw() + 540)).intValue();
        yaw += 45;
        yaw -= yaw % 90;
        result.setYaw(yaw);
        return result;
    }

    private static ShopCreator getShopCreator(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        ShopCreator creator = DraimShop.getPlugin().support().getShopCreator(item);
        if (creator != null) {
            return creator;
        } else if (!meta.hasDisplayName()) {
            throw new NoSuchShopException();
        } else if (!meta.hasCustomModelData()) {
            throw new NoSuchShopException(meta.getDisplayName());
        } else {
            int model = meta.getCustomModelData();

            int defaultVM = DraimShop.getPlugin().getConfig().getInt("defaults.vending-machine");
            if (defaultVM == model) {
                return new VMCreator();
            }
            Set<String> vm = DraimShop.getPlugin().getConfig().getConfigurationSection("vending-machine")
                    .getKeys(false);
            for (String e : vm) {
                int customModelData = DraimShop.getPlugin().getConfig().getInt("vending-machine." + e + ".model-data");
                if (customModelData == model) {
                    return new VMCreator();
                }
            }
            return new BCCreator();
        }
    }

    private int getMaxShops(Player player) {
        int playerPermissions = player.getEffectivePermissions().stream().filter(permsInfo -> permsInfo.getValue())
                .map(permsInfo -> permsInfo.getPermission()).filter(perm -> perm.startsWith("draimshop.createshop."))
                .map(perm -> perm.replaceFirst("draimshop.createshop.", "")).map(str -> {
                    try {
                        return Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }).reduce(0, (x, y) -> Math.max(x, y));
        return Math.max(playerPermissions, DraimShop.getPlugin().getConfig().getInt("max-shops"));
    }

    private static class NoSuchShopException extends RuntimeException {
        private NoSuchShopException(String name) {
            super("Выбранный товар не соответствовал ни одному виду магазинов: " + name);
        }

        private NoSuchShopException() {
            super("Выбранный товар не соответствовал ни одному виду магазинов!!");
        }
    }
}
