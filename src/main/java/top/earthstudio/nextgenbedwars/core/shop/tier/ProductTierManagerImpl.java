package top.earthstudio.nextgenbedwars.core.shop.tier;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.event.HandlerList;
import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.tier.ProductTierManager;
import top.earthstudio.nextgenbedwars.core.shop.tier.listener.ProductTierListener;

import java.util.Map;
import java.util.UUID;

public class ProductTierManagerImpl implements ProductTierManager {
    private Map<UUID, Object2IntOpenHashMap<String>> tiersMap;
    private ProductTierListener productTierListener;

    public ProductTierManagerImpl(GameInstance gameInstance) {
        tiersMap = new Object2ObjectOpenHashMap<>();

        productTierListener = new ProductTierListener(gameInstance, this);
        Bukkit.getPluginManager().registerEvents(productTierListener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public int getTier(Player player, String id) {
        Object2IntOpenHashMap<String> tier = tiersMap.get(player.getUniqueId());
        return tier != null ? tier.getInt(id) : 0;
    }

    @Override
    public void upgradeTier(Player player, String id, int maxLevel) {
        Object2IntOpenHashMap<String> tiers = tiersMap.computeIfAbsent(
                player.getUniqueId(), k -> new Object2IntOpenHashMap<>()
        );
        int current = tiers.getInt(id);
        if (current < maxLevel) {
            tiers.put(id, current + 1);
        }
    }

    @Override
    public void downgradeTier(Player player, String id) {
        Object2IntOpenHashMap<String> tiers = tiersMap.computeIfAbsent(
                player.getUniqueId(), k -> new Object2IntOpenHashMap<>()
        );
        int current = tiers.getInt(id);
        if (current > 0) {
            tiers.put(id, current - 1);
        }
    }

    @Override
    public void downgradeAllTiers(Player player) {
        Object2IntOpenHashMap<String> tiers = tiersMap.get(player.getUniqueId());
        if (tiers == null || tiers.isEmpty()) return;

        tiers.replaceAll((id, level) -> Math.max(0, level - 1));
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(productTierListener);
        productTierListener = null;

        tiersMap.clear();
        tiersMap = null;
    }
}
