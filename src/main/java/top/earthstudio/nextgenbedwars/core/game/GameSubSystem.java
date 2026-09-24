package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.World;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import top.earthstudio.nextgenbedwars.core.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.core.team.TeamManager;
import top.earthstudio.nextgenbedwars.core.world.WorldProtectorImpl;

import java.util.List;

public final class GameSubSystem {
    private List<IGameSubSystem> subSystems;

    public World world;

    public GameSubSystem(World world) {
        this.world = world;
        subSystems = new ObjectArrayList<>();

        subSystems.add(new SpawnerManager());
        subSystems.add(new WorldProtectorImpl(this));
        subSystems.add(new TeamManager(this));
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
        world = null;
    }
}