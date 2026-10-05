package top.earthstudio.nextgenbedwars.core.team;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

import top.earthstudio.nextgenbedwars.core.team.listener.TeamInteractListener;
import top.earthstudio.nextgenbedwars.core.team.listener.TeamRespawnListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamManagerImpl implements TeamManager {
    private Map<UUID, Team> teams;
    private Map<Color, Team> teamsByColor;

    private TeamInteractListener teamInteractListener;
    private TeamRespawnListener teamRespawnListener;

    private World world;

    public TeamManagerImpl(GameInstance gameInstance) {
        teams = new Object2ObjectOpenHashMap<>();
        teamsByColor = new Object2ObjectOpenHashMap<>();
        teamInteractListener = new TeamInteractListener(this, gameInstance);
        teamRespawnListener = new TeamRespawnListener(this, gameInstance);
        world = gameInstance.getWorld();
        Bukkit.getPluginManager().registerEvents(teamInteractListener, BedwarsAPI.getInstance().getPlugin());
        Bukkit.getPluginManager().registerEvents(teamRespawnListener, BedwarsAPI.getInstance().getPlugin());
    }

    @Override
    public UUID add(Team team) {
        UUID uuid = UUID.randomUUID();

        team.respawnLocation.setWorld(world);
        teams.put(uuid, team);
        teamsByColor.put(team.teamColor, team);
        return uuid;
    }

    @Override
    public Team get(UUID uuid) {
        return teams.get(uuid);
    }

    @Override
    public Team get(Color color) {
        return teamsByColor.get(color);
    }

    @Override
    public Team get(Player player) {
        for (Team team : teams.values()) {
            if (team.players.contains(player))
                return team;
        }
        return null;
    }

    @Override
    public List<Team> getAll() {
        List<Team> teamList = new ObjectArrayList<>();
        teamList.addAll(teams.values());
        return teamList;
    }

    @Override
    public void remove(UUID uuid) {
        teamsByColor.remove(teams.remove(uuid).teamColor);
    }

    @Override
    public void shutdown() {
        world = null;

        HandlerList.unregisterAll(teamRespawnListener);
        HandlerList.unregisterAll(teamInteractListener);
        teamRespawnListener = null;
        teamInteractListener = null;

        teamsByColor.clear();
        teamsByColor = null;
        teams.clear();
        teams = null;
    }
}
