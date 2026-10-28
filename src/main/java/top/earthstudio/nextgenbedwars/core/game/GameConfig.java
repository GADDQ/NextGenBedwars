package top.earthstudio.nextgenbedwars.core.game;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import top.earthstudio.nextgenbedwars.api.config.ConfigurateHelper;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;
import top.earthstudio.nextgenbedwars.core.world.WorldManager;

import java.io.File;

public final class GameConfig {
    private final GameInstanceImpl gameInstance;
    private final Game game;
    private final File gameFolder;
    private final File configFile;
    private final YamlConfigurationLoader loader;

    public GameConfig(GameInstanceImpl gameInstance) {
        this.gameInstance = gameInstance;
        this.game = gameInstance.getGame();
        this.gameFolder = GameFolder.of(game);
        this.configFile = new File(gameFolder, "game.yml");
        this.loader = ConfigurateHelper.loader(configFile);
    }

    public void load() {
        if (!configFile.exists()) {
            return;
        }

        try {
            CommentedConfigurationNode root = loader.load();
            Game loaded = root.get(Game.class);
            if (loaded != null) {
                game.maxPlayers = loaded.maxPlayers;
                game.minPlayers = loaded.minPlayers;
                game.displayName = loaded.displayName;
                game.waitingLobby = loaded.waitingLobby;
                game.spectatorRespawnPoint = loaded.spectatorRespawnPoint;
                game.region = loaded.region;
            }
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load game.yml for " + game.name, e);
        }

        game.mapTemplate = new File(gameFolder, "map");
    }

    public void save(boolean isSaveWorldOnly) {
        File mapDir = new File(gameFolder, "map");
        WorldManager.exportWorld(gameInstance.getWorldUUID(), mapDir);

        if (isSaveWorldOnly) return;

        try {
            CommentedConfigurationNode root = loader.load();
            root.set(Game.class, game);
            loader.save(root);
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to save game.yml for " + game.name, e);
        }
    }

    public void release() {}
}