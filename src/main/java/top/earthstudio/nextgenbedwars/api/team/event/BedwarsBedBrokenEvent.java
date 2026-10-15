package top.earthstudio.nextgenbedwars.api.team.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;

public class BedwarsBedBrokenEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public GameInstance gameInstance;
    public Team team;
    public Player bedBreaker;

    public BedwarsBedBrokenEvent(GameInstance gameInstance, Team team, Player bedBreaker) {
        this.gameInstance = gameInstance;
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
