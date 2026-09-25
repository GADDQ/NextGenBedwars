package top.earthstudio.nextgenbedwars.api.team;

import org.bukkit.Color;
import org.bukkit.entity.Player;
import top.earthstudio.nextgenbedwars.api.game.IManager;

import java.util.List;

public interface TeamManager extends IManager<Team> {
    Team get(Color color);
    Team get(Player player);
    List<Team> getAll();
}
