package me.draimgoose.draimshop.shop.conversation;

import me.draimgoose.draimshop.gui.ShopGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PruchaseConvFactory extends ConversationFactory {

    public PruchaseConvFactory() {
        super(DraimShop.getPlugin());
        this.firstPrompt = new PurchaseConversation();
        this.isModal = false;
        this.localEchoEnabled = false;
        this.abandonedListeners.add(new ConversationAbandonedListener() {
            @Override
            public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
                ConversationCanceller canceller = abandonedEvent.getCanceller();
                Player player;
                player = (Player) abandonedEvent.getContext().getForWhom();
                if (canceller != null) {
                    player.sendMessage(LangUtils.getString("purchase-convo-cancelled"));
                }
                PlayerState.getPlayerState(player).clearShopInteractions();
            }
        });
        this.cancellers.add(new InactivityConversationCanceller(plugin, 10));
    }

    private static class PurchaseConversation extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return LangUtils.getString("purchase-convo-prompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (context.getForWhom() instanceof Player) {
                Player player = (Player) context.getForWhom();
                PlayerState state = PlayerState.getPlayerState(player);
                ItemStack purchasingItem = state.removeTransactionItem();
                ShopGUI ui = state.getShopGUI();
                try {
                    int inputInt = Integer.parseInt(input);
                    double inputDouble = Double.parseDouble(input);

                    if (inputInt != inputDouble || inputDouble <= 0) {
                        player.sendMessage(LangUtils.getString("invalid-input"));
                    } else if (context.getForWhom() instanceof Player) {
                        ui.purchaseItem(purchasingItem, inputInt);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(LangUtils.getString("invalid-input"));
                }
            } else {
                context.getForWhom().sendRawMessage("Эта команда доступна только для игроков.");
            }
            return END_OF_CONVERSATION;
        }
    }
}
