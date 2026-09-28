package top.earthstudio.nextgenbedwars.core.gui;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.earthstudio.nextgenbedwars.api.gui.GuiAction;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;

public class GuiHolder implements InventoryHolder {
    private final GuiPage guiPage;
    private final Inventory inventory;

    public GuiHolder(GuiPage guiPage) {
        this.guiPage = guiPage;
        // 1. 创建真正属于当前视窗的全新原生 Inventory（正确传 size！）
        this.inventory = Bukkit.createInventory(this, guiPage.rowCount * 9, guiPage.title);

        guiPage.buttons.forEach((slot, button) -> {
            inventory.setItem(slot, button.first());
        });
    }

    public void handleClick(int slot, Player player, ClickType clickType) {
        Pair<ItemStack, GuiAction> button = guiPage.buttons.get(slot);
        if (button != null && button.second() != null) {
            button.second().execute(player, clickType);
            if (guiPage.closeAfterInteract)
                player.closeInventory();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}