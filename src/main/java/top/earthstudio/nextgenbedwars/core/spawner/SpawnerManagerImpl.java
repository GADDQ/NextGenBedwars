package top.earthstudio.nextgenbedwars.core.spawner;

import org.bukkit.World;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerManager;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.UUID;

public class SpawnerManagerImpl implements SpawnerManager {
    private Map<UUID, SpawnerEntity> spawners;
    private World world;

    public SpawnerManagerImpl(GameInstance gameInstance) {
        spawners = new Object2ObjectOpenHashMap<>();
        world = gameInstance.getWorld();
    }

    @Override
    public void update() {
        spawners.forEach((uuid, spawnerEntity) -> spawnerEntity.update());
    }

    @Override
    public UUID add(Spawner spawner) {
        spawner.location.setWorld(world);
        UUID uuid = UUID.randomUUID();
        spawners.put(uuid, new SpawnerEntity(spawner));
        return uuid;
    }

    @Override
    public Spawner get(UUID uuid) {
        return spawners.get(uuid).spawner;
    }

    @Override
    public void remove(UUID uuid) {
        spawners.remove(uuid).destroy();
    }

    @Override
    public void shutdown() {
        world = null;
        spawners.values().forEach(SpawnerEntity::destroy);
        spawners.clear();
        spawners = null;
    }
}
