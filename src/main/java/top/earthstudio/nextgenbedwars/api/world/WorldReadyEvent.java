package top.earthstudio.nextgenbedwars.api.world;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorldReadyEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public final UUID worldUUID;
    public final World world;
    public final boolean isSuccess;

    public WorldReadyEvent(UUID worldUUID, World world, boolean isSuccess) {
        this.worldUUID = worldUUID;
        this.world = world;
        this.isSuccess = isSuccess;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
