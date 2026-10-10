package top.earthstudio.nextgenbedwars.api.gui.chest;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import top.earthstudio.nextgenbedwars.api.gui.GuiAction;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

public abstract class ChestGuiPage extends IGuiPage {
    public final Component title;
    public final int rowCount;

    @Override
    public void openFor(Player player) {
        player.openInventory(new ChestGuiHolder(this).getInventory());
    }

    protected ChestGuiPage(Component title, int rowCount) {
        this.title = title;
        this.rowCount = rowCount;
    }

    @SuppressWarnings("unused")
    protected ChestGuiPage(Component title, int rowCount, boolean closeAfterInteract) {
        this(title, rowCount);
        this.closeAfterInteract = closeAfterInteract;
    }

    // ==================== Internal Helper Method ====================

    protected void add(int x, int y, ItemStack button, GuiAction action) {
        buttons.put(x + 9 * y, new ObjectObjectImmutablePair<>(button, action));
    }

    protected void add(int x, int y, ItemStack button, Runnable action) {
        add(x, y, button, (player, clickType) -> action.run());
    }

    protected void add(int x, int y, ItemStack button) {
        buttons.put(x + 9 * y, new ObjectObjectImmutablePair<>(button, null));
    }

    protected void remove(int x, int y) {
        buttons.remove(x + 9 * y);
    }
}
