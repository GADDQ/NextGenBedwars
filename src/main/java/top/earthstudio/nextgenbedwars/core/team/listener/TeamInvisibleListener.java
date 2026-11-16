package top.earthstudio.nextgenbedwars.core.team.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

import io.papermc.paper.event.player.PlayerTrackEntityEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

import java.util.Map;
import java.util.Objects;

public class TeamInvisibleListener implements Listener {
    private final TeamManager teamManager;
    private final GameInstance gameInstance;

    private final Map<EquipmentSlot, ItemStack> emptyArmor = Map.of(
            EquipmentSlot.HEAD, ItemStack.empty(),
            EquipmentSlot.CHEST, ItemStack.empty(),
            EquipmentSlot.LEGS, ItemStack.empty(),
            EquipmentSlot.FEET, ItemStack.empty()
    );

    public TeamInvisibleListener(TeamManager teamManager, GameInstance gameInstance) { // TODO: configurable enable/disable
        this.teamManager = teamManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInvisible(EntityPotionEffectEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        if (event.getModifiedType() != PotionEffectType.INVISIBILITY) return;

        Player player = (Player) event.getEntity();

        if (teamManager.isSpectator(player))
            return;

        switch (event.getAction()) {
            case ADDED:
            case CHANGED:
                applyInvisibleVisual(player);
                break;
            case CLEARED:
            case REMOVED:
                restoreArmorVisual(player);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTrackInvisiblePlayer(PlayerTrackEntityEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        Player invisiblePlayer = (Player) event.getEntity();

        if (teamManager.isSpectator(invisiblePlayer))
            return;

        if (invisiblePlayer.getPotionEffect(PotionEffectType.INVISIBILITY) == null)
            return;

        Player viewer = event.getPlayer();
        if (teamManager.get(viewer) == teamManager.get(invisiblePlayer))
            return;

        Bukkit.getScheduler().runTask(BedwarsAPI.getInstance().plugin(), () -> viewer.sendEquipmentChange(invisiblePlayer, emptyArmor));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChangedArmor(PlayerArmorChangeEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        Player player = event.getPlayer();

        if (teamManager.isSpectator(player))
            return;

        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY))
            return;

        Bukkit.getScheduler().runTask(BedwarsAPI.getInstance().plugin(), () -> applyInvisibleVisual(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHurt(EntityDamageByEntityEvent event) { // TODO: i18n
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();

        if (teamManager.isSpectator(player))
            return;

        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY))
            return;

        player.sendMessage(Component.text("由于受到敌方攻击，你的隐身已失效！").color(NamedTextColor.RED));
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        restoreArmorVisual(player);
    }

    private void applyInvisibleVisual(Player player) {
        Team playerTeam = teamManager.get(player);

        teamManager.getAll().forEach(team -> {
            if (team == playerTeam)
                return;

            team.players.forEach(otherTeamPlayer -> otherTeamPlayer.sendEquipmentChange(player, emptyArmor));
        });
    }

    private void restoreArmorVisual(Player player) {
        Team playerTeam = teamManager.get(player);

        PlayerInventory inventory = player.getInventory();
        Map<EquipmentSlot, ItemStack> realArmor = Map.of(
                EquipmentSlot.HEAD, Objects.requireNonNullElse(inventory.getHelmet(), ItemStack.empty()),
                EquipmentSlot.CHEST, Objects.requireNonNullElse(inventory.getChestplate(), ItemStack.empty()),
                EquipmentSlot.LEGS, Objects.requireNonNullElse(inventory.getLeggings(), ItemStack.empty()),
                EquipmentSlot.FEET, Objects.requireNonNullElse(inventory.getBoots(), ItemStack.empty())
        );

        teamManager.getAll().forEach(team -> {
            if (team == playerTeam)
                return;

            team.players.forEach(otherTeamPlayer -> otherTeamPlayer.sendEquipmentChange(player, realArmor));
        });
    }
}
