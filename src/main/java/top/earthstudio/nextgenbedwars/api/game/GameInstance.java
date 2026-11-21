package top.earthstudio.nextgenbedwars.api.game;

import net.kyori.adventure.text.Component;

import org.bukkit.World;

import java.util.UUID;

public interface GameInstance {
    UUID getWorldUUID();
    Component getDisplayName();
    Game getGame();
    void setGameState(GameState gameState);
    GameState getGameState();
    World getWorld();
    boolean isEditMode();
    void saveGame();
    UUID getUUID();

    <M extends IGameSubSystem> M get(Class<M> type);
    <T extends IGameSubSystem> void inject(Class<T> type, SubSystemConstructor<T> constructor);
    <M extends IGameSubSystem> void eject(Class<M> type);
}
