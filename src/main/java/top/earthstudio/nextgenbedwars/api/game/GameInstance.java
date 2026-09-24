package top.earthstudio.nextgenbedwars.api.game;

import net.kyori.adventure.text.Component;

import org.bukkit.World;

import java.util.UUID;

public interface GameInstance {
    UUID getWorldUUID();
    Component getDisplayName();
    Game getGame();
    World getWorld();

    <M extends IGameSubSystem> M get(Class<M> type);
}
