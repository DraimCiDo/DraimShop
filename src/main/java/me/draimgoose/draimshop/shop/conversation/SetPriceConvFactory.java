package me.draimgoose.draimshop.shop.conversation;

import me.draimgoose.draimshop.gui.ShopGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetPriceConvFactory extends ConversationFactory {
    private ItemStack item;

    public SetPriceConvFactory(ItemStack item) {
        super(DraimShop.getPlugin());
        this.item = item.clone();
        this.firstPrompt = new PricePrompt();
        this.localEchoEnabled = false;
        this.isModal = false;
        this.abandonedListeners.add(new ConversationAbandonedListener() {
            @Override
            public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
                ConversationCanceller canceller = abandonedEvent.getCanceller();
                Player player = (Player) abandonedEvent.getContext().getForWhom();
                if (canceller != null) {
                    player.sendMessage(LangUtils.getString("price-convo-cancelled"));
                }
                PlayerState.getPlayerState(player).clearShopInteractions();
            }
        });
        this.cancellers.add(new InactivityConversationCanceller(DraimShop.getPlugin(), 10));
    }

    private class PricePrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return LangUtils.getString("price-convo-prompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (context.getForWhom() instanceof Player) {
                Player player = (Player) context.getForWhom();
                PlayerState state = PlayerState.getPlayerState(player);
                ShopGUI ui = state.getShopGUI();
                try {
                    double price = Math.floor((Double.parseDouble(input) * 100)) / 100;
                    price = Math.min(price, DraimShop.getPlugin().getConfig().getDouble("max-price"));
                    if (DraimShop.getPlugin().getConfig().getBoolean("round-down-price"))
                        price = Math.floor(price);

                    if (price <= 0) {
                        player.sendMessage(LangUtils.getString("invalid-input"));
                    } else {
                        player.sendMessage(ui.listPrice(item, price));
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(LangUtils.getString("invalid-input"));
                }
            } else {
                context.getForWhom().sendRawMessage("This is a player-only command.");
            }
            return END_OF_CONVERSATION;
        }
    }

}
