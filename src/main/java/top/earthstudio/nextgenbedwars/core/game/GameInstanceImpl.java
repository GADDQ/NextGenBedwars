package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;

import org.bukkit.World;

import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.game.SubSystemConstructor;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import top.earthstudio.nextgenbedwars.core.world.WorldManager;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public final class GameInstanceImpl implements GameInstance {
    private final Game game;
    private final Component displayName;
    private UUID worldUUID;
    private World world;
    private Map<Class<? extends IGameSubSystem>, IGameSubSystem> subSystems;

    private boolean isShutdown = false;
    private boolean isReady = false;

    public GameInstanceImpl(Game game, Component displayName, File mapTemplate, WorldReadyListener worldReadyListener, Map<Class<? extends IGameSubSystem>, SubSystemConstructor<?>> subSystemTemplates) {
        this.game = game;
        this.displayName = displayName;
        this.subSystems = new Object2ObjectOpenHashMap<>();

        this.worldUUID = WorldManager.create(mapTemplate);

        worldReadyListener.addTask(worldUUID, world -> {
            if (isShutdown) return;

            this.world = world;
            game.locationsModifier(world);

            buildSubSystems(subSystemTemplates);

            WorldManager.forceLoadRegions(worldUUID, game.region);

            WorldProtector worldProtector = get(WorldProtector.class);
            worldProtector.scanWorldToProtectLater(world, game.region, v -> {
                if (isShutdown) return;
                isReady = true;
            });
        });
    }

    private void buildSubSystems(Map<Class<? extends IGameSubSystem>, SubSystemConstructor<?>> subSystemTemplates) {
        for (var entry : subSystemTemplates.entrySet()) {
            Class<? extends IGameSubSystem> type = entry.getKey();
            SubSystemConstructor<?> constructor = entry.getValue();

            IGameSubSystem instanceSystem = constructor.construct(this);
            subSystems.put(type, instanceSystem);
        }
    }

    public void update() {
        if (!isReady || isShutdown) return;
        subSystems.values().forEach(IGameSubSystem::update);
    }

    public void shutdown() {
        isShutdown = true;

        if (subSystems != null) {
            subSystems.values().forEach(IGameSubSystem::shutdown);
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
        IGameSubSystem system = subSystems.get(type);
        if (system == null) {
            throw new IllegalStateException("SubSystem not registered for type: " + type.getName());
        }
        return type.cast(system);
    }
}