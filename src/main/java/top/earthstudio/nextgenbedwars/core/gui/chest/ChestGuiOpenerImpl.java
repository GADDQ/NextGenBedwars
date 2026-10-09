package top.earthstudio.nextgenbedwars.core.gui.chest;

import org.bukkit.entity.Player;
import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiOpener;
import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiPage;

public class ChestGuiOpenerImpl implements ChestGuiOpener {
    @Override
    public void openFor(Player player, ChestGuiPage chestGuiPage) {
        ChestGuiManager.openFor(player, chestGuiPage);
    }
}
