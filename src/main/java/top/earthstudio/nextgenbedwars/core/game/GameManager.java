package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;

import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.game.SubSystemConstructor;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;

import top.earthstudio.nextgenbedwars.api.world.WorldProtector;
import top.earthstudio.nextgenbedwars.core.spawner.SpawnerManagerImpl;
import top.earthstudio.nextgenbedwars.core.team.TeamManagerImpl;
import top.earthstudio.nextgenbedwars.core.world.WorldProtectorImpl;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private static Map<UUID, GameInstance> gameInstances;
    private static WorldReadyListener worldReadyListener;
    private static final Map<Class<? extends IGameSubSystem>, SubSystemConstructor<?>> SUB_SYSTEM_TEMPLATES =
            new Object2ObjectOpenHashMap<>();

    private GameManager() {};

    static public void initialize(WorldReadyListener worldReadyListener) {
        gameInstances = new Object2ObjectOpenHashMap<>();
        GameManager.worldReadyListener = worldReadyListener;

        registerSubSystem(SpawnerManager.class, game -> new SpawnerManagerImpl());
        registerSubSystem(WorldProtector.class, game -> new WorldProtectorImpl(game.getWorld()));
        registerSubSystem(TeamManager.class, TeamManagerImpl::new);
    }

    static public void shutdown() {
        gameInstances.forEach(((uuid, gameInstance) -> {
            GlobalTicker.remove(uuid);
        }));
        gameInstances.clear();
        gameInstances = null;
    }

    static public UUID addInstance(Game game, Component displayName, File mapTemplate) {
        UUID uuid = UUID.randomUUID();

        GameInstanceImpl gameInstance = new GameInstanceImpl(game, displayName, mapTemplate, worldReadyListener, SUB_SYSTEM_TEMPLATES);

        GlobalTicker.set(uuid, new TickerTask() {
            @Override
            public void shutdown() {
                gameInstance.shutdown();
            }

            @Override
            public void run() {
                gameInstance.update();
            }
        });

        gameInstances.put(uuid, gameInstance);
        return uuid;
    }

    static public GameInstance getInstance(UUID uuid) {
        return gameInstances.get(uuid);
    }

    static public void editInstance(UUID uuid) {
        // TODO: ability to edit instance map and save to template
    }

    static public void removeInstance(UUID uuid) {
        gameInstances.remove(uuid);
        GlobalTicker.remove(uuid);
    }

    public static <T extends IGameSubSystem> void registerSubSystem(Class<T> type, SubSystemConstructor<T> constructor) {
        SUB_SYSTEM_TEMPLATES.put(type, constructor);
    }
}
