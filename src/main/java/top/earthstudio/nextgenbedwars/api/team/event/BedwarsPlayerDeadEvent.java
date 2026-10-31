package top.earthstudio.nextgenbedwars.api.team.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;

public class BedwarsPlayerDeadEvent extends Event implements Cancellable { // TODO: who cause the death? uncancellable condition?
    private static final HandlerList HANDLERS = new HandlerList();

    public final GameInstance gameInstance;
    public final Player player;
    public final boolean isFinalDeath;

    private boolean isCancelled = false;

    public BedwarsPlayerDeadEvent(GameInstance gameInstance, Player player, boolean isFinalDeath) {
        this.gameInstance = gameInstance;
        this.player = player;
        this.isFinalDeath = isFinalDeath;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }
}
