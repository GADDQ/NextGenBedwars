package top.earthstudio.nextgenbedwars.core.game;

import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.Game;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import java.io.File;

public class GameConfig extends IConfig<Game> {
    public GameConfig(GameInstance gameInstance) {
        super(gameInstance, "game.yml", false);
    }

    @Override
    public Game load() {
        Game game = super.load();

        game.mapTemplate = new File(GameFolder.of(game), "map");

        return game;
    }

    @Override
    public void save(Game game) {
        super.save(game);
    }
}
