package top.earthstudio.nextgenbedwars.api.gui.chest;

import org.bukkit.entity.Player;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

public interface ChestGuiOpener extends IGameSubSystem {
    @Override
    default void update() {}

    @Override
    default void shutdown() {}

    void openFor(Player player, ChestGuiPage chestGuiPage);
}
