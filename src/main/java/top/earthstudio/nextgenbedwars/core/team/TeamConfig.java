package top.earthstudio.nextgenbedwars.core.team;

import net.kyori.adventure.text.Component;
import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.util.ColorUtil;

import java.util.List;

public class TeamConfig extends IConfig<List<Team>> {
    public TeamConfig(GameInstance gameInstance) {
        super(gameInstance, "teams.yml", false, "teams");
    }

    @Override
    public List<Team> load() {
        var config = super.load();
        config.forEach(team -> {
            String colorName = ColorUtil.toNamedTextColorName(team.teamColor);
            if (colorName == null || colorName.isEmpty())
                colorName = ColorUtil.colorToHex(team.teamColor);

            team.displayName = Component.translatable("nextgenbedwars.team.team." + colorName + ".display_name").color(ColorUtil.toTextColor(team.teamColor));
        });
        return config;
    }

    @Override
    protected void save(List<Team> teams) {
        super.save(teams);
    }
}
