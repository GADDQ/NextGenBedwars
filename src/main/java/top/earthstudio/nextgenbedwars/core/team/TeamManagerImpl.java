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

import top.earthstudio.nextgenbedwars.api.world.WorldProtector;
import top.earthstudio.nextgenbedwars.core.team.listener.TeamChatListener;
import top.earthstudio.nextgenbedwars.core.team.listener.TeamInteractListener;
import top.earthstudio.nextgenbedwars.core.team.listener.TeamRespawnListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.shuffle;

public class TeamManagerImpl implements TeamManager { // TODO: disconnect / rejoin player support
    private Map<UUID, Team> teams;
    private Map<Color, Team> teamsByColor;

    private TeamInteractListener teamInteractListener;
    private TeamRespawnListener teamRespawnListener;
    private TeamChatListener teamChatListener;

    private World world;

    private TeamConfig teamConfig;

    private GameInstance gameInstance;

    public TeamManagerImpl(GameInstance gameInstance) {
        teams = new Object2ObjectOpenHashMap<>();
        teamsByColor = new Object2ObjectOpenHashMap<>();
        world = gameInstance.getWorld();

        if (!gameInstance.isEditMode()) {
            teamInteractListener = new TeamInteractListener(this, gameInstance);
            teamRespawnListener = new TeamRespawnListener(this, gameInstance);
            teamChatListener = new TeamChatListener(this, gameInstance);

            Bukkit.getPluginManager().registerEvents(teamInteractListener, BedwarsAPI.getInstance().plugin());
            Bukkit.getPluginManager().registerEvents(teamRespawnListener, BedwarsAPI.getInstance().plugin());
            Bukkit.getPluginManager().registerEvents(teamChatListener, BedwarsAPI.getInstance().plugin());
        }

        this.gameInstance = gameInstance;

        this.teamConfig = new TeamConfig(gameInstance);
        teamConfig.getValues().forEach(this::add);
    }

    @Override
    public UUID add(Team team) {
        UUID uuid = UUID.randomUUID();

        if (team.respawnLocation != null)
            team.respawnLocation.setWorld(world);

        team.uuid = uuid;
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
    public void randomAssignTeam(List<Player> players) {
        List<Player> playerList = new ObjectArrayList<>(players);
        List<Team> teamList = getAll();
        shuffle(teamList);
        shuffle(playerList);

        int i = 0;
        for (Player player : playerList) {
            teamList.get(i % teamList.size()).players.add(player);
            i++;
        }
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
    public void save() {
        teamConfig.save(getAll());
    }

    @Override
    public void shutdown() {
        teamConfig.release();
        teamConfig = null;

        world = null;

        if (!gameInstance.isEditMode()) {
            HandlerList.unregisterAll(teamChatListener);
            HandlerList.unregisterAll(teamRespawnListener);
            HandlerList.unregisterAll(teamInteractListener);
            teamChatListener = null;
            teamRespawnListener = null;
            teamInteractListener = null;
        }

        this.gameInstance = null;

        teamsByColor.clear();
        teamsByColor = null;
        teams.clear();
        teams = null;
    }

    @Override
    public void onReady() {
        if (gameInstance.isEditMode())
            return;

        WorldProtector protector = gameInstance.get(WorldProtector.class);

        for (Team team: teams.values()) {
            protector.removeBlockFromWorldGroup(team.bedBlockLongs.firstLong());
            protector.removeBlockFromWorldGroup(team.bedBlockLongs.secondLong());
        }
    }
}
