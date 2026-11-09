package top.earthstudio.nextgenbedwars.core.spawner;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerType;

import java.util.List;
import java.util.Map;

public class SpawnerConfig extends Config<List<Spawner>> {
    public SpawnerConfig(GameInstance gameInstance) {
        super(gameInstance, "spawners.yml", false, "spawners");
    }

    public List<Spawner> load(Map<String, SpawnerType> spawnerTypes) {
        ObjectArrayList<Spawner> spawners = new ObjectArrayList<>(super.load());

        spawners.forEach(spawner -> spawner.spawnerType = spawnerTypes.get(spawner.type));

        return spawners;
    }

    @Override
    public void save(List<Spawner> spawners) {
        super.save(spawners);
    }
}
