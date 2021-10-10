package me.draimgoose.draimshop.shop.conversation;

import me.draimgoose.draimshop.gui.BriefcaseGUI;
import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class RetriveConvFactory extends ConversationFactory {

    public RetriveConvFactory() {
        super(DraimShop.getPlugin());
        this.firstPrompt = new RetrieveConversation();
        this.isModal = false;
        this.localEchoEnabled = false;
        this.abandonedListeners.add(new ConversationAbandonedListener() {
            @Override
            public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
                ConversationCanceller canceller = abandonedEvent.getCanceller();
                Player player = (Player) abandonedEvent.getContext().getForWhom();
                if (canceller != null) {
                    player.sendMessage(LangUtils.getString("retrieve-convo-cancelled"));
                }
                PlayerState.getPlayerState(player).clearShopInteractions();
            }
        });
        this.cancellers.add(new InactivityConversationCanceller(plugin, 10));
    }

    private static class RetrieveConversation extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return LangUtils.getString("retrieve-convo-prompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (context.getForWhom() instanceof Player) {
                Player player = (Player) context.getForWhom();
                PlayerState state = PlayerState.getPlayerState(player);
                BriefcaseGUI ui = (BriefcaseGUI) state.getShopGUI();
                try {
                    int inputInt = Integer.parseInt(input);
                    double inputDouble = Double.parseDouble(input);

                    if (inputInt != inputDouble || inputDouble <= 0) {
                        player.sendMessage(LangUtils.getString("invalid-input"));
                    } else if (context.getForWhom() instanceof Player) {
                        ui.retrieveItem(inputInt);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(LangUtils.getString("invalid-input"));
                }
            } else {
                context.getForWhom().sendRawMessage("Это команда только для игроков.");
            }
            return END_OF_CONVERSATION;
        }
    }
}
