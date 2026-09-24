package top.earthstudio.nextgenbedwars.core.spawner;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;

public final class SpawnerDisplayUtil {
    private SpawnerDisplayUtil() {}

    public static final Vector ZERO_VELOCITY = new Vector(0, 0, 0);

    public static Component updateTextDisplay(int seconds, Spawner spawner) { // TODO: i18n
        Component component = Component.text("");
        if (spawner.isShowHolo) {
            if (spawner.resourceName != null) {
                component = component.append(spawner.resourceName);
            }

            component = component.appendNewline();

            if (spawner.isShowTimer) {
                component = component.append(
                        Component.text(seconds).color(NamedTextColor.WHITE)
                                .append(
                                        Component.text(" 秒后刷新").color(NamedTextColor.GREEN)
                                )
                );
            }

            component = component.appendNewline();

            if (spawner.isShowLevel && spawner.level > 0) {
                component = component.append(
                        Component.text("等级 ").color(NamedTextColor.GOLD)
                                .append(
                                        Component.text(spawner.level).color(NamedTextColor.WHITE)
                                )
                );
            }
        }

        return component;
    }
    public static boolean canSpawn(Spawner spawner) {
        if (spawner.maxSpawnCount <= 0) {
            return true;
        }

        int counter = 0;
        for (Entity entity : spawner.location.getNearbyEntities(0.75, 0.75, 0.75)) {
            if (entity instanceof Item item && item.getItemStack().getType() == spawner.material && item.getPersistentDataContainer().has(PdcKeys.spawner.drop)) {
                int itemAmount = item.getItemStack().getAmount();
                counter += (itemAmount == 0 ? 1 : itemAmount);
            }
        }

        return counter < spawner.maxSpawnCount;
    }
}
