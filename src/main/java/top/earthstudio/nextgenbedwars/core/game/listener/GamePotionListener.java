package top.earthstudio.nextgenbedwars.core.game.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;

public class GamePotionListener implements Listener {
    private final GameInstance gameInstance;

    public GamePotionListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerApplyEffect(EntityPotionEffectEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();

        switch (event.getAction()) {
            case ADDED:
            case CHANGED:
                PotionEffect newEffect = event.getNewEffect();
                if (newEffect != null && newEffect.hasParticles()) {
                    event.setCancelled(true);
                    player.addPotionEffect(new PotionEffect(
                            newEffect.getType(),
                            newEffect.getDuration(),
                            newEffect.getAmplifier(),
                            newEffect.isAmbient(),
                            false,
                            newEffect.hasIcon()
                    ));
                }
                break;
            default:
                break;
        }
    }
}
