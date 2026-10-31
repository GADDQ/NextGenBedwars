package top.earthstudio.nextgenbedwars.core.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.gui.GuiManager;
import top.earthstudio.nextgenbedwars.api.gui.IGuiHolder;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

import top.earthstudio.nextgenbedwars.core.gui.listener.GuiInteractListener;
import top.earthstudio.nextgenbedwars.core.gui.listener.SpecialGuiFixListener;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class GuiManagerImpl implements GuiManager {
    static private Map<InventoryView, IGuiHolder> guiPages;
    static private GuiManager INSTANCE;

    static private GuiInteractListener guiInteractListener;
    static private SpecialGuiFixListener specialGuiFixListener;

    private GuiManagerImpl() {}

    static public void initialize(JavaPlugin plugin) {
        guiPages = new Reference2ObjectOpenHashMap<>();
        INSTANCE = new GuiManagerImpl();

        guiInteractListener = new GuiInteractListener();
        specialGuiFixListener = new SpecialGuiFixListener();

        Bukkit.getPluginManager().registerEvents(guiInteractListener, plugin);
        Bukkit.getPluginManager().registerEvents(specialGuiFixListener, plugin);
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

        guiPage.onOpen(player);

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

        HandlerList.unregisterAll(guiInteractListener);
        HandlerList.unregisterAll(specialGuiFixListener);
        guiInteractListener = null;
        specialGuiFixListener = null;

        guiPages.clear();
        guiPages = null;
        INSTANCE = null;
    }

    static public GuiManager get() {
        return INSTANCE;
    }
}
