package top.earthstudio.nextgenbedwars.core.spawner;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.World;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerManager;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpawnerManagerImpl implements SpawnerManager {
    private Map<UUID, SpawnerEntity> spawners;
    private World world;

    private SpawnerConfig spawnerConfig;

    public SpawnerManagerImpl(GameInstance gameInstance) {
        spawners = new Object2ObjectOpenHashMap<>();
        world = gameInstance.getWorld();

        spawnerConfig = new SpawnerConfig(gameInstance);
        spawnerConfig.getValues().forEach(this::add);
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
    public List<Spawner> getAll() {
        List<Spawner> spanwerList =  new ObjectArrayList<>();
        spawners.values().forEach(spawnerEntity -> spanwerList.add(spawnerEntity.spawner));
        return spanwerList;
    }

    @Override
    public List<String> getTypes() {
        return spawnerConfig.getTypes();
    }

    @Override
    public SpawnerType getSpawnerType(String typeName) {
        return spawnerConfig.getType(typeName);
    }

    @Override
    public void remove(UUID uuid) {
        spawners.remove(uuid).destroy();
    }

    @Override
    public void save() {
        spawnerConfig.save(getAll());
    }

    @Override
    public void shutdown() {
        spawnerConfig.release();
        spawnerConfig = null;

        world = null;
        spawners.values().forEach(SpawnerEntity::destroy);
        spawners.clear();
        spawners = null;
    }
}
