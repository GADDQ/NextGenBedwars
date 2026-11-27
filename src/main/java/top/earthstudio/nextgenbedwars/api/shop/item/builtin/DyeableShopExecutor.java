package top.earthstudio.nextgenbedwars.api.shop.item.builtin;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopBuyAction;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;
import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;
import top.earthstudio.nextgenbedwars.api.util.ColorUtil;

@SuppressWarnings("UnstableApiUsage")
public class DyeableShopExecutor implements ShopBuyAction {
    @Override
    public boolean execute(GameInstance gameInstance, Player player, ShopItem shopItem, ItemStack giveItem) {
        Team team = gameInstance.get(TeamManager.class).get(player);
        if (team == null)
            return true;

        Material type = giveItem.getType();
        if (ColorUtil.isRgbDyeable(type)) {
            giveItem.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(team.teamColor));
            return true;
        }

        if (ColorUtil.isColorableVariant(type)) {
            Material coloredType = ColorUtil.dyeItem(giveItem, team.teamColor).getType();
            if (coloredType != type) {
                giveItem.setType(coloredType);
            }
        }
        return true;
    }
}