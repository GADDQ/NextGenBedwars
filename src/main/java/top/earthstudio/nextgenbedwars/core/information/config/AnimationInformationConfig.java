package top.earthstudio.nextgenbedwars.core.information.config;

import it.unimi.dsi.fastutil.ints.IntObjectMutablePair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;

import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;

import java.util.List;
import java.util.Map;

public class AnimationInformationConfig extends IConfig<Map<String, List<Component>>> {
    public final Map<String, IntObjectMutablePair<List<Component>>> animationComponents;

    public AnimationInformationConfig(GameInstance gameInstance) {
        super(gameInstance, "information/information_animations.yml", true, "animations");
        animationComponents = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public Map<String, List<Component>> load() {
        animationComponents.clear();
        var loaded = super.load();
        if (loaded == null) return null;

        for (var entry : loaded.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                animationComponents.put(entry.getKey(), new IntObjectMutablePair<>(0, entry.getValue()));
            }
        }
        return null;
    }
}
