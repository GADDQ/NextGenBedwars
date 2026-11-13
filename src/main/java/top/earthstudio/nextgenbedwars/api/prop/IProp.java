package top.earthstudio.nextgenbedwars.api.prop;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;

@SuppressWarnings("unused")
public abstract class IProp {
    // TODO: Special Item (e.g. TNT, tower, wall, platform, etc.)
    public final String id;

    protected IProp(String id) {
        this.id = id;
    }

    public boolean onPick(GameInstance gameInstance, Player player, ItemStack prop) { return true; }
    public boolean onDrop(GameInstance gameInstance, Player player, Item prop) { return true; }

    public boolean onClick(GameInstance gameInstance, Player player, ItemStack propItem) { return true; }
    public boolean onUse(GameInstance gameInstance, Player player, ItemStack propItem) { return true; }

    public boolean onClickOnBlock(GameInstance gameInstance, Player player, Block block, ItemStack propItem) { return onClick(gameInstance, player, propItem); }
    public boolean onUseOnBlock(GameInstance gameInstance, Player player, Block block, ItemStack propItem) { return onUse(gameInstance, player, propItem); }

    public boolean onAttack(GameInstance gameInstance, Player attacker, Player victim, ItemStack propItem) {
        onClick(gameInstance, attacker, propItem);
        return true;
    }

    public boolean onUseOnPlayer(GameInstance gameInstance, Player user, Player bearer, ItemStack propItem) {
        onUse(gameInstance, user, propItem);
        return true;
    }

    public boolean onConsume(GameInstance gameInstance, Player player, ItemStack propItem) {
        return true;
    }

    public boolean onCrossbowLoad(GameInstance gameInstance, Player player, ItemStack propItem) {
        return true;
    }

    /**
     * Called when this prop (bow or crossbow) shoots a projectile.
     *
     * <p>The return value is a {@link it.unimi.dsi.fastutil.Pair Pair}:
     * <ul>
     *   <li><b>Left (Boolean):</b> Whether to cancel the original shooting event ({@code true} to cancel).</li>
     *   <li><b>Right (Entity):</b> The replacement projectile entity to shoot instead, or {@code null} to keep the original.</li>
     * </ul>
     *
     * <p><b>Notes:</b>
     * <ul>
     *   <li>If the event is cancelled (Left = {@code true}), the replacement projectile will be ignored.</li>
     *   <li>If the replacement entity is {@code null} (Right = {@code null}), the original projectile will be shot as normal.</li>
     * </ul>
     *
     * @return a pair where Left controls event cancellation and Right provides an optional replacement projectile
     */
    public Pair<Boolean, Entity> onBowShoot(GameInstance gameInstance, Player player, ItemStack propItem) {
        return new ObjectObjectImmutablePair<>(true, null);
    }
}
