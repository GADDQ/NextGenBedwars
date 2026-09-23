package top.earthstudio.nextgenbedwars.api.game;

public interface IGameSubSystem {
    default void initialize() {

    }

    void update();
    void shutdown();
}
