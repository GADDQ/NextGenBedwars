package top.earthstudio.nextgenbedwars.api.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import org.bukkit.Location;
import org.bukkit.World;

import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.shop.Shop;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.team.Team;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class Game {
    public File mapTemplate;

    public Location waitingLobby;
    public Location spectatorRespawnPoint;
    public ObjectObjectImmutablePair<Vector3i, Vector3i> region;

    public Game(File mapTemplate, Location waitingLobby, Location spectatorRespawnPoint, ObjectObjectImmutablePair<Vector3i, Vector3i> region) {
        this.mapTemplate = mapTemplate;
        this.waitingLobby = waitingLobby;
        this.spectatorRespawnPoint = spectatorRespawnPoint;
        this.region = region;
    }

    public void locationsModifier(World world) {
        waitingLobby.setWorld(world);
        spectatorRespawnPoint.setWorld(world);
    }
}
