package top.earthstudio.nextgenbedwars.api.gui;

import org.bukkit.entity.Player;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

public interface GuiOpener extends IGameSubSystem {
    @Override
    default void update() {}

    @Override
    default void shutdown() {}

    void openFor(Player player, GuiPage guiPage);
}
