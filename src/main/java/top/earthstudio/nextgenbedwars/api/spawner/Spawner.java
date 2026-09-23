package top.earthstudio.nextgenbedwars.api.spawner;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;

public final class Spawner { // struct Spawner
    public Location location;
    public int spawnTick;
    public Material material;
    public boolean isShowHolo = false;
    public Material holoMaterial;
    public Component resourceName;
    public boolean isShowTimer = true;
    public int level = 0;
    public boolean isShowLevel = true;

    public int maxSpawnCount = 0;
    public boolean isAllowMerge = true;

    public Spawner(Location location, int spawnTick, Material material) {
        this.location = location;
        this.spawnTick = spawnTick;
        this.material = material;
    }

    public Spawner(Location location, int spawnTick, Material material, boolean isShowHolo, Material holoMaterial, Component resourceName, boolean isShowTimer, int level) {
        this(location, spawnTick, material);
        this.isShowHolo = isShowHolo;
        this.holoMaterial = holoMaterial;
        this.resourceName = resourceName;
        this.isShowTimer = isShowTimer;
        this.level = level;
    }

    public Spawner(Location location, int spawnTick, Material material, boolean isShowHolo, Material holoMaterial, Component resourceName, boolean isShowTimer, int level, boolean isShowLevel) {
        this(location, spawnTick, material, isShowHolo, holoMaterial, resourceName, isShowTimer, level);
        this.isShowLevel = isShowLevel;
    }

    public Spawner(Location location, int spawnTick, Material material, boolean isShowHolo, Material holoMaterial, Component resourceName, boolean isShowTimer, int level, boolean isShowLevel, int maxSpawnCount) {
        this(location, spawnTick, material, isShowHolo, holoMaterial, resourceName, isShowTimer, level, isShowLevel);
        this.maxSpawnCount = maxSpawnCount;
    }

    public Spawner(Location location, int spawnTick, Material material, boolean isShowHolo, Material holoMaterial, Component resourceName, boolean isShowTimer, int level, int maxSpawnCount) {
        this(location, spawnTick, material, isShowHolo, holoMaterial, resourceName, isShowTimer, level);
        this.maxSpawnCount = maxSpawnCount;
    }

    public Spawner(Location location, int spawnTick, Material material, boolean isShowHolo, Material holoMaterial, Component resourceName, boolean isShowTimer, int level, int maxSpawnCount, boolean isAllowMerge) {
        this(location, spawnTick, material, isShowHolo, holoMaterial, resourceName, isShowTimer, level, maxSpawnCount);
        this.isAllowMerge = isAllowMerge;
    }
}
