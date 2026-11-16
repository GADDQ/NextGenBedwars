package top.earthstudio.nextgenbedwars.core.placeholder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.placeholder.PlaceholderManager;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class PlaceholderManagerImpl implements PlaceholderManager { // TODO: support read PlaceholderAPI
    private Map<String, Function<Player, Component>> placeholderHooks;

    private static final Pattern HOOK_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)}");

    public PlaceholderManagerImpl(GameInstance gameInstance) {
        placeholderHooks = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void create(String id, Function<Player, Component> placeholderHook) {
        if (placeholderHooks.putIfAbsent(id, placeholderHook) != null)
            throw new IllegalStateException("Placeholder ID has been used!");
    }

    @Override
    public void remove(String id) {
        placeholderHooks.remove(id);
    }

    @Override
    public Component get(String id) {
        return get(id, null);
    }

    @Override
    public Component get(String id, Player player) {
        var hook = placeholderHooks.get(id);
        return hook == null ? null : hook.apply(player);
    }

    @Override
    public Component resolve(Component template) {
        return resolve(template, null);
    }

    @Override
    public Component resolve(Component template, Player player) {
        if (template == null) return Component.empty();
        return template.replaceText(builder -> builder
                .match(HOOK_PATTERN)
                .replacement((matchResult, textBuilder) -> {
                    String id = matchResult.group(1);
                    var component = get(id, player);
                    return component != null ? component : Component.text(matchResult.group(0));
                })
        );
    }

    @Override
    public void shutdown() {
        placeholderHooks.clear();
        placeholderHooks = null;
    }
}
