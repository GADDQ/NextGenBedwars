package top.earthstudio.nextgenbedwars.core.shop.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;
import top.earthstudio.nextgenbedwars.api.language.ComponentDataType;
import top.earthstudio.nextgenbedwars.api.level.LevelManager;
import top.earthstudio.nextgenbedwars.api.prop.IProp;
import top.earthstudio.nextgenbedwars.api.prop.PropManager;
import top.earthstudio.nextgenbedwars.api.shop.ShopManager;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecutor;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;
import top.earthstudio.nextgenbedwars.api.shop.item.ItemShopManager;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;

import top.earthstudio.nextgenbedwars.core.shop.item.listener.ItemShopLevelListener;
import top.earthstudio.nextgenbedwars.core.spawner.SpawnerTypeConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static top.earthstudio.nextgenbedwars.core.shop.item.ItemShopUtil.translateItem;

@SuppressWarnings("UnstableApiUsage")
public class ItemShopManagerImpl implements ItemShopManager {
    private Map<UUID, ItemShopPage> itemShopPages;
    private Map<Material, Component> resourceNames;
    private List<Category> categories;

    private GameInstance gameInstance;
    private ItemShopConfig itemShopConfig;
    private ShopExecutor shopExecutor;
    private LevelManager levelManager;
    private PropManager propManager;

    private ItemShopLevelListener itemShopLevelListener;

    public ItemShopManagerImpl(GameInstance gameInstance) {
        this.gameInstance = gameInstance;

        itemShopPages = new Object2ObjectOpenHashMap<>();
        resourceNames = new Object2ObjectOpenHashMap<>();

        itemShopConfig = new ItemShopConfig(gameInstance);
        categories = itemShopConfig.load();

        var spawnerTypes = new SpawnerTypeConfig(gameInstance).load();
        if (spawnerTypes != null) {
            spawnerTypes.forEach((id, spawnerType) ->
                    resourceNames.put(spawnerType.material, spawnerType.resourceName)
            );
        }
    }

    @Override
    public void onReady() {
        shopExecutor = gameInstance.get(ShopExecutor.class);
        levelManager = gameInstance.get(LevelManager.class);
        propManager = gameInstance.get(PropManager.class);
        gameInstance.get(ShopManager.class).addProvider("item_shop", this::cacheAndOpenFor);

        itemShopLevelListener = new ItemShopLevelListener(gameInstance, levelManager, itemShopConfig);
        Bukkit.getPluginManager().registerEvents(itemShopLevelListener, BedwarsAPI.getInstance().plugin());
    }

    @Override
    public IGuiPage cacheAndOpenFor(Player player) {
        ItemShopPage itemShopPage = itemShopPages.get(player.getUniqueId());
        if (itemShopPage == null) {
            itemShopPage = new ItemShopPage(this, player);
            itemShopPages.put(player.getUniqueId(), itemShopPage);
        }
        return itemShopPage;
    }

    @Override
    public List<Category> getTemplateCategories() {
        return categories;
    }

    @Override
    public Map<Material, Component> getResourceNames() {
        return resourceNames;
    }

    @Override
    public boolean canAfford(Player player, ShopItem shopItem) {
        if (shopItem.coinType == null || shopItem.price <= 0) {
            return true;
        }
        return countMaterial(player, shopItem.coinType) >= shopItem.price;
    }

    @Override
    public boolean isMaxLevel(Player player, ShopItem shopItem) {
        if (!shopItem.isUpgradeable()) {
            return false;
        }
        var playerGroup = levelManager.getAll(player.getUniqueId());
        boolean hasPurchased = playerGroup != null && playerGroup.containsKey(shopItem.id);
        int currentLevel = hasPurchased ? levelManager.getLevel(player.getUniqueId(), shopItem.id) : -1;
        return hasPurchased && currentLevel >= shopItem.maxLevel;
    }

    @Override
    public int getTargetLevel(Player player, ShopItem shopItem) {
        if (!shopItem.isUpgradeable()) {
            return 0;
        }
        var playerGroup = levelManager.getAll(player.getUniqueId());
        boolean hasPurchased = playerGroup != null && playerGroup.containsKey(shopItem.id);
        if (!hasPurchased) {
            return 0;
        }
        int currentLevel = levelManager.getLevel(player.getUniqueId(), shopItem.id);
        return Math.min(currentLevel + 1, shopItem.maxLevel);
    }

    @Override
    public int getBatchTimes(ShopItem shopItem, boolean fullStack) {
        if (shopItem.isUpgradeable() || (shopItem.executorId != null && !shopItem.executorId.isBlank())) {
            return 1;
        }
        ItemStack sample = (shopItem.actualItems != null && !shopItem.actualItems.isEmpty())
                ? shopItem.getActualItem(0)
                : shopItem.getShowItem(0);
        int maxStack = sample.getMaxStackSize();
        if (maxStack <= 1) {
            return 1;
        }
        int targetAmount = fullStack ? maxStack : Math.max(1, maxStack / 2);
        int singleAmount = Math.max(1, sample.getAmount());
        return Math.max(1, targetAmount / singleAmount);
    }

    @Override
    public boolean buyItem(Player player, ShopItem shopItem) {
        return buyItem(player, shopItem, 1);
    }

    @Override
    public boolean buyItem(Player player, ShopItem shopItem, int times) {
        if (times <= 0) return false;

        if (isMaxLevel(player, shopItem)) {
            player.sendMessage(Component.translatable("nextgenbedwars.shop.item_shop_already_max_level"));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0f);
            return false;
        }

        if (shopItem.isUpgradeable() || (shopItem.executorId != null && !shopItem.executorId.isBlank())) {
            times = 1;
        }

        if (shopItem.coinType != null && shopItem.price > 0) {
            int owned = countMaterial(player, shopItem.coinType);
            int maxAffordable = owned / shopItem.price;
            if (maxAffordable <= 0) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
                player.sendMessage(Component.translatable("nextgenbedwars.shop.item_shop_resource_missing", resourceNames.get(shopItem.coinType), Component.text(shopItem.price - owned)));
                return false;
            }
            times = Math.min(times, maxAffordable);
        }

        int totalPrice = shopItem.price * times;

        int targetLevel = getTargetLevel(player, shopItem);

        ItemStack toGive;
        if (shopItem.actualItems != null && !shopItem.actualItems.isEmpty()) {
            toGive = shopItem.getActualItem(targetLevel).clone();
        } else {
            ItemStack show = shopItem.getShowItem(targetLevel);
            toGive = new ItemStack(show.getType(), show.getAmount());
        }
        toGive.setAmount(toGive.getAmount() * times);

        int existingSlot = shopItem.isUpgradeable() ? findSlotByShopItemId(player, shopItem.id) : -1;

        if (!toGive.isEmpty()) { // exclude Material.AIR, most use for non-physical prop
            if (existingSlot == -1 && !canFit(player, toGive, shopItem.coinType, totalPrice)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(Component.translatable("nextgenbedwars.shop.item_shop_inventory_full"));
                return false;
            }
        }

        IProp prop = shopItem.propId != null ? propManager.get(shopItem.propId) : null;
        if (prop != null && prop.propTexture != null)
            toGive = toGive.withType(prop.propTexture);

        toGive.editMeta(itemMeta -> {
            var pdc = itemMeta.getPersistentDataContainer();
            pdc.set(PdcKeys.shop.itemId, PersistentDataType.STRING, shopItem.id);

            if (shopItem.isDecreaseLevelWhenPlayerDead)
                pdc.set(PdcKeys.shop.downgradeWhenDead, PersistentDataType.BOOLEAN, true);
            if (shopItem.isKeepWhenDead)
                pdc.set(PdcKeys.shop.keepWhenDead, PersistentDataType.BOOLEAN, true);
            if (!shopItem.isThrowable)
                pdc.set(PdcKeys.shop.throwable, PersistentDataType.BOOLEAN, false);

            if (shopItem.propId != null) {
                pdc.set(PdcKeys.prop.id, PersistentDataType.STRING, shopItem.propId);

                if (prop != null) {
                    if (prop.cooldownTick > 0) {
                        var cd = itemMeta.getUseCooldown();
                        cd.setCooldownSeconds(prop.cooldownTick / 20f);
                        cd.setCooldownGroup(new NamespacedKey("nextgenbedwars", "prop_" + shopItem.propId + "_cooldown"));
                        itemMeta.setUseCooldown(cd);
                    }

                    if (prop.isShowGlintEffect)
                        itemMeta.setEnchantmentGlintOverride(true);
                }
            }
        });

        if (shopItem.executorId != null && !shopItem.executorId.isBlank()) {
            if (!shopExecutor.get(shopItem.executorId).execute(player, shopItem)) { // message handle by executor
                return false;
            }
        }

        removeCoin(player, shopItem.coinType, totalPrice);

        int boughtAmount = toGive.getAmount();

        toGive.editMeta(itemMeta -> {
            var pdc = itemMeta.getPersistentDataContainer();

            if (itemMeta.hasItemName())
                pdc.set(PdcKeys.i18n.name, ComponentDataType.COMPONENT, itemMeta.itemName());

            if (itemMeta.hasLore() && itemMeta.lore() != null)
                pdc.set(PdcKeys.i18n.lore, ComponentDataType.COMPONENT_LIST, itemMeta.lore());
        });

        toGive = translateItem(toGive, player.locale());

        if (existingSlot != -1) {
            player.getInventory().setItem(existingSlot, toGive);
        } else {
            player.getInventory().addItem(toGive);
        }

        if (shopItem.isUpgradeable()) {
            var playerGroup = levelManager.getAll(player.getUniqueId());
            if (playerGroup == null || !playerGroup.containsKey(shopItem.id)) {
                levelManager.createGroup(player.getUniqueId());
                levelManager.create(player.getUniqueId(), shopItem.id, shopItem.maxLevel);
            } else {
                levelManager.upgrade(player.getUniqueId(), shopItem.id, 1);
            }
        }

        ItemStack finalItem = shopItem.getShowItem(targetLevel);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        player.sendMessage(Component.translatable(
                "nextgenbedwars.shop.item_shop_buy_item",
                finalItem.getItemMeta().hasItemName() ? finalItem.getItemMeta().itemName() : Component.translatable(finalItem.getType()).color(NamedTextColor.WHITE),
                Component.text(boughtAmount)
        ));
        return true;
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(itemShopLevelListener);
        itemShopLevelListener = null;

        itemShopConfig = null;
        shopExecutor = null;
        propManager = null;
        levelManager = null;
        gameInstance = null;

        itemShopPages.clear();
        itemShopPages = null;

        resourceNames.clear();
        resourceNames = null;

        categories.clear();
        categories = null;
    }

    private boolean canFit(Player player, ItemStack toAdd, Material removeType, int removeAmount) {
        var inv = player.getInventory();
        ItemStack[] contents = inv.getStorageContents();

        ItemStack[] simulated = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            simulated[i] = contents[i] == null ? null : contents[i].clone();
        }

        if (removeType != null && removeAmount > 0) {
            int need = removeAmount;
            for (int i = 0; i < simulated.length && need > 0; i++) {
                ItemStack item = simulated[i];
                if (item != null && item.getType() == removeType) {
                    int take = Math.min(item.getAmount(), need);
                    item.setAmount(item.getAmount() - take);
                    need -= take;
                    if (item.getAmount() <= 0) {
                        simulated[i] = null;
                    }
                }
            }
        }

        int maxStack = toAdd.getMaxStackSize();
        int remaining = toAdd.getAmount();

        if (maxStack > 1) {
            for (ItemStack item : simulated) {
                if (item != null && item.isSimilar(toAdd)) {
                    remaining -= (maxStack - item.getAmount());
                    if (remaining <= 0) return true;
                }
            }
        }

        int emptySlots = 0;
        for (ItemStack item : simulated) {
            if (item == null) emptySlots++;
        }

        return remaining <= (long) emptySlots * maxStack;
    }

    private int findSlotByShopItemId(Player player, String itemId) {
        if (itemId == null) return -1;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            var meta = item.getItemMeta();
            if (meta == null) continue;
            String id = meta.getPersistentDataContainer().get(PdcKeys.shop.itemId, PersistentDataType.STRING);
            if (itemId.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void removeCoin(Player player, Material material, int amount) {
        if (material == null || amount <= 0) return;
        var inv = player.getInventory();
        ItemStack[] storage = inv.getStorageContents();
        int remaining = amount;
        for (int i = 0; i < storage.length && remaining > 0; i++) {
            ItemStack item = storage[i];
            if (item == null || item.getType() != material) continue;

            int take = Math.min(item.getAmount(), remaining);
            item.setAmount(item.getAmount() - take);
            remaining -= take;
            inv.setItem(i, item.getAmount() <= 0 ? null : item);
        }
    }

    private int countMaterial(Player player, Material material) {
        if (material == null) return 0;
        int total = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }
}