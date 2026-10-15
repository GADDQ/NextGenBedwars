package top.earthstudio.nextgenbedwars.api.spawner;

import org.bukkit.Location;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public final class Spawner { // struct Spawner
    public String type;

    public Location location;
    public int level = 0;

    public transient SpawnerType spawnerType;

    public Spawner() {}
}
