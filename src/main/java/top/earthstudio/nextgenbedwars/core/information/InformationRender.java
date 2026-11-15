package top.earthstudio.nextgenbedwars.core.information;

import io.papermc.paper.scoreboard.numbers.NumberFormat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import top.earthstudio.nextgenbedwars.api.game.GameState;
import top.earthstudio.nextgenbedwars.api.util.TextUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InformationRender {
    private static final Pattern HOOK_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)}");

    public static Component resolveSingle(
            Component template,
            Player player,
            Map<String, Function<Player, List<Component>>> hooks
    ) {
        if (template == null) return Component.empty();
        return template.replaceText(builder -> builder
                .match(HOOK_PATTERN)
                .replacement((matchResult, textBuilder) -> {
                    String key = matchResult.group(1);
                    var hook = hooks.get(key);
                    if (hook == null) return Component.text(matchResult.group(0));

                    List<Component> resolved = hook.apply(player);
                    if (resolved == null || resolved.isEmpty()) return Component.empty();
                    return resolved.size() == 1 ? resolved.getFirst() : Component.join(JoinConfiguration.newlines(), resolved);
                })
        );
    }

    public static List<Component> resolveLines(
            Component template,
            Player player,
            Map<String, Function<Player, List<Component>>> hooks
    ) {
        if (template == null) return List.of(Component.empty());

        String serialized = TextUtil.toMiniMessage(template).trim();
        Matcher matcher = HOOK_PATTERN.matcher(serialized);

        if (matcher.matches()) {
            String key = matcher.group(1);
            var hook = hooks.get(key);
            if (hook != null) {
                List<Component> resolved = hook.apply(player);
                return (resolved != null && !resolved.isEmpty()) ? resolved : List.of();
            }
        }

        return List.of(resolveSingle(template, player, hooks));
    }

    public static void renderSidebar(
            Map<UUID, Scoreboard> scoreboards,
            Map<GameState, List<Component>> components,
            Map<String, Function<Player, List<Component>>> hooks,
            GameState gameState
    ) {
        List<Component> lines = components.get(gameState);
        if (lines == null || lines.isEmpty()) return;

        Component titleTemplate = lines.getFirst();

        for (Map.Entry<UUID, Scoreboard> entry : scoreboards.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;

            Scoreboard scoreboard = entry.getValue();
            Component title = resolveSingle(titleTemplate, player, hooks);

            Objective sidebar = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (sidebar == null) {
                sidebar = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, title);
                sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                sidebar.displayName(title);
            }

            List<Component> expanded = new ObjectArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                expanded.addAll(resolveLines(lines.get(i), player, hooks));
            }

            int displayCount = Math.min(expanded.size(), 15);
            int scoreValue = displayCount;
            for (int i = 0; i < displayCount; i++) {
                String key = "line_" + i;
                Score score = sidebar.getScore(key);
                score.customName(expanded.get(i));
                score.numberFormat(NumberFormat.blank());
                score.setScore(scoreValue--);
            }

            for (int i = displayCount; i < 15; i++) {
                scoreboard.resetScores("line_" + i);
            }
        }
    }

    public static void renderTab(
            Map<UUID, Scoreboard> scoreboards,
            Map<GameState, List<Component>> headerComponents,
            Map<GameState, List<Component>> footerComponents,
            Map<String, Function<Player, List<Component>>> hooks,
            GameState gameState
    ) {
        List<Component> headerLines = headerComponents != null ? headerComponents.get(gameState) : null;
        List<Component> footerLines = footerComponents != null ? footerComponents.get(gameState) : null;

        for (UUID uuid : scoreboards.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            Component header = renderLines(headerLines, player, hooks);
            Component footer = renderLines(footerLines, player, hooks);
            player.sendPlayerListHeaderAndFooter(header, footer);
        }
    }

    public static void renderPlayerName(
            Map<UUID, Scoreboard> scoreboards,
            Map<GameState, List<Component>> prefixComponents,
            Map<GameState, List<Component>> suffixComponents,
            Map<GameState, List<Component>> underComponents,
            Map<String, Function<Player, List<Component>>> hooks,
            GameState gameState
    ) {
        List<Component> prefixLines = prefixComponents != null ? prefixComponents.get(gameState) : null;
        List<Component> suffixLines = suffixComponents != null ? suffixComponents.get(gameState) : null;
        List<Component> underLines = underComponents != null ? underComponents.get(gameState) : null;

        Component prefixTemplate = (prefixLines != null && !prefixLines.isEmpty()) ? prefixLines.getFirst() : Component.empty();
        Component suffixTemplate = (suffixLines != null && !suffixLines.isEmpty()) ? suffixLines.getFirst() : Component.empty();
        Component underTemplate = (underLines != null && !underLines.isEmpty()) ? underLines.getFirst() : null;

        for (Map.Entry<UUID, Scoreboard> entry : scoreboards.entrySet()) {
            Player viewer = Bukkit.getPlayer(entry.getKey());
            if (viewer == null) continue;

            Scoreboard scoreboard = entry.getValue();

            Objective underObjective = null;
            if (underTemplate != null) {
                underObjective = scoreboard.getObjective(DisplaySlot.BELOW_NAME);
                if (underObjective == null) {
                    underObjective = scoreboard.registerNewObjective("head", Criteria.DUMMY, Component.empty());
                    underObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
                }
            }

            for (UUID targetUuid : scoreboards.keySet()) {
                Player target = Bukkit.getPlayer(targetUuid);
                if (target == null) continue;

                Component prefix = resolveSingle(prefixTemplate, target, hooks);
                Component suffix = resolveSingle(suffixTemplate, target, hooks);

                NamedTextColor nameColor = extractNameColor(prefix);

                String teamId = target.getName();
                Team sbTeam = scoreboard.getTeam(teamId);
                if (sbTeam == null) {
                    sbTeam = scoreboard.registerNewTeam(teamId);
                }
                sbTeam.prefix(prefix);
                sbTeam.suffix(suffix);
                sbTeam.color(nameColor);

                if (!sbTeam.hasEntity(target)) {
                    sbTeam.addEntity(target);
                }

                if (underObjective != null) {
                    Component underText = resolveSingle(underTemplate, target, hooks);
                    Score score = underObjective.getScoreFor(target);
                    score.numberFormat(NumberFormat.fixed(underText));
                    score.setScore(0);
                }
            }
        }
    }

    private static Component renderLines(
            List<Component> lines,
            Player player,
            Map<String, Function<Player, List<Component>>> hooks
    ) {
        if (lines == null || lines.isEmpty()) return Component.empty();
        List<Component> expanded = new ObjectArrayList<>();
        for (Component line : lines) {
            expanded.addAll(resolveLines(line, player, hooks));
        }
        return Component.join(JoinConfiguration.newlines(), expanded);
    }

    private static NamedTextColor extractNameColor(Component prefix) {
        if (prefix == null) return NamedTextColor.WHITE;
        TextColor lastColor = findTrailingColor(prefix);
        return lastColor != null ? NamedTextColor.nearestTo(lastColor) : NamedTextColor.WHITE;
    }

    private static TextColor findTrailingColor(Component component) {
        TextColor current = component.color();
        List<Component> children = component.children();
        if (children.isEmpty()) return current;

        for (int i = children.size() - 1; i >= 0; i--) {
            TextColor childColor = findTrailingColor(children.get(i));
            if (childColor != null) return childColor;
        }
        return current;
    }
}