package top.earthstudio.nextgenbedwars.core.shop.item;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class ItemShopUtil {
    private static final Style DEFAULT_LORE_STYLE = Style.style()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .build();

    private ItemShopUtil() {}

    public static ItemStack translateItem(ItemStack item, Locale locale) {
        ItemStack copy = item.clone();
        copy.editMeta(meta -> {
            if (meta.hasItemName()) {
                meta.itemName();
                meta.itemName(
                        GlobalTranslator.render(meta.itemName(), locale)
                );
            }
            if (meta.hasLore() && meta.lore() != null) {
                meta.lore(meta.lore().stream()
                        .map(line -> GlobalTranslator.render(line, locale)
                                .applyFallbackStyle(DEFAULT_LORE_STYLE))
                        .toList());
            }
        });
        return copy;
    }
}
