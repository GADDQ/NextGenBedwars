package top.earthstudio.nextgenbedwars.core.spawner;

import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerManager;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.UUID;

public class SpawnerManagerImpl implements SpawnerManager {
    private Map<UUID, SpawnerEntity> spawners;

    public SpawnerManagerImpl() {
        spawners = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void update() {
        spawners.forEach((uuid, spawnerEntity) -> spawnerEntity.update());
    }

    @Override
    public UUID add(Spawner spawner) {
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
        spawners.values().forEach(SpawnerEntity::destroy);
        spawners.clear();
        spawners = null;
    }
}
