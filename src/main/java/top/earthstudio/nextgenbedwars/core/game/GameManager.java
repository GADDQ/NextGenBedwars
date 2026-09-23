package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private static Map<UUID, GameInstance> gameInstances;
    private static WorldReadyListener worldReadyListener;

    private GameManager() {};

    static public void initialize(WorldReadyListener worldReadyListener) {
        gameInstances = new Object2ObjectOpenHashMap<>();
        GameManager.worldReadyListener = worldReadyListener;
    }

    static public void shutdown() {
        gameInstances.forEach(((uuid, gameInstance) -> {
            GlobalTicker.remove(uuid);
        }));
        gameInstances.clear();
        gameInstances = null;
    }

    static public UUID addInstance(Game game, GameSubSystem gameSubSystem, String displayName, File mapTemplate) {
        UUID uuid = UUID.randomUUID();

        GameInstance gameInstance = new GameInstance(game, gameSubSystem, displayName, mapTemplate, worldReadyListener);

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

    static public void removeInstance(UUID uuid) {
        gameInstances.remove(uuid);
        GlobalTicker.remove(uuid);
    }
}
