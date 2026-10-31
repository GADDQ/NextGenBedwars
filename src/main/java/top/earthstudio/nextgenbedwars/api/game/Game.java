package top.earthstudio.nextgenbedwars.api.game;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.World;

import org.joml.Vector3i;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.File;
import java.util.UUID;

@ConfigSerializable
public final class Game {
    public String name;
    public Component displayName;
    public int maxPlayers;
    public int minPlayers = 2;
    public Location waitingLobbyLocation;
    public Location spectatorRespawnLocation;
    public ObjectObjectImmutablePair<Vector3i, Vector3i> region;

    public transient File mapTemplate;
    public transient UUID uuid;

    public Game() {}

    public void locationsModifier(World world) {
        if (waitingLobbyLocation != null)
            waitingLobbyLocation.setWorld(world);

        if (spectatorRespawnLocation != null)
            spectatorRespawnLocation.setWorld(world);
    }
}
