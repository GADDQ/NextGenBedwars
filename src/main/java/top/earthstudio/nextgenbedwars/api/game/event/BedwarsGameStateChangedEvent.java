package top.earthstudio.nextgenbedwars.api.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;

public class BedwarsGameStateChangedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public final GameInstance gameInstance;
    public final GameState gameState;

    public BedwarsGameStateChangedEvent(GameInstance gameInstance, GameState gameState) {
        this.gameInstance = gameInstance;
        this.gameState = gameState;
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
