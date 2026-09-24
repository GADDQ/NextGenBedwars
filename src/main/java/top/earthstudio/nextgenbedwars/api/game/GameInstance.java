package top.earthstudio.nextgenbedwars.api.game;

import net.kyori.adventure.text.Component;

import org.bukkit.World;

import java.util.UUID;

public interface GameInstance {
    UUID getWorldUUID();
    Component getDisplayName();
    Game getGame();
    World getWorld();

    void add(IGameSubSystem gameSubSystem);
    <M extends IGameSubSystem> M get(Class<M> type);
}
