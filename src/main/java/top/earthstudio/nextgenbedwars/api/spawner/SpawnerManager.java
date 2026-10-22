package top.earthstudio.nextgenbedwars.api.spawner;

import top.earthstudio.nextgenbedwars.api.game.IManager;

import java.util.List;

public interface SpawnerManager extends IManager<Spawner> {
    List<String> getTypes();

    SpawnerType getSpawnerType(String typeName);
}
