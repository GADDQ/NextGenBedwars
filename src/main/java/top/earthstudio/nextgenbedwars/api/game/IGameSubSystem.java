package top.earthstudio.nextgenbedwars.api.game;

public interface IGameSubSystem {
    void update();
    void shutdown();
    default void onReady() {}
}
