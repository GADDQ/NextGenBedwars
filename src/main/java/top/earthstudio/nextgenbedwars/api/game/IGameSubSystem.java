package top.earthstudio.nextgenbedwars.api.game;

public interface IGameSubSystem {
    default void initialize() {
        // TODO: If nobody use this method in release, delete it
    }

    void update();
    void shutdown();
}
