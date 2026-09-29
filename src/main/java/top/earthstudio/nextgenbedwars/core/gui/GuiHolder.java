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
    public final GuiPage guiPage;
    private final Inventory inventory;

    public GuiHolder(GuiPage guiPage) {
        this.guiPage = guiPage;
        this.inventory = Bukkit.createInventory(this, guiPage.rowCount * 9, guiPage.title);

        this.guiPage.bindRefresh(this::refreshInventory);
        this.guiPage.bindClose(this::closeInventory);

        guiPage.buttons.forEach((slot, button) -> {
            inventory.setItem(slot, button.first());
        });
    }

    public void refreshInventory() {
        int totalSlots = guiPage.rowCount * 9;
        for (int slot = 0; slot < totalSlots; slot++) {
            Pair<ItemStack, GuiAction> button = guiPage.buttons.get(slot);
            if (button != null && button.first() != null) {
                inventory.setItem(slot, button.first());
            } else {
                inventory.setItem(slot, null); // 清空没有按钮的残余格子
            }
        }
    }

    public void closeInventory() {
        inventory.close();
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