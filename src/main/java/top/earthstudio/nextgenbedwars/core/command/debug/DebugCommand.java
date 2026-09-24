package top.earthstudio.nextgenbedwars.core.command.debug;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.NextGenBedwars;

import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;

import top.earthstudio.nextgenbedwars.core.game.GameInstance;
import top.earthstudio.nextgenbedwars.core.game.GameManager;
import top.earthstudio.nextgenbedwars.core.game.GameSubSystem;
import top.earthstudio.nextgenbedwars.core.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.core.world.WorldManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DebugCommand {
    private JavaPlugin plugin;
    private List<UUID> gameUuids = new ArrayList<>();
    private List<UUID> spawnerUuids = new ArrayList<>();

    private GameInstance activeGame;

    public DebugCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("bw").then(
                Commands.literal("debug").then(
                        Commands.literal("spawner").then(
                                Commands.literal("add").executes(this::addSpawner)
                        ).then(
                                Commands.literal("removeAll").executes(this::removeAllSpawner)
                        )
                ).then(
                        Commands.literal("game").then(
                                Commands.literal("add").executes(this::addGame)
                        ).then(
                                Commands.literal("switchTo").then(
                                        Commands
                                                .argument("targetGame", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    String remaining = builder.getRemaining().toLowerCase();

                                                    for (UUID uuid : gameUuids) {
                                                        String option = uuid.toString();
                                                        // 根据玩家当前已输入的字符做前缀匹配过滤
                                                        if (option.toLowerCase().startsWith(remaining)) {
                                                            builder.suggest(option);
                                                        }
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(this::switchToGame)
                                )
                        ).then(
                                Commands.literal("removeAll").executes(this::removeAllGame)
                        )
                )
        );

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(root.build()));
    }

    private int switchToGame(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        activeGame = GameManager.getInstance(UUID.fromString(commandSourceStackCommandContext.getArgument("targetGame", String.class)));
        ((Player) commandSourceStackCommandContext.getSource().getSender()).teleport(new Location(WorldManager.get(activeGame.worldUUID), 0, 0, 0));
        return 0;
    }

    private int addSpawner(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        spawnerUuids.add(activeGame.gameSubSystem.get(SpawnerManager.class).add(
                new Spawner(
                        ((Player) commandSourceStackCommandContext.getSource().getSender()).getLocation(),
                        20,
                        Material.COPPER_INGOT,
                        true,
                        Material.COPPER_BLOCK,
                        Component.text("铜锭").color(NamedTextColor.RED),
                        true,
                        1,
                        32,
                        false
                )
        ));

        return 0;
    }

    private int removeAllSpawner(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        spawnerUuids.forEach(uuid -> activeGame.gameSubSystem.get(SpawnerManager.class).remove(uuid));
        spawnerUuids.clear();
        return 0;
    }

    private int addGame(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        Game game = new Game(
                new Location(null, 0, 0, 0),
                new Location(null, 0, 0, 0),
                new ObjectObjectImmutablePair<>(new Vector3i(100, 320, 100), new Vector3i(-100, -64, -100)),
                new ArrayList<>()
        );

        UUID uuid = GameManager.addInstance(game, Component.text("test"), new File(Bukkit.getWorldContainer(), "testworld"));
        Player player = (Player) commandSourceStackCommandContext.getSource().getSender();
        NextGenBedwars.worldReadyListener.addTask(GameManager.getInstance(uuid).worldUUID, world -> {
            player.teleport(new Location(world, 0, 0, 0));
        });
        activeGame = GameManager.getInstance(uuid);
        gameUuids.add(uuid);
        return 0;
    }

    private int removeAllGame(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        gameUuids.forEach(GameManager::removeInstance);
        gameUuids.clear();
        return 0;
    }

    public void shutdown() {
        spawnerUuids.clear();
        gameUuids.clear();
        spawnerUuids = null;
        gameUuids = null;
        plugin = null;
    }
}
