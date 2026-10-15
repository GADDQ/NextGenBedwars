package top.earthstudio.nextgenbedwars.api.spawner;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class SpawnerType {
    public Material material;
    public List<Integer> spawnTick;

    public boolean isShowHolo = false;

    public Material holoMaterial;
    public Component resourceName;

    public boolean isShowTimer = true;
    public boolean isShowLevel = true;

    public boolean isAllowMerge = true;
    public int maxSpawnCount = 0;

    public SpawnerType() {}
}
