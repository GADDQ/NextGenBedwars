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

    public final Map<Integer, Pair<ItemStack, GuiAction>> buttons = new Int2ObjectOpenHashMap<>();

    public GuiPage(Component title, int rowCount) {
        this.title = title;
        this.rowCount = rowCount;
    }

    public GuiPage(Component title, int rowCount, boolean closeAfterInteract) {
        this(title, rowCount);
        this.closeAfterInteract = closeAfterInteract;
    }

    protected void add(int slot, ItemStack button, GuiAction action) {
        buttons.put(slot, new ObjectObjectImmutablePair<>(button, action));
    }

    protected void add(int slot, ItemStack button) {
        buttons.put(slot, new ObjectObjectImmutablePair<>(button, null));
    }
}
