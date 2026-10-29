package top.earthstudio.nextgenbedwars.core.command.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;
import top.earthstudio.nextgenbedwars.core.game.GameManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CommonCommand {

    public static void build(LiteralArgumentBuilder<CommandSourceStack> root) {
        CommonCommand handler = new CommonCommand();

        root.then(
                Commands.literal("join")
                        .requires(source -> source.getSender() instanceof Player)
                        .then(
                                Commands.argument("game", StringArgumentType.word())
                                        .suggests(handler::suggestJoinableGames)
                                        .executes(handler::executeJoin)
                        )
        );
    }

    private CompletableFuture<Suggestions> suggestJoinableGames(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        for (GameInstance instance : GameManager.getRunningInstances()) {
            if (instance.getGameState() != GameState.WAITING && instance.getGameState() != GameState.STARTING) {
                continue;
            }

            String gameName = instance.getGame().name;
            if (gameName.toLowerCase().startsWith(remaining)) {
                int currentPlayers = instance.getWorld().getPlayerCount();
                int maxPlayers = instance.getGame().maxPlayers;

                Component tooltip = (instance.getDisplayName() != null ? instance.getDisplayName() : Component.text(gameName))
                        .append(Component.text(" (" + currentPlayers + "/" + maxPlayers + ")").color(NamedTextColor.GRAY));

                builder.suggest(gameName, MessageComponentSerializer.message().serialize(tooltip));
            }
        }

        return builder.buildFuture();
    }

    private int executeJoin(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        String query = StringArgumentType.getString(ctx, "game");

        GameInstance target = null;

        try {
            UUID uuid = UUID.fromString(query);
            target = GameManager.getInstance(uuid);
        } catch (IllegalArgumentException ignored) {}

        if (target == null) {
            List<GameInstance> matches = GameManager.getInstancesByName(query);
            if (matches != null && !matches.isEmpty()) {
                target = matches.stream()
                        .filter(i -> !i.isEditMode() && (i.getGameState() == GameState.WAITING || i.getGameState() == GameState.STARTING))
                        .filter(i -> i.getWorld().getPlayerCount() < i.getGame().maxPlayers)
                        .findFirst()
                        .orElse(matches.getFirst());
            }
        }

        if (target == null || target.isEditMode()) {
            player.sendMessage(Component.text("未找到可加入的房间: " + query).color(NamedTextColor.RED));
            return 0;
        }

        if (target.getGameState() == GameState.PLAYING || target.getGameState() == GameState.ENDING) {
            player.sendMessage(Component.text("该房间游戏已经开始或正在结算中，无法加入！").color(NamedTextColor.RED));
            return 0;
        }

        if (target.getWorld().getPlayerCount() >= target.getGame().maxPlayers) {
            player.sendMessage(Component.text("该房间人数已满！").color(NamedTextColor.RED));
            return 0;
        }

        Location targetLoc = target.getGame().waitingLobby != null
                ? target.getGame().waitingLobby
                : target.getWorld().getSpawnLocation();

        player.teleport(targetLoc);
        player.sendMessage(Component.text("正在加入房间: ").color(NamedTextColor.GREEN)
                .append(target.getDisplayName() != null ? target.getDisplayName() : Component.text(target.getGame().name)));

        return Command.SINGLE_SUCCESS;
    }
}