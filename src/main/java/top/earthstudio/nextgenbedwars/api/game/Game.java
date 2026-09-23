package top.earthstudio.nextgenbedwars.api.game;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import org.bukkit.Location;
import org.bukkit.World;

import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.spawner.Spawner;

import java.util.List;

public final class Game {
    public Location waitingLobby;
    public Location spectatorRespawnPoint;
    public ObjectObjectImmutablePair<Vector3i, Vector3i> region;

    public List<Spawner> spawners;

    /* TODO: Game Information
     *    e.g. Team...
     *  */
    public Game(Location waitingLobby, Location spectatorRespawnPoint, ObjectObjectImmutablePair<Vector3i, Vector3i> region, List<Spawner> spawners) {
        this.waitingLobby = waitingLobby;
        this.spectatorRespawnPoint = spectatorRespawnPoint;
        this.region = region;
        this.spawners = spawners;
    }

    public void locationsModifier(World world) {
        waitingLobby.setWorld(world);
        spectatorRespawnPoint.setWorld(world);

        spawners.forEach(spawner -> {
            spawner.location.setWorld(world);
        });
    }
}
