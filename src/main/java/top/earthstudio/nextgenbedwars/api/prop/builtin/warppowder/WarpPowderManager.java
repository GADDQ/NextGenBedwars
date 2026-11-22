package top.earthstudio.nextgenbedwars.api.prop.builtin.warppowder;

import org.bukkit.entity.Player;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.WarpPowderProp;

public interface WarpPowderManager extends IGameSubSystem {
    void use(Player player, WarpPowderProp warpPowder);
    boolean contains(Player player);
    void remove(Player player);
}
