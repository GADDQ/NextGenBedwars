package top.earthstudio.nextgenbedwars.api.shop.item.builtin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecute;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.ColorUtil;

public class DyeableShopExecutor implements ShopExecute {
    @Override
    public ItemStack execute(GameInstance gameInstance, Player player, ShopItem shopItem, ItemStack giveItem) {
        if (!ColorUtil.isDyeable(giveItem.getType()))
            return giveItem;

        Team team = gameInstance.get(TeamManager.class).get(player);
        if (team == null)
            return giveItem;

        return ColorUtil.dyeItem(giveItem, team.teamColor);
    }
}