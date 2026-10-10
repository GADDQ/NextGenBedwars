package top.earthstudio.nextgenbedwars.api.gui.chest;

import net.kyori.adventure.text.Component;

import top.earthstudio.nextgenbedwars.api.gui.IGuiHolder;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

public abstract class ChestGuiPage extends IGuiPage {
    public final Component title;
    public final int rowCount;
    public boolean closeAfterInteract = false;
    public boolean allowPlayerInventoryInteraction = false;

    @Override
    public IGuiHolder createHolder() {
        return new ChestGuiHolder(this);
    }

    protected ChestGuiPage(Component title, int rowCount) {
        this.title = title;
        this.rowCount = rowCount;
    }

    protected ChestGuiPage(Component title, int rowCount, boolean closeAfterInteract) {
        this(title, rowCount);
        this.closeAfterInteract = closeAfterInteract;
    }
}
