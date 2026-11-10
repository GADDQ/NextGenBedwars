package top.earthstudio.nextgenbedwars.core.shop.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiPage;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecutor;
import top.earthstudio.nextgenbedwars.api.level.LevelManager;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class ItemShopPage extends ChestGuiPage {
    private final boolean isCategoryFullSize = false;
    private int activeCategoryIndex = 0;
    private int currentCategoryIconPage = 0;
    private int currentCategoryContentPage = 0;

    private final LevelManager levelManager;
    private final ShopExecutor shopExecutor;
    private final Player player;

    public final List<Category> categories;

    public ItemShopPage(GameInstance gameInstance, Player player, Component title, List<Category> categories) { // TODO: i18n
        super(title, 6);
        this.levelManager = gameInstance.get(LevelManager.class);
        this.shopExecutor = gameInstance.get(ShopExecutor.class);
        this.player = player;

        this.allowPlayerInventoryInteraction = true;

        if (categories == null || categories.isEmpty())
            throw new IllegalStateException("ItemShopPage requires at least one category");

        this.categories = new ObjectArrayList<>(categories);
        buildPage();
    }

    @Override
    public void onOpen(Player player) {
        activeCategoryIndex = 0;
        currentCategoryIconPage = 0;
        currentCategoryContentPage = 0;
        buildPage();
    }

    private void buildPage() {
        clearCategoryIconSlots();
        clearCategoryContentSlots();
        clearPageDivider();
        buildPageDivider(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        buildCategoryList(currentCategoryIconPage);
        buildCategoryContent(categories.get(activeCategoryIndex), currentCategoryContentPage, isCategoryFullSize);
    }

    private void buildCategoryList(int page) {
        final int startIndex = page * 9;

        for (int i = 0; i <= 8; i++) {
            int targetIndex = startIndex + i;
            final int index = targetIndex;

            if (targetIndex >= categories.size())
                break;

            Category category = categories.get(targetIndex);

            if (category != null)
                add(
                        i,
                        0,
                        category.categoryIcon,
                        (player, clickType) ->  {
                            currentCategoryContentPage = 0;
                            activeCategoryIndex = index;
                            buildPage();
                            update();
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        }
                );
        }

        buildCategoryIconFlipButton();
    }

    private void buildPageDivider(ItemStack divider) {
        for (int x = 0; x <= 8; x++) {
            if (x == 4) {
                add(
                        x,
                        1,
                        categories.get(activeCategoryIndex).categoryIcon
                );
            } else {
                add(
                        x,
                        1,
                        divider
                );
            }

            add(
                    x,
                    5,
                    divider
            );
        }
    }

    private void buildCategoryContent(Category category, int page, boolean isFullSize) {
        int minX = isFullSize ? 0 : 1;
        int maxX = isFullSize ? 8 : 7;
        int categorySize = isFullSize ? 27 : 21;

        var playerGroup = levelManager.getAll(player.getUniqueId());

        int index = page * categorySize;
        loop: for (int y = 2; y <= 4; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (index >= category.items.size())
                    break loop;

                ShopItem shopItem = category.items.get(index);

                boolean hasPurchased = shopItem.isUpgradeable()
                        && playerGroup != null
                        && playerGroup.containsKey(shopItem.id);

                int currentLevel = hasPurchased ? levelManager.getLevel(player.getUniqueId(), shopItem.id) : -1;
                boolean isMaxLevel = hasPurchased && currentLevel >= shopItem.maxLevel;

                ItemStack showItem;
                if (!shopItem.isUpgradeable()) {
                    showItem = shopItem.getShowItem(0);
                } else if (isMaxLevel) {
                    showItem = shopItem.getShowItem(currentLevel);
                } else {
                    int targetLevel = hasPurchased ? currentLevel + 1 : 0;
                    showItem = shopItem.getShowItem(targetLevel);
                }

                add(
                        x,
                        y,
                        showItem,
                        (player, clickType) -> {
                            if (clickType != ClickType.LEFT) // TODO: quick buy action: x32, x64
                                return;

                            if (isMaxLevel) {
                                player.sendMessage(Component.text("该物品已达到最高等级！").color(NamedTextColor.RED));
                                return;
                            }

                            int targetLevel = hasPurchased ? currentLevel + 1 : 0;

                            if (buyItem(player, shopItem, targetLevel)) {
                                if (shopItem.isUpgradeable()) {
                                    if (!hasPurchased) {
                                        levelManager.createGroup(player.getUniqueId());
                                        levelManager.create(player.getUniqueId(), shopItem.id, shopItem.maxLevel);
                                    } else {
                                        levelManager.upgrade(player.getUniqueId(), shopItem.id, 1);
                                    }
                                }

                                buildPage();
                                update();
                            }
                        }
                );
                index++;
            }
        }

        buildCategoryContentFlipButton(page, categorySize, category);
    }

    private void buildCategoryIconFlipButton() { // TODO: Material = divider
        remove(0, 1);
        remove(8, 1);

        if (categories.size() <= 9) {
            add(0, 1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            add(8, 1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            return;
        }

        if (currentCategoryIconPage == 0) {
            add(0, 1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            add(
                    8,
                    1,
                    new ItemStack(Material.ARROW),
                    (player, clickType) -> {
                        currentCategoryIconPage++;
                        buildPage();
                        update();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    }
            );
            return;
        }

        if (currentCategoryIconPage > 0 && currentCategoryIconPage < (categories.size() - 1) / 9) {
            add(
                    0,
                    1,
                    new ItemStack(Material.ARROW),
                    (player, clickType) -> {
                        currentCategoryIconPage--;
                        buildPage();
                        update();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    }
            );
            add(
                    8,
                    1,
                    new ItemStack(Material.ARROW),
                    (player, clickType) -> {
                        currentCategoryIconPage++;
                        buildPage();
                        update();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    }
            );
            return;
        }

        add(
                0,
                1,
                new ItemStack(Material.ARROW),
                (player, clickType) -> {
                    currentCategoryIconPage--;
                    buildPage();
                    update();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                }
        );
        add(8, 1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
    }

    private void buildCategoryContentFlipButton(int page, int categorySize, Category category) {
        remove(0, 5);
        remove(8, 5);

        if (category.items.size() <= categorySize) {
            add(0, 5, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            add(8, 5, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            return;
        }

        if (page == 0) {
            add(0, 5, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            add(
                    8,
                    5,
                    new ItemStack(Material.ARROW),
                    (player, clickType) -> {
                        currentCategoryContentPage++;
                        buildPage();
                        update();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    }
            );
            return;
        }

        if (page > 0 && page < (category.items.size() - 1) / categorySize) {
            add(
                    0,
                    5,
                    new ItemStack(Material.ARROW),
                    (player, clickType) -> {
                        currentCategoryContentPage--;
                        buildPage();
                        update();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    }
            );
            add(
                    8,
                    5,
                    new ItemStack(Material.ARROW),
                    (player, clickType) -> {
                        currentCategoryContentPage++;
                        buildPage();
                        update();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    }
            );
            return;
        }

        add(
                0,
                5,
                new ItemStack(Material.ARROW),
                (player, clickType) -> {
                    currentCategoryContentPage--;
                    buildPage();
                    update();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                }
        );
        add(8, 5, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
    }

    private void clearPageDivider() {
        for (int i = 0; i <= 8; i++) {
            remove(i, 1);
            remove(i, 5);
        }
    }

    private void clearCategoryIconSlots() {
        for (int i = 0; i <= 8; i++) {
            remove(i);
        }
    }

    private void clearCategoryContentSlots() {
        for (int x = 0; x <= 8; x++) {
            for (int y = 2; y <= 4; y++) {
                remove(x, y);
            }
        }
    }

    private boolean buyItem(Player player, ShopItem shopItem, int targetLevel) { // TODO: i18n
        ItemStack cost = new ItemStack(shopItem.coinType, shopItem.price);

        if (!player.getInventory().containsAtLeast(cost, shopItem.price)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
            player.sendMessage(Component.text("货币不足！需要 " + shopItem.price + " 个 ")
                    .color(NamedTextColor.RED)
                    .append(Component.translatable(requireNonNull(shopItem.coinType.getItemTranslationKey()))));
            return false;
        }

        ItemStack toGive;
        if (shopItem.actualItems != null && !shopItem.actualItems.isEmpty())
            toGive = shopItem.getActualItem(targetLevel).clone();
        else
            toGive = new ItemStack(shopItem.getShowItem(targetLevel).getType(), shopItem.getShowItem(targetLevel).getAmount());

        int existingSlot = -1;
        if (shopItem.isUpgradeable()) {
            existingSlot = findSlotByShopItemId(player, shopItem.id);
        }

        if (existingSlot == -1 && !canFit(player, toGive, cost)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(Component.text("购买失败！背包已满！").color(NamedTextColor.RED));
            return false;
        }

        toGive.editMeta(itemMeta -> {
            var pdc = itemMeta.getPersistentDataContainer();
            if (shopItem.isDecreaseLevelWhenPlayerDead)
                pdc.set(PdcKeys.shop.downgradeWhenDead, PersistentDataType.BOOLEAN, true);
            if (shopItem.isKeepWhenDead)
                pdc.set(PdcKeys.shop.keepWhenDead, PersistentDataType.BOOLEAN, true);
            if (!shopItem.isThrowable)
                pdc.set(PdcKeys.shop.throwable, PersistentDataType.BOOLEAN, false);
            if (shopItem.id != null)
                pdc.set(PdcKeys.shop.itemId, PersistentDataType.STRING, shopItem.id);
        });

        if (shopItem.executorId != null && !shopItem.executorId.isBlank()) {
            if (!shopExecutor.get(shopItem.executorId).execute(player, shopItem)) {
                player.sendMessage(Component.text("购买失败！").color(NamedTextColor.RED));
                return false;
            }
        }

        player.getInventory().removeItem(cost);

        if (existingSlot != -1) {
            player.getInventory().setItem(existingSlot, toGive);
        } else {
            player.getInventory().addItem(toGive);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        player.sendMessage(Component.text("购买成功！").color(NamedTextColor.GREEN)); // TODO: buy what x?
        return true;
    }

    private boolean canFit(Player player, ItemStack toAdd, ItemStack toRemove) {
        var inv = player.getInventory();
        ItemStack[] contents = inv.getStorageContents();

        ItemStack[] simulated = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            simulated[i] = contents[i] == null ? null : contents[i].clone();
        }

        if (toRemove != null && toRemove.getAmount() > 0) {
            int need = toRemove.getAmount();
            for (int i = 0; i < simulated.length && need > 0; i++) {
                ItemStack item = simulated[i];
                if (item != null && item.isSimilar(toRemove)) {
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
}
