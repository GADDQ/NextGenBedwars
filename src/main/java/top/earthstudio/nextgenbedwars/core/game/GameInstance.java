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

    private WorldProtectListener worldProtectListener;
    private boolean isShutdown = false;
    private boolean isReady = false;

    public GameInstance(Game game, GameSubSystem gameSubSystem, Component displayName, File mapTemplate, WorldReadyListener worldReadyListener) {
        this.game = game;
        this.gameSubSystem = gameSubSystem;

        this.displayName = displayName;
        this.mapTemplate = mapTemplate;

        worldUUID = WorldManager.create(mapTemplate);

        worldReadyListener.addTask(worldUUID, world -> {
            if (isShutdown) return;

            game.locationsModifier(world);
            WorldManager.forceLoadRegions(worldUUID, game.region);
            WorldProtector worldProtector = gameSubSystem.get(WorldProtector.class);

            this.worldProtectListener = new WorldProtectListener(worldProtector, world);
            Bukkit.getPluginManager().registerEvents(worldProtectListener, BedwarsAPI.getInstance().getPlugin());

            worldProtector.scanWorldToProtectLater(world, game.region, uuid -> {
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

        if (worldProtectListener != null)
            HandlerList.unregisterAll(worldProtectListener);

        worldProtectListener = null;

        gameSubSystem.shutdown();
        gameSubSystem = null;

        game = null;

        WorldManager.destroy(worldUUID);

        worldUUID = null;
    }
}
