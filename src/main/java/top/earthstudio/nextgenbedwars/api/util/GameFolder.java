package top.earthstudio.nextgenbedwars.api.util;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.Game;

import java.io.File;

public final class GameFolder {
    private GameFolder() {}

    /** 返回 <dataFolder>/Game/<gameName>/ 目录（不保证存在）。 */
    public static File of(Game game) {
        return new File(
                new File(BedwarsAPI.getInstance().getPlugin().getDataFolder(), "Game"),
                game.name
        );
    }
}