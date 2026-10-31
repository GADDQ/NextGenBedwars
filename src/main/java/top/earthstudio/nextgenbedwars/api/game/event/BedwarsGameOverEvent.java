package top.earthstudio.nextgenbedwars.api.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;

public class BedwarsGameOverEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public final GameInstance gameInstance;
    public final Team winningTeam;

    public BedwarsGameOverEvent(GameInstance gameInstance, Team team) {
        this.gameInstance = gameInstance;
        this.winningTeam = team;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
