package top.earthstudio.nextgenbedwars.api.game;

import java.util.UUID;

public interface IManager<T> extends IGameSubSystem {
    default void update() {

    }
    UUID add(T object);
    T get(UUID uuid);
    void remove(UUID uuid);
    void shutdown();
}
