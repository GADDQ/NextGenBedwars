package top.earthstudio.nextgenbedwars.core.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import top.earthstudio.nextgenbedwars.api.gui.GuiManager;
import top.earthstudio.nextgenbedwars.api.gui.IGuiHolder;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class GuiManagerImpl implements GuiManager {
    static private Map<InventoryView, IGuiHolder> guiPages;
    static GuiManager INSTANCE;

    private GuiManagerImpl() {}

    static public void initialize() {
        guiPages = new Reference2ObjectOpenHashMap<>();
        INSTANCE = new GuiManagerImpl();
    }

    @Override
    public void openFor(Player player, IGuiPage guiPage) {
        _openFor(player, guiPage);
    }

    static public void _openFor(Player player, IGuiPage guiPage) {
        InventoryView inventoryView = guiPage.guiHolder.getBuilder().build(player);
        guiPage.buttons.forEach((slot, pair) ->
                inventoryView.getTopInventory().setItem(slot, pair.first())
        );
        guiPage.guiHolder.inventoryView = inventoryView;

        guiPages.put(inventoryView, guiPage.guiHolder);
        player.openInventory(inventoryView);
    }

    static public IGuiHolder getGuiHolder(InventoryView inventoryView) {
        return guiPages.get(inventoryView);
    }

    static public void close(InventoryView inventoryView) {
        guiPages.remove(inventoryView);
    }

    static public void shutdown() {
        new ObjectArrayList<>(guiPages.keySet()).forEach(InventoryView::close);

        guiPages.clear();
        guiPages = null;
        INSTANCE = null;
    }

    static public GuiManager get() {
        return INSTANCE;
    }
}
