package top.earthstudio.nextgenbedwars.api.game;

import org.bukkit.World;

public interface GameAsset {
    default void bindWorld(World world) {}
}
