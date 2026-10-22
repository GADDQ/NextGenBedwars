package top.earthstudio.nextgenbedwars.core.command.edit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerType;

import top.earthstudio.nextgenbedwars.core.game.GameManager;

import java.util.concurrent.CompletableFuture;

public class EditCommand {

    public static void build(LiteralArgumentBuilder<CommandSourceStack> root) {
        EditCommand handler = new EditCommand();

        root.then(
                Commands.literal("spawner")
                        .requires(handler::requireInEditingWorld)
                        .then(
                                Commands.literal("add").then(
                                        Commands.argument("type", StringArgumentType.word())
                                                .suggests(handler::suggestSpawnerTypes)
                                                .executes(handler::executeAddSpawner)
                                )
                        )
                        .then(Commands.literal("remove").executes(handler::executeRemoveSpawner))
        ).then(
                Commands.literal("team")
                        .requires(handler::requireInEditingWorld)
                        .then(Commands.literal("add").executes(handler::stub))
                        .then(Commands.literal("remove").executes(handler::stub))
                        .then(Commands.literal("setBed").executes(handler::stub))
                        .then(Commands.literal("setRespawn").executes(handler::stub))
        ).then(
                Commands.literal("shop")
                        .requires(handler::requireInEditingWorld)
                        .then(Commands.literal("add").executes(handler::stub))
                        .then(Commands.literal("remove").executes(handler::stub))
        ).then(
                Commands.literal("setLobby")
                        .requires(handler::requireInEditingWorld)
                        .executes(handler::executeSetLobby)
        ).then(
                Commands.literal("setSpectator")
                        .requires(handler::requireInEditingWorld)
                        .executes(handler::executeSetSpectator)
        ).then(
                Commands.literal("save")
                        .requires(handler::requireInEditingWorld)
                        .executes(handler::executeSave)
        ).then(
                Commands.literal("exit")
                        .requires(handler::requireInEditingWorld)
                        .executes(handler::executeExit)
        );
    }

    private boolean requireInEditingWorld(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player player)) return false;
        if (!player.hasPermission("nextgenbedwars.admin.edit")) return false;

        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        return instance != null && instance.isEditMode();
    }

    private CompletableFuture<Suggestions> suggestSpawnerTypes(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        if (instance == null) return builder.buildFuture();

        String remaining = builder.getRemaining().toLowerCase();
        SpawnerManager spawnerManager = instance.get(SpawnerManager.class);

        for (String type : spawnerManager.getTypes()) {
            if (type.toLowerCase().startsWith(remaining)) {
                builder.suggest(type);
            }
        }
        return builder.buildFuture();
    }

    private int executeAddSpawner(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        SpawnerManager spawnerManager = instance.get(SpawnerManager.class);

        String typeName = StringArgumentType.getString(ctx, "type");
        SpawnerType spawnerType = spawnerManager.getSpawnerType(typeName);

        if (spawnerType == null) {
            player.sendMessage(Component.text("未知的生成器类型: " + typeName).color(NamedTextColor.RED));
            return 0;
        }

        Spawner spawner = new Spawner();
        spawner.type = typeName;
        spawner.location = player.getLocation();
        spawner.level = 0;
        spawner.spawnerType = spawnerType;

        spawnerManager.add(spawner);
        player.sendMessage(Component.text("成功放置生成器: " + typeName).color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeRemoveSpawner(CommandContext<CommandSourceStack> ctx) {
        // TODO: 移出最近的生成器
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetLobby(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        instance.getGame().waitingLobby = player.getLocation();
        player.sendMessage(Component.text("已将当前位置设为等待大厅 (waitingLobby)！").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetSpectator(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());
        assert instance != null;
        instance.getGame().spectatorRespawnPoint = player.getLocation();
        player.sendMessage(Component.text("已将当前位置设为观察者重生点 (spectatorRespawnPoint)！").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeSave(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());

        assert instance != null;
        instance.saveGame();
        player.sendMessage(Component.text("已成功保存地图与所有子系统配置！").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeExit(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        GameInstance instance = GameManager.getInstanceByWorld(player.getWorld());

        player.sendMessage(Component.text("正在退出编辑模式...").color(NamedTextColor.YELLOW));

        assert instance != null;
        GameManager.removeInstanceByName(instance.getGame().name);

        Location lobby = Bukkit.getWorlds().getFirst().getSpawnLocation();
        player.teleport(lobby);

        player.sendMessage(Component.text("已退出编辑模式").color(NamedTextColor.GREEN));

        player.updateCommands();

        return Command.SINGLE_SUCCESS;
    }

    private int stub(CommandContext<CommandSourceStack> ctx) {
        return Command.SINGLE_SUCCESS;
    }
}