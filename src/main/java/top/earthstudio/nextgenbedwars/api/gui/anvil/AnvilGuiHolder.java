package top.earthstudio.nextgenbedwars.api.gui.anvil;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.MenuType;

import org.bukkit.inventory.view.AnvilView;
import top.earthstudio.nextgenbedwars.api.gui.IGuiHolder;

@SuppressWarnings("UnstableApiUsage")
public class AnvilGuiHolder extends IGuiHolder {
    private final AnvilGuiPage anvilGuiPage;

    public AnvilGuiHolder(AnvilGuiPage guiPage) {
        super(guiPage, MenuType.ANVIL.builder());

        this.anvilGuiPage = guiPage;
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot == 2) {
            AnvilView anvilView = (AnvilView) inventoryView;
            String text = anvilView.getRenameText();

            if (text == null || text.isBlank())
                return;
            else
                anvilGuiPage.finalText = text;

            player.closeInventory();
        }
    }
}
