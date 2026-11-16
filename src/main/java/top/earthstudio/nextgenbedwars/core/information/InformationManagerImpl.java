package top.earthstudio.nextgenbedwars.core.information;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Scoreboard;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.information.InformationManager;
import top.earthstudio.nextgenbedwars.api.placeholder.PlaceholderManager;
import top.earthstudio.nextgenbedwars.core.information.config.*;
import top.earthstudio.nextgenbedwars.core.information.listener.InformationPlayerChangedListener;

import java.util.Map;
import java.util.UUID;

public class InformationManagerImpl implements InformationManager {
    private Map<UUID, Scoreboard> scoreboards;

    private GameInstance gameInstance;
    private PlaceholderManager placeholderManager;

    private InformationPlayerChangedListener informationPlayerChangedListener;

    private TabHeaderConfig tabHeaderConfig;
    private TabFooterConfig tabFooterConfig;
    private SideBarConfig sideBarConfig;
    private PlayerNameUnderConfig playerNameUnderConfig;
    private PlayerNamePrefixConfig playerNamePrefixConfig;
    private PlayerNameSuffixConfig playerNameSuffixConfig;

    private AnimationInformationConfig animationInformationConfig;

    public InformationManagerImpl(GameInstance gameInstance) {
        scoreboards = new Object2ObjectOpenHashMap<>();

        this.gameInstance = gameInstance;

        tabHeaderConfig = new TabHeaderConfig(gameInstance);
        tabFooterConfig = new TabFooterConfig(gameInstance);
        sideBarConfig = new SideBarConfig(gameInstance);
        playerNameUnderConfig = new PlayerNameUnderConfig(gameInstance);
        playerNamePrefixConfig = new PlayerNamePrefixConfig(gameInstance);
        playerNameSuffixConfig = new PlayerNameSuffixConfig(gameInstance);

        informationPlayerChangedListener = new InformationPlayerChangedListener(this, gameInstance);
        Bukkit.getPluginManager().registerEvents(informationPlayerChangedListener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public void onReady() {
        this.placeholderManager = gameInstance.get(PlaceholderManager.class);
        animationInformationConfig = new AnimationInformationConfig(gameInstance);
        animationInformationConfig.load();
        animationInformationConfig.animationComponents.forEach((id, pair) ->
                placeholderManager.create(id, player -> pair.right().get(pair.leftInt()))
        );
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
        animationInformationConfig.animationComponents.forEach((string, pair) ->
                pair.left((pair.leftInt() + 1) % pair.right().size())
        );

        switch (gameInstance.getGameState()) {
            case WAITING:
            case STARTING:
            case PLAYING:
            case ENDING:
                InformationRender.renderSidebar(scoreboards, sideBarConfig.load(), placeholderManager, gameInstance.getGameState());
                InformationRender.renderTab(scoreboards, tabHeaderConfig.load(), tabFooterConfig.load(), placeholderManager, gameInstance.getGameState());
                InformationRender.renderPlayerName(scoreboards, playerNamePrefixConfig.load(), playerNameSuffixConfig.load(), playerNameUnderConfig.load(), placeholderManager, gameInstance.getGameState());
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
        HandlerList.unregisterAll(informationPlayerChangedListener);
        informationPlayerChangedListener = null;

        tabHeaderConfig = null;
        tabFooterConfig = null;
        sideBarConfig = null;
        playerNameUnderConfig = null;
        playerNamePrefixConfig = null;
        playerNameSuffixConfig = null;

        animationInformationConfig = null;
        placeholderManager = null;
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