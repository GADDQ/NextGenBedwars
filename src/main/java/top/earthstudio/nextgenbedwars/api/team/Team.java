package top.earthstudio.nextgenbedwars.api.team;

import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.kyori.adventure.text.Component;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Set;
import java.util.UUID;

@ConfigSerializable
public final class Team { // struct Team
    public Component displayName;
    public Color teamColor;

    public int maxPlayerCount;
    public int respawnTick;

    public Location respawnLocation;
    public LongLongPair bedBlockLongs;

    public transient boolean isBedAlive = true;
    public transient final Set<Player> players = new ObjectOpenHashSet<>();
    public transient UUID uuid;

    public Team() {}
}
