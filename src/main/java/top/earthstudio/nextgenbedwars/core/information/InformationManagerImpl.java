package top.earthstudio.nextgenbedwars.core.information;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.information.InformationManager;
import top.earthstudio.nextgenbedwars.api.information.InformationPosition;
import top.earthstudio.nextgenbedwars.core.information.config.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class InformationManagerImpl implements InformationManager {
    private Map<UUID, Scoreboard> scoreboards;
    private Map<String, Function<Player, Component>> informationHooks;
    private Map<InformationPosition, List<Component>> informationMap;

    private GameInstance gameInstance;

    private TabHeaderConfig tabHeaderConfig;
    private TabFooterConfig tabFooterConfig;
    private SideBarConfig sideBarConfig;
    private PlayerNameUnderConfig playerNameUnderConfig;
    private PlayerNamePrefixConfig playerNamePrefixConfig;
    private PlayerNameSuffixConfig playerNameSuffixConfig;

    private AnimationInformationConfig animationInformationConfig;

    public InformationManagerImpl(GameInstance gameInstance) {
        scoreboards = new Object2ObjectOpenHashMap<>();
        informationHooks = new Object2ObjectOpenHashMap<>();
        informationMap = new Object2ObjectOpenHashMap<>();

        this.gameInstance = gameInstance;

        tabHeaderConfig = new TabHeaderConfig(gameInstance);
        tabFooterConfig = new TabFooterConfig(gameInstance);
        sideBarConfig = new SideBarConfig(gameInstance);
        playerNameUnderConfig = new PlayerNameUnderConfig(gameInstance);
        playerNamePrefixConfig = new PlayerNamePrefixConfig(gameInstance);
        playerNameSuffixConfig = new PlayerNameSuffixConfig(gameInstance);

        animationInformationConfig = new AnimationInformationConfig(gameInstance);
        animationInformationConfig.load();
    }

    @Override
    public void create(String id, Function<Player, Component> informationHook) {
        if (informationHooks.putIfAbsent(id, informationHook) != null)
            throw new IllegalStateException("Information ID has been used!");
    }

    @Override
    public void remove(String id) {
        informationHooks.remove(id);
    }

    @Override
    public void displayFor(Player player) {
        if (scoreboards.containsKey(player.getUniqueId()))
            throw new IllegalStateException("Player has been shown Information!");

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        player.setScoreboard(scoreboard);
        scoreboards.put(player.getUniqueId(), scoreboard);
    }

    @Override
    public void clearFor(Player player) {
        Scoreboard old = scoreboards.remove(player.getUniqueId());
        if (old != null) {
            if (player.getScoreboard() == old) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
            player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
            destroyScoreboard(old);
        }
    }

    @Override
    public void update() {
        animationInformationConfig.animationComponents.forEach((string, pair) -> {
            pair.left((pair.leftInt() + 1) % pair.right().size());
        });

        switch (gameInstance.getGameState()) {
            case WAITING:
            case STARTING:
            case PLAYING:
            case ENDING:
                InformationRender.renderSidebar(scoreboards, sideBarConfig.load(), gameInstance.getGameState());
                InformationRender.renderTab();
                InformationRender.renderPlayerName();
            default:
                break;
        }
    }

    @Override
    public void forceUpdate() {
        update();
    }

    @Override
    public void shutdown() {
        tabHeaderConfig = null;
        tabFooterConfig = null;
        sideBarConfig = null;
        playerNameUnderConfig = null;
        playerNamePrefixConfig = null;
        playerNameSuffixConfig = null;

        animationInformationConfig = null;

        gameInstance = null;

        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (var entry : scoreboards.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            Scoreboard scoreboard = entry.getValue();

            if (player != null && player.getScoreboard() == scoreboard) {
                player.setScoreboard(mainScoreboard);
                player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
            }
            destroyScoreboard(scoreboard);
        }

        scoreboards.clear();
        scoreboards = null;
        informationHooks.clear();
        informationHooks = null;
        informationMap.clear();
        informationMap = null;
    }

    private void destroyScoreboard(Scoreboard scoreboard) {
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        for (var team : scoreboard.getTeams()) {
            team.removeEntries(team.getEntries());
            team.unregister();
        }

        for (var objective : scoreboard.getObjectives()) {
            objective.unregister();
        }
    }
}
