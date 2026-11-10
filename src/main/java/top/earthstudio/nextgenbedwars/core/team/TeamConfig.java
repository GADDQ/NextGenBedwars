package top.earthstudio.nextgenbedwars.core.team;

import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;

import java.util.List;

public class TeamConfig extends IConfig<List<Team>> {
    public TeamConfig(GameInstance gameInstance) {
        super(gameInstance, "teams.yml", false, "teams");
    }

    @Override
    public List<Team> load() {
        return super.load();
    }

    @Override
    protected void save(List<Team> teams) {
        super.save(teams);
    }
}
