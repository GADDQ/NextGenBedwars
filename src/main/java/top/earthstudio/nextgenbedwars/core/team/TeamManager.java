package top.earthstudio.nextgenbedwars.core.team;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.IManager;
import top.earthstudio.nextgenbedwars.api.team.Team;

import top.earthstudio.nextgenbedwars.core.team.listener.TeamInteractListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamManager implements IManager<Team> {
    private Map<UUID, TeamInstance> teams;
    private Map<Color, TeamInstance> teamsByColor;

    private TeamInteractListener teamInteractListener;

    public TeamManager(GameInstance gameInstance) {
        teams = new Object2ObjectOpenHashMap<>();
        teamsByColor = new Object2ObjectOpenHashMap<>();
        teamInteractListener = new TeamInteractListener(this, gameInstance);
        Bukkit.getPluginManager().registerEvents(teamInteractListener, BedwarsAPI.getInstance().getPlugin());
    }

    @Override
    public UUID add(Team team) {
        UUID uuid = UUID.randomUUID();
        TeamInstance teamInstance = new TeamInstance(team);

        teams.put(uuid, teamInstance);
        teamsByColor.put(team.teamColor, teamInstance);
        return uuid;
    }

    @Override
    public Team get(UUID uuid) {
        return teams.get(uuid).team;
    }

    public Team get(Color color) {
        return teamsByColor.get(color).team;
    }

    public Team get(Player player) {
        UUID uuid = player.getUniqueId();
        for (TeamInstance instance : teams.values()) {
            if (instance.team.players.contains(uuid))
                return instance.team;
        }
        return null; // 零 heap 内存分配，极速
    }

    public TeamInstance getInstance(Team team) {
        for (TeamInstance instance : teams.values())
            if (instance.team == team) return instance;
        return null;
    }

    public List<Team> getAll() {
        List<Team> teamList = new ObjectArrayList<>();
        teams.values().forEach(teamInstance -> teamList.add(teamInstance.team));
        return teamList;
    }

    @Override
    public void remove(UUID uuid) {
        TeamInstance teamInstance = teams.remove(uuid);
        teamsByColor.remove(teamInstance.team.teamColor);
        teamInstance.destroy();
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(teamInteractListener);
        teamInteractListener = null;

        teamsByColor.clear();
        teamsByColor = null;
        teams.clear();
        teams = null;
    }
}
