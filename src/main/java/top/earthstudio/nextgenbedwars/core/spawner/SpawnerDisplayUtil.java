package top.earthstudio.nextgenbedwars.core.spawner;

import net.kyori.adventure.text.Component;

import org.bukkit.util.Vector;

import top.earthstudio.nextgenbedwars.api.spawner.Spawner;

public final class SpawnerDisplayUtil {
    private SpawnerDisplayUtil() {}

    public static final Vector ZERO_VELOCITY = new Vector(0, 0, 0);

    public static Component updateTextDisplay(int seconds, Spawner spawner) {
        Component component = Component.text("");
        if (spawner.spawnerType.isShowHolo) {
            if (spawner.spawnerType.resourceName != null) {
                component = component.append(spawner.spawnerType.resourceName);
            }

            component = component.appendNewline();

            if (spawner.spawnerType.isShowTimer) {
                component = component.append(
                        Component.translatable("nextgenbedwars.spawner.display.rest_spawn_time", Component.text(seconds))
                );
            }

            component = component.appendNewline();

            if (spawner.spawnerType.isShowLevel && spawner.level > 0) {
                component = component.append(
                        Component.translatable("nextgenbedwars.spawner.display.level", Component.text(spawner.level))
                );
            }
        }

        return component;
    }
}
