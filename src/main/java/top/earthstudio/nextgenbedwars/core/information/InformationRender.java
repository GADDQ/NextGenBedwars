package top.earthstudio.nextgenbedwars.core.information;

import net.kyori.adventure.text.Component;

import org.bukkit.scoreboard.*;

import top.earthstudio.nextgenbedwars.api.game.GameState;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InformationRender {
    public static void renderSidebar(Map<UUID, Scoreboard> scoreboards, Map<GameState, List<Component>> components, GameState gameState) {
        List<Component> lines = components.get(gameState);
        if (lines == null || lines.isEmpty()) return;

        Component title = lines.getFirst();

        scoreboards.forEach((uuid, scoreboard) -> {
            Objective sidebar = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (sidebar == null) {
                sidebar = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, title);
                sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                sidebar.displayName(title);
            }

            int bodyCount = lines.size() - 1;
            int scoreValue = bodyCount;
            for (int i = 1; i < lines.size(); i++) {
                String key = "line_" + (i - 1);
                Score score = sidebar.getScore(key);
                score.customName(lines.get(i));
                score.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
                score.setScore(scoreValue--);
            }

            for (int i = bodyCount; i < 15; i++) {
                scoreboard.resetScores("line_" + i);
            }
        });
    }

    static public void renderTab() {
        // TODO
    }

    static public void renderPlayerName() {
        // TODO
    }
}
