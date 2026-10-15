package top.earthstudio.nextgenbedwars.core.team;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import top.earthstudio.nextgenbedwars.api.config.ConfigurateHelper;
import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import java.io.File;
import java.util.List;

public class TeamConfig implements IConfig<Team> {
    private final File file;
    private final YamlConfigurationLoader loader;
    private final List<Team> teamList;

    public TeamConfig(GameInstance gameInstance) {
        File gameFolder = GameFolder.of(gameInstance.getGame());
        this.file = new File(gameFolder, "team.yml");
        this.loader = ConfigurateHelper.loader(file);
        this.teamList = new ObjectArrayList<>();

        loadFromConfig();
    }

    private void loadFromConfig() {
        if (!file.exists()) return;

        try {
            CommentedConfigurationNode root = loader.load();
            List<Team> loaded = root.node("teams").getList(Team.class);
            if (loaded != null) {
                teamList.addAll(loaded);
            }
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load team.yml", e);
        }
    }

    @Override
    public List<Team> getValues() {
        return teamList;
    }

    @Override
    public void release() {
        teamList.clear();
    }

    @Override
    public void save(List<Team> values) {
        try {
            CommentedConfigurationNode root = loader.load();
            root.node("teams").setList(Team.class, values);
            loader.save(root);

            teamList.clear();
            teamList.addAll(values);
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to save team.yml", e);
        }
    }
}