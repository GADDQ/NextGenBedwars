package top.earthstudio.nextgenbedwars.core.team.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.team.event.BedwarsPlayerDeadEvent;
import top.earthstudio.nextgenbedwars.api.team.event.BedwarsPlayerRespawnEvent;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;
import top.earthstudio.nextgenbedwars.api.util.ticker.TickerTask;

import java.time.Duration;
import java.util.UUID;

public class TeamRespawnListener implements Listener {
    private final TeamManager teamManager;
    private final GameInstance gameInstance;

    public TeamRespawnListener(TeamManager teamManager, GameInstance gameInstance) {
        this.teamManager = teamManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPLayerFallInVoid(EntityDamageEvent event) { // TODO: Send event with uncancelled property
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntityType() != EntityType.PLAYER)
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.VOID)
            return;

        Player player = (Player) event.getEntity();

        // TODO: spectator logic

        if (teamManager.get(player) == null)
            return;

        Team team = teamManager.get(player);

        if (!team.isBedAlive)
            teamManager.getSpectators(team).add(player);

        BedwarsPlayerDeadEvent sendEvent = new BedwarsPlayerDeadEvent(gameInstance, player, !team.isBedAlive);
        Bukkit.getPluginManager().callEvent(sendEvent);

        event.setCancelled(true);
        tryRespawn(player, team, true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDead(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld() != gameInstance.getWorld())
            return;

        if (teamManager.get(player) == null)
            return;

        Team team = teamManager.get(player);

        if (!team.isBedAlive)
            teamManager.getSpectators(team).add(player);

        event.setCancelled(true);
        event.deathMessage(null);

        // TODO: kill message
        BedwarsPlayerDeadEvent sendEvent = new BedwarsPlayerDeadEvent(gameInstance, player, !team.isBedAlive);
        Bukkit.getPluginManager().callEvent(sendEvent);

        if (sendEvent.isCancelled())
            return;

        tryRespawn(player, team, true);
    }

    private void tryRespawn(Player player, Team team, boolean teleportToSpectatorRespawnPoint) { // TODO: respawn cannot see other player, this == below TODO
        player.setFireTicks(0);
        player.clearActivePotionEffects();
        player.setFallDistance(0);
        player.heal(Double.MAX_VALUE);
        player.setFoodLevel(20);
        player.setSaturation(0);
        clearInventory(player);
        player.setGameMode(GameMode.SPECTATOR); // TODO: this need replace with fake spectator mode (adventure) in the future!
        player.setVelocity(new Vector(0, 0, 0));

        UUID uuid = UUID.randomUUID();

        if (!team.isBedAlive) {
            GlobalTicker.set(uuid, () -> {
                GlobalTicker.remove(uuid);
                player.teleport(gameInstance.getGame().spectatorRespawnLocation);
            });
            return;
        }

        GlobalTicker.set(uuid, new TickerTask() {
            boolean isFirst = true;
            int tick = team.respawnTick;

            @Override
            public void run() { // TODO: i18n
                if (isFirst) {
                    if (teleportToSpectatorRespawnPoint)
                        player.teleport(gameInstance.getGame().spectatorRespawnLocation); // TODO: Configurable

                    isFirst = false;
                }

                if (tick % 20 == 0 && tick > 0) {
                    int remainingSeconds = tick / 20;
                    player.showTitle(Title.title(
                            Component.text("你死了！").color(NamedTextColor.RED),
                            Component.text("你将在 ").append(Component.text(remainingSeconds).color(NamedTextColor.YELLOW)).append(Component.text(" 秒后重生")),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
                    ));
                }

                if (tick <= 0) {
                    GlobalTicker.remove(uuid);

                    player.teleport(team.respawnLocation);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                    player.showTitle(Title.title(
                            Component.text("已重生！").color(NamedTextColor.GREEN),
                            Component.text(""),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
                    ));

                    Bukkit.getPluginManager().callEvent(new BedwarsPlayerRespawnEvent(gameInstance, player));
                }

                tick--;
            }
        });
    }

    private void clearInventory(Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack[] storageContents = inventory.getStorageContents();
        for (int i = 0; i < storageContents.length; i++) {
            if (shouldKeep(storageContents[i])) continue;
            if (storageContents[i] != null && !storageContents[i].getType().isAir()) {
                inventory.clear(i);
            }
        }

        ItemStack[] armorContents = inventory.getArmorContents();
        boolean armorChanged = false;
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack item = armorContents[i];
            if (item == null || item.getType().isAir()) continue;
            if (shouldKeep(item)) continue;
            armorContents[i] = null;
            armorChanged = true;
        }
        if (armorChanged) {
            inventory.setArmorContents(armorContents);
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (!offHand.getType().isAir() && !shouldKeep(offHand)) {
            inventory.setItemInOffHand(null);
        }
    }

    /** 带有 keepWhenDead 标记的物品在死亡时保留 */
    private boolean shouldKeep(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        var meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(PdcKeys.shop.keepWhenDead);
    }
}
