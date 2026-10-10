package top.earthstudio.nextgenbedwars.api.util;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GuiUtil {
    private GuiUtil() {}

    static public ItemStack button(Material material, Component name) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> {
            meta.displayName(name);
        });
        return itemStack;
    }

    static public ItemStack button(Material material, Component name, List<Component> description) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> {
            meta.displayName(name);
            meta.lore(description);
        });
        return itemStack;
    }

    public static ItemStack button(Material material, Component name, Component... description) {
        return button(material, name, List.of(description));
    }
}
