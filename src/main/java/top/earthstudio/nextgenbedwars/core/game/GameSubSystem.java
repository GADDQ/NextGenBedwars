package top.earthstudio.nextgenbedwars.core.game;

import org.bukkit.plugin.java.JavaPlugin;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import top.earthstudio.nextgenbedwars.core.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.core.world.WorldProtectorImpl;

import java.util.ArrayList;
import java.util.List;

public final class GameSubSystem {
    public List<IGameSubSystem> subSystems;

    public GameSubSystem() {
        subSystems = new ArrayList<>();

        subSystems.add(new SpawnerManager());
        subSystems.add(new WorldProtectorImpl());

        subSystems.forEach(IGameSubSystem::initialize);
    }

    public <M extends IGameSubSystem> M get(Class<M> type) {
        for (IGameSubSystem s : subSystems)
            if (type.isInstance(s)) return type.cast(s);
        throw new IllegalStateException("SubSystem not found: " + type.getName());
    }

    public void update() {
        subSystems.forEach(IGameSubSystem::update);
    }

    public void shutdown() {
        subSystems.forEach(IGameSubSystem::shutdown);
        subSystems.clear();
        subSystems = null;
    }
}