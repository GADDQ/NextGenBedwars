package top.earthstudio.nextgenbedwars.core.gui;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.gui.GuiOpener;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

public class GuiOpenerImpl implements GuiOpener {
    @Override
    public void openFor(Player player, IGuiPage guiPage) {
        guiPage.openFor(player);
    }
}
