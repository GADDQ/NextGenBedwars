package top.earthstudio.nextgenbedwars.api.game;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import org.bukkit.Location;
import org.bukkit.World;

import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.shop.Shop;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.team.Team;

import java.util.List;

public final class Game {
    public Location waitingLobby;
    public Location spectatorRespawnPoint;
    public ObjectObjectImmutablePair<Vector3i, Vector3i> region;

    public List<Spawner> spawners;
    public List<Team> teams;
    public List<Shop> shops;

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
        // TODO
//        teams.forEach(team -> {
//            team.respawnLocation.setWorld(world);
//        });
//        shops.forEach(shop -> {
//            shop.location.setWorld(world);
//        });
    }
}
