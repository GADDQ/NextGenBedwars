package top.earthstudio.nextgenbedwars.core.spawner;

import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerType;

import java.util.Map;

public class SpawnerTypeConfig extends IConfig<Map<String, SpawnerType>> {
    public SpawnerTypeConfig(GameInstance gameInstance) {
        super(gameInstance, "spawner_types.yml", true, "spawnerTypes");
    }

    @Override
    public Map<String, SpawnerType> load() {
        return super.load();
    }

    public void save(Map<String, SpawnerType> spawnerTypes) {
        super.save(spawnerTypes);
    }
}
