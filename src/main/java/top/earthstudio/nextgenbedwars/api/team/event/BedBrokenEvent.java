package top.earthstudio.nextgenbedwars.api.team.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.team.Team;

import java.util.UUID;

public class BedBrokenEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public Team team;
    public Player bedBreaker;
    // public Game game;

    public BedBrokenEvent(Team team, Player bedBreaker) {
        this.team = team;
        this.bedBreaker = bedBreaker;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
