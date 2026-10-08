package top.earthstudio.nextgenbedwars.core.game;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.util.ConfigUtil;

import top.earthstudio.nextgenbedwars.core.world.WorldManager;

import java.io.File;

public final class GameConfig {
    private final GameInstanceImpl gameInstance;
    private final Game game;
    private final File gameFolder;
    private final Config config;

    public GameConfig(GameInstanceImpl gameInstance) {
        this.gameInstance = gameInstance;
        this.game = gameInstance.getGame();

        gameFolder = new File(
                new File(BedwarsAPI.getInstance().getPlugin().getDataFolder(), "Game"),
                game.name
        );
        config = new Config(gameFolder, "game.yml");
    }

    public void load() {
        game.displayName = ConfigUtil.parseComponent(
                config.read("displayName"), "game.yml.displayName");
        game.waitingLobby = ConfigUtil.parseLocation(
                config.read("waitingLobby"), "game.yml.waitingLobby");
        game.spectatorRespawnPoint = ConfigUtil.parseLocation(
                config.read("spectatorRespawnPoint"), "game.yml.spectatorRespawnPoint");
        game.region = ConfigUtil.parseRegion(
                config.read("region"), "game.yml.region");

        game.mapTemplate = new File(gameFolder, "map");
    }

    public void save(boolean isSaveWorldOnly) {
        File mapDir = new File(gameFolder, "map");
        WorldManager.exportWorld(gameInstance.getWorldUUID(), mapDir);

        if (isSaveWorldOnly)
            return;

        config.write("displayName", ConfigUtil.buildComponent(game.displayName));
        config.write("waitingLobby", ConfigUtil.buildLocation(game.waitingLobby));
        config.write("spectatorRespawnPoint", ConfigUtil.buildLocation(game.spectatorRespawnPoint));
        config.write("region", ConfigUtil.buildRegion(game.region));

        config.save();
    }

    public void release() {
        config.release();
    }
}