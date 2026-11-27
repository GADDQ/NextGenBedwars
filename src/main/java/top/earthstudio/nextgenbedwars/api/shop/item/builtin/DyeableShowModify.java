package top.earthstudio.nextgenbedwars.api.shop.item.builtin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.showmodifier.ShopShowModify;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.ColorUtil;

public class DyeableShowModify implements ShopShowModify {
    @Override
    public ItemStack modify(GameInstance gameInstance, Player player, ShopItem shopItem, int targetLevel, ItemStack showItem) {
        if (!ColorUtil.isDyeable(showItem.getType()))
            return showItem;

        Team team = gameInstance.get(TeamManager.class).get(player);
        if (team == null)
            return showItem;

        return ColorUtil.dyeItem(showItem, team.teamColor);
    }
}