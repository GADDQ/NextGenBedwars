package top.earthstudio.nextgenbedwars.core.game;


import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import top.earthstudio.nextgenbedwars.core.world.WorldManager;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldProtectListener;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.util.UUID;

public final class GameInstance {
    public Game game;
    public GameSubSystem gameSubSystem;

    public Component displayName;
    public File mapTemplate;
    public UUID worldUUID;

    private boolean isShutdown = false;
    private boolean isReady = false;

    public GameInstance(Game game, Component displayName, File mapTemplate, WorldReadyListener worldReadyListener) {
        this.game = game;

        this.displayName = displayName;
        this.mapTemplate = mapTemplate;

        worldUUID = WorldManager.create(mapTemplate);

        worldReadyListener.addTask(worldUUID, world -> {
            if (isShutdown) return;

            game.locationsModifier(world);
            this.gameSubSystem = new GameSubSystem(world);
            WorldManager.forceLoadRegions(worldUUID, game.region);
            WorldProtector worldProtector = gameSubSystem.get(WorldProtector.class);

            worldProtector.scanWorldToProtectLater(world, game.region, v -> {
                if (isShutdown) return;

                isReady = true;
            });
        });
    }

    public void update() {
        if (!isReady || isShutdown) {
            return;
        }
        gameSubSystem.update();
    }

    public void shutdown() {
        isShutdown = true;

        gameSubSystem.shutdown();
        gameSubSystem = null;

        game = null;

        WorldManager.destroy(worldUUID);

        worldUUID = null;
    }
}
