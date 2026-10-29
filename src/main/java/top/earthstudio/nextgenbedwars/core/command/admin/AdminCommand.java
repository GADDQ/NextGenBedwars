package top.earthstudio.nextgenbedwars.core.command.admin;

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

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.codehaus.plexus.util.FileUtils;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.Game;

import top.earthstudio.nextgenbedwars.core.game.GameManager;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class AdminCommand {
    public static void build(LiteralArgumentBuilder<CommandSourceStack> root) {
        AdminCommand handler = new AdminCommand();

        root.then(
                Commands.literal("create")
                        .requires(source -> source.getSender() instanceof Player p && p.hasPermission("nextgenbedwars.admin.create"))
                        .then(
                                Commands.argument("name", StringArgumentType.word())
                                        .executes(handler::executeCreate)
                        )
        ).then(
                Commands.literal("edit")
                        .requires(source -> source.getSender() instanceof Player p && p.hasPermission("nextgenbedwars.admin.edit"))
                        .then(
                                Commands.argument("game", StringArgumentType.word())
                                        .suggests(handler::suggestExistingGames)
                                        .executes(handler::executeEdit)
                        )
        ).then(
                Commands.literal("remove")
                        .requires(source -> source.getSender() instanceof Player p && p.hasPermission("nextgenbedwars.admin.remove"))
                        .then(
                                Commands.argument("game", StringArgumentType.word())
                                        .suggests(handler::suggestExistingGames)
                                        .executes(handler::executeRemove)
                        )
        ).then(
                Commands.literal("enable")
                        .requires(source -> source.getSender() instanceof Player p && p.hasPermission("nextgenbedwars.admin.toggle"))
                        .then(
                                Commands.argument("game", StringArgumentType.word())
                                        .suggests(handler::suggestExistingGames)
                                        .executes(handler::executeEnable)
                        )
        ).then(
                Commands.literal("disable")
                        .requires(source -> source.getSender() instanceof Player p && p.hasPermission("nextgenbedwars.admin.toggle"))
                        .then(
                                Commands.argument("game", StringArgumentType.word())
                                        .suggests(handler::suggestExistingGames)
                                        .executes(handler::executeDisable)
                        )
        );
    }

    public CompletableFuture<Suggestions> suggestExistingGames(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        File gameBaseDir = new File(BedwarsAPI.getInstance().plugin().getDataFolder(), "Game");
        File[] gameFolders = gameBaseDir.listFiles(File::isDirectory);

        if (gameFolders != null) {
            for (File folder : gameFolders) {
                String gameName = folder.getName();
                if (gameName.toLowerCase().startsWith(remaining)) {
                    builder.suggest(gameName);
                }
            }
        }
        return builder.buildFuture();
    }

    private int executeCreate(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        Player player = (Player) ctx.getSource().getSender();

        File gameDir = new File(new File(BedwarsAPI.getInstance().plugin().getDataFolder(), "Game"), name);
        if (gameDir.exists()) {
            player.sendMessage(Component.text("游戏 " + name + " 已存在！如需编辑请使用: /bw edit " + name).color(NamedTextColor.RED));
            return 0;
        }

        player.sendMessage(Component.text("正在准备创建游戏: " + name).color(NamedTextColor.YELLOW));

        Game game = new Game();
        game.name = name;

        WorldReadyListener.addTask(GameManager.getInstance(GameManager.addInstance(game, true)).getWorldUUID(), world -> {
            player.teleport(new Location(world, 0.5, 65, 0.5));
            player.sendMessage(Component.text("游戏已创建！已自动进入编辑模式").color(NamedTextColor.GREEN));
            player.updateCommands();
        });

        return Command.SINGLE_SUCCESS;
    }

    private int executeEdit(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "game");
        Player player = (Player) ctx.getSource().getSender();

        File gameDir = new File(new File(BedwarsAPI.getInstance().plugin().getDataFolder(), "Game"), name);
        if (!gameDir.exists()) {
            player.sendMessage(Component.text("未找到游戏 " + name + "！请先使用 /bw create " + name).color(NamedTextColor.RED));
            return 0;
        }

        if (GameManager.getInstancesByName(name) != null) {
            player.sendMessage(Component.text("游戏 " + name + " 当前正在被编辑或游玩，无法进入编辑模式！").color(NamedTextColor.RED));
            return 0;
        }

        player.sendMessage(Component.text("正在加载地图并进入编辑模式: " + name).color(NamedTextColor.GREEN));

        Game game = new Game();
        game.name = name;

        WorldReadyListener.addTask(GameManager.getInstance(GameManager.addInstance(game, true)).getWorldUUID(), world -> {
            Location targetLoc = (game.waitingLobby != null) ? game.waitingLobby : ((game.spectatorRespawnPoint != null) ? game.spectatorRespawnPoint : world.getSpawnLocation());
            player.teleport(targetLoc);
            player.sendMessage(Component.text("您正在编辑游戏: " + name).color(NamedTextColor.GREEN));
            player.updateCommands();
        });

        return Command.SINGLE_SUCCESS;
    }

    private int executeRemove(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "game");
        Player player = (Player) ctx.getSource().getSender();

        player.sendMessage(Component.text("正在删除游戏: " + name).color(NamedTextColor.YELLOW));

        GameManager.removeInstanceByName(name);

        File gameBaseDir = new File(BedwarsAPI.getInstance().plugin().getDataFolder(), "Game");
        File targetGameDir = new File(gameBaseDir, name);

        try {
            FileUtils.deleteDirectory(targetGameDir);
            player.sendMessage(Component.text("已成功删除游戏: " + name).color(NamedTextColor.GREEN));
        } catch (IOException e) {
            player.sendMessage(Component.text("删除游戏失败: " + name).color(NamedTextColor.RED));
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeEnable(CommandContext<CommandSourceStack> ctx) { // TODO: auto open new Instance?
        String name = StringArgumentType.getString(ctx, "game");
        Player player = (Player) ctx.getSource().getSender();

        File gameDir = new File(new File(BedwarsAPI.getInstance().plugin().getDataFolder(), "Game"), name);
        if (!gameDir.exists()) {
            player.sendMessage(Component.text("未找到游戏 " + name + "！请先使用 /bw create " + name).color(NamedTextColor.RED));
            return 0;
        }

        Game game = new Game();
        game.name = name;

        GameManager.addInstance(game, false);
        player.sendMessage(Component.text("已启用游戏: " + name).color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int executeDisable(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "game");
        Player player = (Player) ctx.getSource().getSender();

        GameManager.removeInstanceByName(name);
        player.sendMessage(Component.text("已禁用游戏: " + name).color(NamedTextColor.GRAY));
        return Command.SINGLE_SUCCESS;
    }
}