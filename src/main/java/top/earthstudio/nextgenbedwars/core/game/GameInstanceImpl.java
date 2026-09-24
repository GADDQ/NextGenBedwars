package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;

import org.bukkit.World;

import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import top.earthstudio.nextgenbedwars.core.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.core.team.TeamManager;
import top.earthstudio.nextgenbedwars.core.world.WorldManager;
import top.earthstudio.nextgenbedwars.core.world.WorldProtectorImpl;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.util.List;
import java.util.UUID;

public final class GameInstanceImpl implements GameInstance {
    private final Game game;
    private final Component displayName;
    private UUID worldUUID;
    private World world;
    private List<IGameSubSystem> subSystems;

    private boolean isShutdown = false;
    private boolean isReady = false;

    public GameInstanceImpl(Game game, Component displayName, File mapTemplate, WorldReadyListener worldReadyListener) {
        this.game = game;
        this.displayName = displayName;

        this.worldUUID = WorldManager.create(mapTemplate);

        worldReadyListener.addTask(worldUUID, world -> {
            if (isShutdown) return;

            this.world = world;
            game.locationsModifier(world);

            this.subSystems = new ObjectArrayList<>();
            buildSubSystems();

            WorldManager.forceLoadRegions(worldUUID, game.region);

            WorldProtector worldProtector = get(WorldProtector.class);
            worldProtector.scanWorldToProtectLater(world, game.region, v -> {
                if (isShutdown) return;
                isReady = true;
            });
        });
    }

    @Override
    public void add(IGameSubSystem gameSubSystem) {
        subSystems.add(gameSubSystem);
    }

    private void buildSubSystems() { // TODO: Isolate registerListener method to clean constructor
        subSystems.add(new SpawnerManager());
        subSystems.add(new WorldProtectorImpl(world));
        subSystems.add(new TeamManager(this));
    }

    public void update() {
        if (!isReady || isShutdown) return;
        subSystems.forEach(IGameSubSystem::update);
    }

    public void shutdown() {
        isShutdown = true;

        if (subSystems != null) {
            subSystems.forEach(IGameSubSystem::shutdown);
            subSystems.clear();
            subSystems = null;
        }

        WorldManager.destroy(worldUUID);
        worldUUID = null;
        world = null;
    }

    // ================= 接口实现 =================
    @Override public UUID getWorldUUID() { return worldUUID; }
    @Override public Component getDisplayName() { return displayName; }
    @Override public Game getGame() { return game; }
    @Override public World getWorld() { return world; }

    @Override
    public <M extends IGameSubSystem> M get(Class<M> type) {
        for (IGameSubSystem s : subSystems) {
            if (type.isInstance(s)) return type.cast(s);
        }
        throw new IllegalStateException("SubSystem not found: " + type.getName());
    }
}