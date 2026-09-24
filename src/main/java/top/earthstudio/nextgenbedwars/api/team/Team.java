package top.earthstudio.nextgenbedwars.api.team;

import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.kyori.adventure.text.Component;

import org.bukkit.Color;
import org.bukkit.Location;

import top.earthstudio.nextgenbedwars.api.spawner.Spawner;

import java.util.Set;
import java.util.UUID;

public final class Team { // struct Team
    public final Component displayName;
    public final Color teamColor;

    public final int maxPlayerCount;
    public int respawnTick;

    public Location respawnLocation;
    public final LongLongPair bedBlockLongs;

    // TODO: upgrade struct?

    public final Set<Spawner> teamSpawners = new ObjectOpenHashSet<>();

    public final Set<UUID> players = new ObjectOpenHashSet<>();

    public Team(Component displayName, Color teamColor, int maxPlayerCount, int respawnTick, Location respawnLocation, LongLongPair bedBlockLongs) {
        this.displayName = displayName;
        this.teamColor = teamColor;
        this.maxPlayerCount = maxPlayerCount;
        this.respawnTick = respawnTick;
        this.respawnLocation = respawnLocation;
        this.bedBlockLongs = bedBlockLongs;
    }
}
