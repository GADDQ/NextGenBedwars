package top.earthstudio.nextgenbedwars.api.gui.anvil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnstableApiUsage")
public abstract class AnvilGuiPage extends IGuiPage {
    public String finalText = "";

    private final String defaultText;

    public AnvilGuiPage(Component title, String defaultText, Material material) {
        this(title, buildItem(defaultText, material));
    }

    public AnvilGuiPage(Component title, ItemStack leftItem) {
        this.title = title;
        this.defaultText = ((TextComponent) requireNonNull(leftItem.getItemMeta().displayName())).content();

        this.closeAfterInteract = true;
        this.allowPlayerInventoryInteraction = false;

        add(0, leftItem);

        this.guiHolder = new AnvilGuiHolder(this);
    }

    @Override
    public void onOpen(Player player) {
        AnvilView anvilView = (AnvilView) guiHolder.inventoryView;
        anvilView.setMaximumRepairCost(0);
        anvilView.setRepairCost(0);
    }

    public String getDefaultText() {
        return defaultText;
    }

    private static ItemStack buildItem(String defaultText, Material material) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(itemMeta -> itemMeta.displayName(Component.text(defaultText)));
        return itemStack;
    }
}