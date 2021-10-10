package me.draimgoose.draimshop.player;

import me.draimgoose.draimshop.gui.CreationGUI;
import me.draimgoose.draimshop.gui.ShopGUI;
import me.draimgoose.draimshop.plugin.DraimShop;
import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Optional;

public class PlayerState {
    private static HashMap<Player, PlayerState> playerStates = new HashMap<>();
    private ShopGUI shopGUI;
    private ItemStack transactionItem;
    private Conversation conversation;
    private Player player;
    private BukkitRunnable unlockingShop;
    private ItemStack unlockingShopItem;
    private CreationGUI creationGUI;
    private PlayerState(Player player) {
        this.player = player;
        playerStates.put(player, this);
    }

    public static PlayerState getPlayerState(Player player) {
        PlayerState result = playerStates.get(player);
        if (result == null) {
            return new PlayerState(player);
        } else {
            return result;
        }
    }

    public ItemStack getUnlockingShopItem() {
        return this.unlockingShopItem;
    }

    public void setUnlockingShop(ItemStack shopItem) {
        if (this.unlockingShop != null && !this.unlockingShop.isCancelled())
            this.unlockingShop.cancel();
        this.unlockingShopItem = shopItem;
        this.unlockingShop = new BukkitRunnable() {
            @Override
            public void run() {
                PlayerState.this.unlockingShopItem = null;
                PlayerState.this.unlockingShop = null;
            }
        };
        this.unlockingShop.runTaskLater(DraimShop.getPlugin(), 45);
    }

    public CreationGUI createCreationGUI(boolean isAdmin) {
        this.creationGUI = new CreationGUI(this.player, isAdmin);
        return this.creationGUI;
    }

    public CreationGUI getCreationGUI() {
        return this.creationGUI;
    }

    public void setCreationGUI(CreationGUI gui) {
        this.creationGUI = gui;
    }

    public void closeCreationGUI() {
        Bukkit.getScheduler().runTask(DraimShop.getPlugin(), () -> this.player.closeInventory());
        this.creationGUI = null;
    }

    public void setShopGUI(ShopGUI gui) {
        this.shopGUI = gui;
    }

    public ShopGUI getShopGUI() {
        return this.shopGUI;
    }

    public static Player getInteractingPlayer(ArmorStand armorStand) {
        return playerStates.entrySet().stream()
                .filter(e -> armorStand.getUniqueId()
                        .equals(Optional.ofNullable(e.getValue().shopGUI).map(gui -> gui.getArmorStand())
                                .map(stand -> stand.getUniqueId()).orElse(null)))
                .findFirst().map(e -> e.getKey()).orElse(null);
    }

    public boolean startTransaction(ItemStack item, ConversationFactory factory) {
        if (!player.isConversing()) {
            this.transactionItem = item;
            conversation = factory.buildConversation(player);
            conversation.begin();
            return true;
        } else {
            return false;
        }
    }

    public boolean startConversation(ConversationFactory factory) {
        if (!player.isConversing()) {
            conversation = factory.buildConversation(player);
            conversation.begin();
            return true;
        } else {
            return false;
        }
    }

    private boolean abandonConversation() {
        if (conversation != null) {
            player.abandonConversation(conversation);
            this.conversation = null;
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeTransactionItem() {
        ItemStack clone = transactionItem.clone();
        this.transactionItem = null;
        return clone;
    }

    public void clearShopInteractions() {
        abandonConversation();
        if (this.shopGUI != null) {
            this.shopGUI.saveInventories();
            this.shopGUI = null;
        }
        if (this.creationGUI != null) {
            this.creationGUI = null;
        }
    }

    public static void clearAllShopInteractions() {
        playerStates.forEach((player, state) -> state.clearShopInteractions());
    }
}
