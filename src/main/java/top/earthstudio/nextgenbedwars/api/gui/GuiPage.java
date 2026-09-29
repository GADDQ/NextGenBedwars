package top.earthstudio.nextgenbedwars.api.gui;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GuiPage {
    public final Component title;
    public final int rowCount;
    public boolean closeAfterInteract = false;
    public boolean allowPlayerInventoryInteraction = false;

    public final Map<Integer, Pair<ItemStack, GuiAction>> buttons = new Int2ObjectOpenHashMap<>();

    private Runnable refreshCallback;
    private Runnable closeCallback;

    public void bindRefresh(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    public void bindClose(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    public GuiPage(Component title, int rowCount) {
        this.title = title;
        this.rowCount = rowCount;
    }

    public GuiPage(Component title, int rowCount, boolean closeAfterInteract) {
        this(title, rowCount);
        this.closeAfterInteract = closeAfterInteract;
    }

    public void update() {
        refreshCallback.run();
    }

    public void close() {
        closeCallback.run();
    }

    protected void add(int slot, ItemStack button, GuiAction action) {
        buttons.put(slot, new ObjectObjectImmutablePair<>(button, action));
    }

    protected void add(int slot, ItemStack button, Runnable action) {
        add(slot, button, (player, clickType) -> action.run());
    }

    protected void add(int slot, ItemStack button) {
        buttons.put(slot, new ObjectObjectImmutablePair<>(button, null));
    }

    protected void add(int x, int y, ItemStack button, GuiAction action) {
        buttons.put(x + 9 * y, new ObjectObjectImmutablePair<>(button, action));
    }

    protected void add(int x, int y, ItemStack button, Runnable action) {
        add(x, y, button, (player, clickType) -> action.run());
    }

    protected void add(int x, int y, ItemStack button) {
        buttons.put(x + 9 * y, new ObjectObjectImmutablePair<>(button, null));
    }

    protected void remove(int slot) {
        buttons.remove(slot);
    }

    protected void remove(int x, int y) {
        buttons.remove(x + 9 * y);
    }
}
