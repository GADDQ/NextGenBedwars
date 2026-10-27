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
    public Location waitingLobby;
    public Location spectatorRespawnPoint;
    public ObjectObjectImmutablePair<Vector3i, Vector3i> region;

    public transient File mapTemplate;
    public transient UUID uuid;

    public Game() {}

    public void locationsModifier(World world) {
        if (waitingLobby != null)
            waitingLobby.setWorld(world);

        if (spectatorRespawnPoint != null)
            spectatorRespawnPoint.setWorld(world);
    }
}
