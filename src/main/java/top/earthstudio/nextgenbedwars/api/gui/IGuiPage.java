package top.earthstudio.nextgenbedwars.api.gui;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public abstract class IGuiPage {
    public boolean closeAfterInteract = false;
    public boolean allowPlayerInventoryInteraction = false;

    protected Runnable refreshCallback;
    protected Runnable closeCallback;

    public final Map<Integer, Pair<ItemStack, GuiAction>> buttons = new Int2ObjectOpenHashMap<>();

    /**
     * This is an advanced API, DO NOT call it unless you know what are you doing!
     * */
    public abstract void openFor(Player player);

    /**
     * This is NOT API, DO NOT call it!
     * */
    public void bindRefresh(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    /**
     * This is NOT API, DO NOT call it!
     * */
    public void bindClose(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    protected void update() {
        refreshCallback.run();
    }

    @SuppressWarnings("unused")
    protected void close() {
        closeCallback.run();
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
