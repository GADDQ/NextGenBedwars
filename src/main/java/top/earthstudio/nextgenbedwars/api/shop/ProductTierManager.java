package top.earthstudio.nextgenbedwars.api.shop;

import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;

public interface ProductTierManager extends IGameSubSystem {
    int getTier(Player player, String id);
    void upgradeTier(Player player, String id, int maxLevel);
    void downgradeTier(Player player, String id);
    void downgradeAllTiers(Player player);

    @Override
    default void update() {}
}
