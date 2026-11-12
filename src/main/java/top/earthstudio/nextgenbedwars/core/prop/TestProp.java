package top.earthstudio.nextgenbedwars.core.prop;

import net.kyori.adventure.text.Component;

import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.prop.IProp;

public class TestProp extends IProp {
    public TestProp() {
        super("test");
    }

    @Override
    public boolean onAttack(GameInstance gameInstance, Player attacker, Player victim, ItemStack propItem) {
        attacker.sendMessage(Component.text("使用道具攻击"));
        victim.sendMessage(Component.text("被道具攻击"));
        return true;
    }

    @Override
    public boolean onDrop(GameInstance gameInstance, Player player, Item prop) {
        player.sendMessage(Component.text("丢弃道具"));
        prop.setUnlimitedLifetime(true);
        return true;
    }

    @Override
    public boolean onPick(GameInstance gameInstance, Player player, ItemStack prop) {
        player.sendMessage(Component.text("捡起道具"));
        return true;
    }

    @Override
    public void onClick(GameInstance gameInstance, Player player, ItemStack propItem) {
        player.sendMessage(Component.text("左键点击道具"));
    }

    @Override
    public void onClickOnBlock(GameInstance gameInstance, Player player, Block block, ItemStack propItem) {
        player.sendMessage(Component.text("道具左键点击方块"));
    }

    @Override
    public void onUse(GameInstance gameInstance, Player player, ItemStack propItem) {
        player.sendMessage(Component.text("右键点击道具"));
    }

    @Override
    public void onUseOnBlock(GameInstance gameInstance, Player player, Block block, ItemStack propItem) {
        player.sendMessage(Component.text("道具右键点击方块"));
    }

    @Override
    public boolean onUseOnPlayer(GameInstance gameInstance, Player user, Player bearer, ItemStack propItem) {
        user.sendMessage(Component.text("道具右键玩家"));
        bearer.sendMessage(Component.text("被道具右键"));
        return true;
    }
}
