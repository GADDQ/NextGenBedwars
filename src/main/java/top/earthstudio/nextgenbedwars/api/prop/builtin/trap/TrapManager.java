package top.earthstudio.nextgenbedwars.api.prop.builtin.trap;

import org.bukkit.block.Block;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.team.Team;

public interface TrapManager extends IGameSubSystem {
    @Override
    default void update() {}

    void add(Block block, Team team);
    boolean contains(Block block);
    Team getTeam(Block block);
    Team remove(Block block);
}
