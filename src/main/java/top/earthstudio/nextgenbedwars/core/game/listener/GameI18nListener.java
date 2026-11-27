package top.earthstudio.nextgenbedwars.core.game.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.language.ComponentDataType;

import java.util.List;

@SuppressWarnings("DataFlowIssue")
public class GameI18nListener implements Listener {
    private final GameInstance gameInstance;

    private static final Style DEFAULT_LORE_STYLE = Style.style()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .build();

    public GameI18nListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPick(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        var pdc = event.getItem().getItemStack().getPersistentDataContainer();

        if (player.getWorld() != gameInstance.getWorld())
            return;

        if (!pdc.has(PdcKeys.i18n.name))
            return;

        if (pdc.has(PdcKeys.i18n.name)) {
            Component renderedName = GlobalTranslator.render(
                    pdc.get(PdcKeys.i18n.name, ComponentDataType.COMPONENT),
                    player.locale()
            );
            item.editMeta(itemMeta -> {
                if (itemMeta instanceof org.bukkit.inventory.meta.PotionMeta) {
                    itemMeta.displayName(renderedName.applyFallbackStyle(DEFAULT_LORE_STYLE));
                } else {
                    itemMeta.itemName(renderedName);
                }
            });
        }

        if (pdc.has(PdcKeys.i18n.lore)) {
            List<Component> rawLore = pdc.get(PdcKeys.i18n.lore, ComponentDataType.COMPONENT_LIST);
            item.editMeta(itemMeta ->
                    itemMeta.lore(rawLore.stream()
                            .map(line -> GlobalTranslator.render(line, player.locale()).applyFallbackStyle(DEFAULT_LORE_STYLE))
                            .toList())
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLocaleChanged(PlayerLocaleChangeEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld() != gameInstance.getWorld())
            return;

        Inventory inventory = event.getPlayer().getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null)
                continue;

            var pdc = item.getPersistentDataContainer();

            if (pdc.has(PdcKeys.i18n.name)) {
                item.editMeta(itemMeta ->
                        itemMeta.itemName(GlobalTranslator.render(
                                pdc.get(PdcKeys.i18n.name, ComponentDataType.COMPONENT),
                                event.locale()
                        ))
                );
            }

            if (pdc.has(PdcKeys.i18n.lore)) {
                List<Component> rawLore = pdc.get(PdcKeys.i18n.lore, ComponentDataType.COMPONENT_LIST);
                item.editMeta(itemMeta ->
                        itemMeta.lore(rawLore.stream()
                                .map(line -> GlobalTranslator.render(line, event.locale()).applyFallbackStyle(DEFAULT_LORE_STYLE))
                                .toList())
                );
            }
        }
    }
}
