package top.earthstudio.nextgenbedwars.api.prop;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

import java.util.function.Supplier;

public interface PropManager extends IGameSubSystem {
    @Override
    default void update() {}

    void create(String id, Supplier<? extends IProp> factory);
    IProp get(String id);
    void remove(String id);
}
