package top.earthstudio.nextgenbedwars.core.information.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;

import org.jetbrains.annotations.NotNull;

import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.game.GameState;

import java.util.List;
import java.util.Map;

public class PlayerNameSuffixConfig extends IConfig<Map<GameState, List<Component>>> {
    private final Map<GameState, List<Component>> components;

    public PlayerNameSuffixConfig(@NotNull GameInstance gameInstance) {
        super(gameInstance, "information/player_name_suffix.yml", true, "information");
        components = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public Map<GameState, List<Component>> load() {
        if (components.isEmpty())
            components.putAll(super.load());

        return components;
    }
}
