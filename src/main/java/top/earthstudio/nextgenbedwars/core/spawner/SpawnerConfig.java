package top.earthstudio.nextgenbedwars.core.spawner;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.Material;

import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SpawnerConfig {
    private final Config localParams;
    private final Config spawnersConfig;

    public final List<Spawner> spawnerList;

    public SpawnerConfig(GameInstance gameInstance) {
        File gameFolder = GameFolder.of(gameInstance.getGame());

        Config globalParams = new Config("spawner_types.yml");
        this.localParams  = new Config(gameFolder, "spawner_types.yml", globalParams);
        this.spawnersConfig = new Config(gameFolder, "spawners.yml");
        this.spawnerList = new ObjectArrayList<>();

        loadFromConfig(spawnersConfig, localParams);
    }

    private void loadFromConfig(Config spawnsConfig, Config paramsConfig) {
        Object raw = spawnsConfig.read("spawners");
        if (raw == null) return;

        if (!(raw instanceof List<?> list))
            throw new IllegalStateException("'spawners' must be a list in spawners.yml");

        for (Object entry : list) {
            if (!(entry instanceof Map<?, ?> map))
                throw new IllegalStateException("Each spawner entry must be a map");

            String type = (String) map.get("type");
            if (type == null)
                throw new IllegalStateException("Spawner entry missing 'type' field");

            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();

            Location location = new Location(null, x, y, z);
            spawnerList.add(buildSpawner(type, paramsConfig, location));
        }
    }

    private Spawner buildSpawner(String type, Config params, Location location) {
        String prefix = type + ".";

        Material material = requireMaterial(
                params.read(prefix + "material"), type, "material");

        Spawner spawner = new Spawner(
                location,
                params.read(prefix + "spawnTick", 20),
                material
        );

        spawner.isAllowMerge  = params.read(prefix + "isAllowMerge",  true);
        spawner.maxSpawnCount = params.read(prefix + "maxSpawnCount", 0);
        spawner.level         = params.read(prefix + "level",         0);
        spawner.isShowLevel   = params.read(prefix + "isShowLevel",   true);
        spawner.isShowTimer   = params.read(prefix + "isShowTimer",   true);

        spawner.isShowHolo = params.read(prefix + "isShowHolo", false);
        if (spawner.isShowHolo) {
            spawner.holoMaterial = requireMaterial(
                    params.read(prefix + "holoMaterial"), type, "holoMaterial");

            String name = params.read(prefix + "resourceName");
            if (name != null) spawner.resourceName = Component.text(name);
        }

        return spawner;
    }

    private static Material requireMaterial(String name, String type, String field) {
        if (name == null)
            throw new IllegalStateException(
                    "Spawner type '" + type + "' missing required field '" + field + "'");
        Material m = Material.matchMaterial(name);
        if (m == null)
            throw new IllegalStateException(
                    "Unknown material '" + name + "' for spawner type '" + type + "', field '" + field + "'");
        return m;
    }

    public void release() {
        localParams.release();
        spawnersConfig.release();
    }

    // TODO: export
}
