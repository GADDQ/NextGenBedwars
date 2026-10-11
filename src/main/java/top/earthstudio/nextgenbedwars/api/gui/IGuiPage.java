package top.earthstudio.nextgenbedwars.api.gui;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public abstract class IGuiPage {
    public Component title;

    public boolean closeAfterInteract = false;
    public boolean allowPlayerInventoryInteraction = false;

    public IGuiHolder guiHolder;

    public final Map<Integer, Pair<ItemStack, GuiAction>> buttons = new Int2ObjectOpenHashMap<>();

    /**
     * This is NOT API, DO NOT call it!
     * */
    @SuppressWarnings("unused")
    public void onClose(Player player) {}

    protected void update() {
        guiHolder.refreshInventory();
    }

    @SuppressWarnings("unused")
    protected void close() {
        guiHolder.inventoryView.close();
    }

    // ==================== Internal Helper Method ====================

    protected void add(int slot, ItemStack button, GuiAction action) {
        buttons.put(slot, new ObjectObjectImmutablePair<>(button, action));
    }

    protected void add(int slot, ItemStack button, Runnable action) {
        add(slot, button, (player, clickType) -> action.run());
    }

    protected void add(int slot, ItemStack button) {
        buttons.put(slot, new ObjectObjectImmutablePair<>(button, null));
    }

    protected void remove(int slot) {
        buttons.remove(slot);
    }

    @SuppressWarnings("unused")
    protected void removeAll() {
        buttons.clear();
    }
}
