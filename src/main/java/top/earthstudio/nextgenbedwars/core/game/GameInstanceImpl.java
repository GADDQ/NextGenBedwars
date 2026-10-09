package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.kyori.adventure.text.Component;

import org.bukkit.World;

import org.joml.Vector3i;
import top.earthstudio.nextgenbedwars.api.game.*;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import top.earthstudio.nextgenbedwars.core.world.WorldManager;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

import java.util.Map;
import java.util.UUID;

public final class GameInstanceImpl implements GameInstance {
    public GameState gameState;

    private final Game game;
    private UUID worldUUID;
    private World world;
    private Map<Class<? extends IGameSubSystem>, IGameSubSystem> subSystems;

    private GameConfig gameConfig;
    private boolean isShutdown = false;
    private boolean isReady = false;
    private boolean isEditMode = false;

    public GameInstanceImpl(Game game, WorldReadyListener worldReadyListener, Map<Class<? extends IGameSubSystem>, SubSystemConstructor<?>> subSystemTemplates, boolean isEditMode) {
        this.game = game;

        this.gameConfig = new GameConfig(this);
        gameConfig.load();

        this.subSystems = new Object2ObjectOpenHashMap<>();

        if (game.mapTemplate == null)
            this.worldUUID = WorldManager.createEmptyTemp();
        else
            this.worldUUID = WorldManager.create(game.mapTemplate);

        worldReadyListener.addTask(worldUUID, world -> {
            if (isShutdown) return;

            this.world = world;
            game.locationsModifier(world);

            buildSubSystems(subSystemTemplates);

            WorldManager.forceLoadRegions(worldUUID, game.region);

            if (isEditMode) {
                gameState = GameState.EDITING;
            } else {
                WorldProtector worldProtector = get(WorldProtector.class);

                if (game.region.equals(new ObjectObjectImmutablePair<>(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0)))) { // TODO: Config allow set no protection
                    setReady();
                    return;
                }

                worldProtector.scanWorldToProtectLater(world, game.region, v -> {
                    setReady();
                });
            }
        });
    }

    private void setReady() {
        if (isShutdown) return;
        isReady = true;

        this.gameState = GameState.PLAYING; // TODO: DEBUG ONLY
        // gameState = GameState.WAITING; // TODO
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
        if (gameState != GameState.PLAYING) return;

        subSystems.values().forEach(IGameSubSystem::update);
    }

    public void shutdown() {
        if (gameState == GameState.EDITING)
            gameConfig.save(true); // World should be saved in any time, but configs not.

        gameConfig.release();
        gameConfig = null;

        gameState = null;
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

    @Override
    public void saveGame() {
        gameConfig.save(false);
    }

    @Override public UUID getWorldUUID() { return worldUUID; }
    @Override public Component getDisplayName() { return game.displayName; }
    @Override public Game getGame() { return game; }
    @Override public World getWorld() { return world; }
    @Override public boolean isEditMode() { return isEditMode; }

    @Override
    public <M extends IGameSubSystem> M get(Class<M> type) {
        IGameSubSystem system = subSystems.get(type);
        if (system == null) {
            throw new IllegalStateException("SubSystem not registered for type: " + type.getName());
        }
        return type.cast(system);
    }

    /*
    *TODO LISTS:
    * 1. Killer Check
    * 2. Fake Spectator mode
    * 3. Player join/leave server logic handler
    * 4. Scoreboard INFO show
    * 5. Velocity Proxy Support
    * 6. Fast buy UI
    * 7. Data Base
    * 8. upgrade + upgrade shop
    * 9. lock resource when fight
    * **/
}