package me.draimgoose.draimshop.shop;

import me.draimgoose.draimshop.player.PlayerState;
import me.draimgoose.draimshop.plugin.DSComd;
import me.draimgoose.draimshop.plugin.DraimShop;
import me.draimgoose.draimshop.utils.LangUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class SetShopCount extends DSComd {
    private static ConversationFactory confirmingConversation;
    private int newCount;

    public SetShopCount(CommandSender sender, String[] args) {
        super(sender, args);
        initConversationFactory();
    }

    @Override
    public boolean exec() {
        if (!sender.hasPermission("draimshop.setcount") || !(sender instanceof Player)) {
            sender.sendMessage(LangUtils.getString("command-no-perms"));
            return false;
        }
        if (args.length < 3) {
            sender.sendMessage("§cНедопустимое количество аргументов!");
            return false;
        }
        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage("§cНе удается найти указанного игрока, возможно его нет в сети!");
            return true;
        }
        try {
            this.newCount = Integer.parseInt(SetShopCount.this.args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cНеверный ввод цифры!");
            return false;
        }
        PlayerState state = PlayerState.getPlayerState(player);
        if (!state.startConversation(confirmingConversation)) {
            player.sendMessage("§cВы все еще заняты другим делом!");
        }
        return true;
    }

    private void initConversationFactory() {
        Plugin plugin = DraimShop.getPlugin();
        confirmingConversation = new ConversationFactory(plugin).withFirstPrompt(new AmountPrompt()).withModality(false)
                .withLocalEcho(false)
                .withConversationCanceller(new InactivityConversationCanceller(DraimShop.getPlugin(), 10))
                .addConversationAbandonedListener(new ConversationAbandonedListener() {
                    @Override
                    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
                        ConversationCanceller canceller = abandonedEvent.getCanceller();
                        Player player = (Player) abandonedEvent.getContext().getForWhom();
                        if (canceller != null) {
                            player.sendMessage("§cОперация отменена...");
                        }
                        PlayerState.getPlayerState(player).clearShopInteractions();
                    }
                });
    }

    private class AmountPrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return "§9Убедитесь, что у игрока есть правильное количество существующих магазинов, прежде чем давать команду. "
                    + "Подтвердить сброс? (да/нет)";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (context.getForWhom() instanceof Player) {
                Player player = (Player) context.getForWhom();
                switch (input) {
                    case "да":
                        try {
                            CompletableFuture<Void> voidcf = CompletableFuture.runAsync(() -> DraimShop.getPlugin()
                                    .getDB().setShopsOwned(player.getUniqueId(), SetShopCount.this.newCount));
                            voidcf.thenRun(() -> player
                                    .sendMessage("§aОбщее количество магазинов игроков установлено на " + SetShopCount.this.newCount + "!"));
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cНеверный ввод номера!");
                        }
                        break;
                    case "нет":
                        player.sendMessage("§cОперация отменена...");
                        break;
                    default:
                        player.sendMessage("§cНеверный ввод!");
                        break;
                }
            } else {
                context.getForWhom().sendRawMessage("Это команда доступна только для игроков.");
            }
            return END_OF_CONVERSATION;
        }
    }

}
