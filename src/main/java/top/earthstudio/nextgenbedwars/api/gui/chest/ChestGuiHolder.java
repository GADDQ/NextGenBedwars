package top.earthstudio.nextgenbedwars.api.gui.chest;

import it.unimi.dsi.fastutil.Pair;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.builder.InventoryViewBuilder;

import top.earthstudio.nextgenbedwars.api.gui.GuiAction;
import top.earthstudio.nextgenbedwars.api.gui.IGuiHolder;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnstableApiUsage")
public class ChestGuiHolder extends IGuiHolder {
    private final ChestGuiPage chestGuiPage;

    public ChestGuiHolder(ChestGuiPage chestGuiPage) {
        super(chestGuiPage, requireNonNull(getMenuBuilder(chestGuiPage.rowCount)));

        this.chestGuiPage = chestGuiPage;
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        Pair<ItemStack, GuiAction> button = chestGuiPage.buttons.get(slot);
        if (button != null && button.second() != null) {
            button.second().execute(player, clickType);
            if (chestGuiPage.closeAfterInteract)
                player.closeInventory();
        }
    }

    static private InventoryViewBuilder<InventoryView> getMenuBuilder(int rowCount) {
        return switch (rowCount) {
            case 1 -> MenuType.GENERIC_9X1.builder();
            case 2 -> MenuType.GENERIC_9X2.builder();
            case 3 -> MenuType.GENERIC_9X3.builder();
            case 4 -> MenuType.GENERIC_9X4.builder();
            case 5 -> MenuType.GENERIC_9X5.builder();
            case 6 -> MenuType.GENERIC_9X6.builder();
            default -> null;
        };
    }
}