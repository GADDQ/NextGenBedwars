package top.earthstudio.nextgenbedwars.core.game;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;

import org.joml.Vector3i;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.util.ConfigParser;

import java.io.File;

public final class GameConfig {
    private GameConfig() {}

    public static Game load(String name) {
        File gameFolder = new File(
                new File(BedwarsAPI.getInstance().getPlugin().getDataFolder(), "Game"),
                name
        );
        Config config = new Config(gameFolder, "game.yml");

        try {
            Component displayName = ConfigParser.parseComponent(
                    config.read("displayName"), "game.yml.displayName");
            Location waitingLobby = ConfigParser.parseLocation(
                    config.read("waitingLobby"), "game.yml.waitingLobby");
            Location spectatorRespawnPoint = ConfigParser.parseLocation(
                    config.read("spectatorRespawnPoint"), "game.yml.spectatorRespawnPoint");
            ObjectObjectImmutablePair<Vector3i, Vector3i> region = ConfigParser.parseRegion(
                    config.read("region"), "game.yml.region");

            File mapTemplate = new File(gameFolder, "map");

            return new Game(name, displayName, mapTemplate,
                    waitingLobby, spectatorRespawnPoint, region);
        } finally {
            config.release();
        }
    }

    public static void save(Game game) {
        // TODO
    }
}