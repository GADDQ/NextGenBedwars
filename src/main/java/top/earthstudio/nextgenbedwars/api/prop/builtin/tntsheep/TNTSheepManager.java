package top.earthstudio.nextgenbedwars.api.prop.builtin.tntsheep;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.team.Team;

import java.util.UUID;

public interface TNTSheepManager extends IGameSubSystem { // ATTENTION: This was built by LLM.
    boolean spawn(Player owner, Location location);
    boolean isManagedSheep(Entity entity);
    Team getExplodingTeam();
    void removeBySheep(UUID sheepUuid);
}