package top.earthstudio.nextgenbedwars.api.gui.chest;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.earthstudio.nextgenbedwars.api.gui.GuiAction;
import top.earthstudio.nextgenbedwars.api.gui.IGuiHolder;

public class ChestGuiHolder implements IGuiHolder {
    public final ChestGuiPage chestGuiPage;
    private final Inventory inventory;

    public ChestGuiHolder(ChestGuiPage chestGuiPage) {
        this.chestGuiPage = chestGuiPage;
        this.inventory = Bukkit.createInventory(this, chestGuiPage.rowCount * 9, chestGuiPage.title);

        this.chestGuiPage.bindRefresh(this::refreshInventory);
        this.chestGuiPage.bindClose(this::closeInventory);

        chestGuiPage.buttons.forEach((slot, button) -> {
            inventory.setItem(slot, button.first());
        });
    }

    @Override
    public void refreshInventory() {
        int totalSlots = chestGuiPage.rowCount * 9;
        for (int slot = 0; slot < totalSlots; slot++) {
            Pair<ItemStack, GuiAction> button = chestGuiPage.buttons.get(slot);
            if (button != null && button.first() != null) {
                inventory.setItem(slot, button.first());
            } else {
                inventory.setItem(slot, null); // 清空没有按钮的残余格子
            }
        }
    }

    @Override
    public void closeInventory() {
        inventory.close();
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

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}