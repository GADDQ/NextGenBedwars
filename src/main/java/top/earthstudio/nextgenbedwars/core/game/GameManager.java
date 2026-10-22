package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.World;

import org.bukkit.entity.Player;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.game.SubSystemConstructor;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecutor;
import top.earthstudio.nextgenbedwars.api.shop.tier.ProductTierManager;
import top.earthstudio.nextgenbedwars.api.shop.ShopManager;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;
import top.earthstudio.nextgenbedwars.api.world.WorldProtector;

import top.earthstudio.nextgenbedwars.core.shop.executor.ShopExecutorImpl;
import top.earthstudio.nextgenbedwars.core.shop.tier.ProductTierManagerImpl;
import top.earthstudio.nextgenbedwars.core.shop.ShopManagerImpl;
import top.earthstudio.nextgenbedwars.core.spawner.SpawnerManagerImpl;
import top.earthstudio.nextgenbedwars.core.team.TeamManagerImpl;
import top.earthstudio.nextgenbedwars.core.world.WorldProtectorImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameManager {
    private static Map<UUID, GameInstance> gameInstances;
    private static Map<Class<? extends IGameSubSystem>, SubSystemConstructor<?>> SUB_SYSTEM_TEMPLATES;

    private GameManager() {}

    static public void initialize() { // TODO: ability to edit instance map and save to template
        gameInstances = new Object2ObjectOpenHashMap<>();
        SUB_SYSTEM_TEMPLATES = new Object2ObjectOpenHashMap<>();

        registerSubSystem(SpawnerManager.class, SpawnerManagerImpl::new);
        registerSubSystem(WorldProtector.class, WorldProtectorImpl::new);
        registerSubSystem(TeamManager.class, TeamManagerImpl::new);
        registerSubSystem(ShopManager.class, ShopManagerImpl::new);
        registerSubSystem(ProductTierManager.class, ProductTierManagerImpl::new);
        registerSubSystem(ShopExecutor.class, ShopExecutorImpl::new);
    }

    static public void shutdown() {
        SUB_SYSTEM_TEMPLATES.clear();
        SUB_SYSTEM_TEMPLATES = null;
        gameInstances.forEach(((uuid, gameInstance) -> GlobalTicker.remove(uuid)));
        gameInstances.clear();
        gameInstances = null;
    }

    static public UUID addInstance(Game game, boolean isEditMode) {
        UUID uuid = UUID.randomUUID();

        GameInstanceImpl gameInstance = new GameInstanceImpl(game, SUB_SYSTEM_TEMPLATES, isEditMode);

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

    static public GameInstance getInstanceByWorld (World world) {
        for (Map.Entry<UUID, GameInstance> entry : gameInstances.entrySet()) {
            if (entry.getValue().getWorld() == world)
                return entry.getValue();
        }
        return null;
    }

    public static List<GameInstance> getInstancesByName(String name) {
        if (name == null) return null;

        List<GameInstance> instances = new ObjectArrayList<>();
        for (GameInstance instance : gameInstances.values()) {
            if (name.equals(instance.getGame().name)) {
                instances.add(instance);
            }
        }
        return instances.isEmpty() ? null : instances;
    }

    static public void removeInstance(UUID uuid) {
        gameInstances.remove(uuid);
        GlobalTicker.remove(uuid);
    }

    static public void removeInstanceByWorldUuid (UUID worldUUID) {
        for (Map.Entry<UUID, GameInstance> entry : gameInstances.entrySet()) {
            if (worldUUID.equals(entry.getValue().getWorldUUID())) {
                removeInstance(entry.getKey());
                return;
            }
        }
    }

    static public void removeInstanceByName (String name) {
        List<UUID> removes = new ArrayList<>();

        for (Map.Entry<UUID, GameInstance> entry : gameInstances.entrySet()) {
            if (entry.getValue().getGame().name.equals(name))
                removes.add(entry.getKey());
        }

        removes.forEach(GameManager::removeInstance);
        removes.clear();
    }

    public static <T extends IGameSubSystem> void registerSubSystem(Class<T> type, SubSystemConstructor<T> constructor) {
        SUB_SYSTEM_TEMPLATES.put(type, constructor);
    }
}
