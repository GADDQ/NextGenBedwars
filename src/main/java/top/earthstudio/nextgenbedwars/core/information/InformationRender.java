package top.earthstudio.nextgenbedwars.core.information;

import io.papermc.paper.scoreboard.numbers.NumberFormat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import top.earthstudio.nextgenbedwars.api.game.GameState;
import top.earthstudio.nextgenbedwars.api.placeholder.PlaceholderManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InformationRender {
    public static void renderSidebar(
            Map<UUID, Scoreboard> scoreboards,
            Map<GameState, List<Component>> components,
            PlaceholderManager placeholderManager,
            GameState gameState
    ) {
        List<Component> lines = components.get(gameState);
        if (lines == null || lines.isEmpty()) return;

        Component titleTemplate = lines.getFirst();

        scoreboards.forEach((uuid, scoreboard) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            Component title = placeholderManager.resolve(titleTemplate, player);
            Objective sidebar = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (sidebar == null) {
                sidebar = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, title);
                sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                sidebar.displayName(title);
            }

            List<Component> expanded = new ObjectArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                expanded.addAll(splitLines(placeholderManager.resolve(lines.get(i), player)));
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
        });
    }

    public static void renderTab(
            Map<UUID, Scoreboard> scoreboards,
            Map<GameState, List<Component>> headerComponents,
            Map<GameState, List<Component>> footerComponents,
            PlaceholderManager placeholderManager,
            GameState gameState
    ) {
        List<Component> headerLines = headerComponents != null ? headerComponents.get(gameState) : null;
        List<Component> footerLines = footerComponents != null ? footerComponents.get(gameState) : null;

        for (UUID uuid : scoreboards.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            Component header = renderLines(headerLines, player, placeholderManager);
            Component footer = renderLines(footerLines, player, placeholderManager);
            player.sendPlayerListHeaderAndFooter(header, footer);
        }
    }

    public static void renderPlayerName(
            Map<UUID, Scoreboard> scoreboards,
            Map<GameState, List<Component>> prefixComponents,
            Map<GameState, List<Component>> suffixComponents,
            Map<GameState, List<Component>> underComponents,
            PlaceholderManager placeholderManager,
            GameState gameState
    ) {
        List<Component> prefixLines = prefixComponents != null ? prefixComponents.get(gameState) : null;
        List<Component> suffixLines = suffixComponents != null ? suffixComponents.get(gameState) : null;
        List<Component> underLines = underComponents != null ? underComponents.get(gameState) : null;

        Component prefixTemplate = (prefixLines != null && !prefixLines.isEmpty()) ? prefixLines.getFirst() : Component.empty();
        Component suffixTemplate = (suffixLines != null && !suffixLines.isEmpty()) ? suffixLines.getFirst() : Component.empty();
        Component underTemplate = (underLines != null && !underLines.isEmpty()) ? underLines.getFirst() : null;

        scoreboards.forEach((viewerUuid, scoreboard) -> {
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

                Component prefix = placeholderManager.resolve(prefixTemplate, target);
                Component suffix = placeholderManager.resolve(suffixTemplate, target);
                if (suffix.color() == null) {
                    suffix = suffix.color(NamedTextColor.WHITE);
                }

                TextColor trailingColor = findTrailingColor(prefix);
                NamedTextColor nameColor = trailingColor != null ? NamedTextColor.nearestTo(trailingColor) : NamedTextColor.WHITE;

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
                    Component underText = placeholderManager.resolve(underTemplate, target);
                    Score score = underObjective.getScoreFor(target);
                    score.numberFormat(NumberFormat.fixed(underText));
                    score.setScore(0);
                }
            }
        });
    }

    private static Component renderLines(
            List<Component> lines,
            Player player,
            PlaceholderManager placeholderManager
    ) {
        if (lines == null || lines.isEmpty()) return Component.empty();
        return Component.join(
                JoinConfiguration.newlines(),
                lines.stream().map(line -> placeholderManager.resolve(line, player)).toList()
        );
    }

    private static List<Component> splitLines(Component root) {
        List<Component> result = new ObjectArrayList<>();
        List<Component> currentParts = new ObjectArrayList<>();
        collectLineParts(root, Style.empty(), result, currentParts);
        result.add(joinParts(currentParts));
        return result;
    }

    private static void collectLineParts(
            Component node,
            Style inheritedStyle,
            List<Component> result,
            List<Component> currentParts
    ) {
        Style effectiveStyle = inheritedStyle.merge(node.style());
        if (node instanceof TextComponent text) {
            String[] segments = text.content().split("\n", -1);
            for (int i = 0; i < segments.length; i++) {
                if (i > 0) {
                    result.add(joinParts(currentParts));
                    currentParts.clear();
                }
                if (!segments[i].isEmpty()) {
                    currentParts.add(Component.text(segments[i], effectiveStyle));
                }
            }
        } else {
            currentParts.add(node.children(List.of()).style(effectiveStyle));
        }

        for (Component child : node.children()) {
            collectLineParts(child, effectiveStyle, result, currentParts);
        }
    }

    private static Component joinParts(List<Component> parts) {
        if (parts.isEmpty()) return Component.empty();
        if (parts.size() == 1) return parts.getFirst();
        return Component.empty().children(List.copyOf(parts));
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