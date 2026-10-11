package top.earthstudio.nextgenbedwars.api.gui;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.builder.InventoryViewBuilder;

@SuppressWarnings("UnstableApiUsage")
public abstract class IGuiHolder {
    public final IGuiPage guiPage;
    public InventoryView inventoryView;

    protected final InventoryViewBuilder<InventoryView> builder;

    protected IGuiHolder(IGuiPage guiPage, InventoryViewBuilder<InventoryView> builder) {
        this.guiPage = guiPage;
        this.builder = builder.title(guiPage.title);
    }

    abstract public void handleClick(int slot, Player player, ClickType clickType);

    public void refreshInventory() {
        int totalSlots = inventoryView.getTopInventory().getSize();
        for (int slot = 0; slot < totalSlots; slot++) {
            Pair<ItemStack, GuiAction> button = guiPage.buttons.get(slot);
            if (button != null && button.first() != null) {
                inventoryView.getTopInventory().setItem(slot, button.first());
            } else {
                inventoryView.getTopInventory().setItem(slot, null); // 清空没有按钮的残余格子
            }
        }
    }

    public void handleClose(Player player) {
        guiPage.onClose(player);
    }

    public InventoryViewBuilder<InventoryView> getBuilder() {
        return builder;
    }
}
