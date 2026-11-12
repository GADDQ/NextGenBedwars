package top.earthstudio.nextgenbedwars.api.prop;

import org.bukkit.block.Block;
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

    public boolean onPick(GameInstance gameInstance, Player player) { return true; }
    public boolean onDrop(GameInstance gameInstance, Player player) { return true; }

    public void onClick(GameInstance gameInstance, Player player, ItemStack propItem) {}
    public void onUse(GameInstance gameInstance, Player player, ItemStack propItem) {}

    public void onClickOnBlock(GameInstance gameInstance, Player player, Block block, ItemStack propItem) { onClick(gameInstance, player, propItem); }
    public void onUseOnBlock(GameInstance gameInstance, Player player, Block block, ItemStack propItem) { onUse(gameInstance, player, propItem); }

    public boolean onAttack(GameInstance gameInstance, Player attacker, Player victim, ItemStack propItem) {
        onClick(gameInstance, attacker, propItem);
        return true;
    }

    public boolean onUseOnPlayer(GameInstance gameInstance, Player user, Player bearer, ItemStack propItem) {
        onUse(gameInstance, user, propItem);
        return true;
    }
}
