package top.earthstudio.nextgenbedwars.api.spawner;

import org.bukkit.Location;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.UUID;

@ConfigSerializable
public final class Spawner { // struct Spawner
    public String type;

    public Location location;
    public int level = 0;

    public transient SpawnerType spawnerType;
    public transient UUID uuid;

    public Spawner() {}
}
