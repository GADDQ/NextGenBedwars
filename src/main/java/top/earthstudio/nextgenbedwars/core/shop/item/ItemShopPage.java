package top.earthstudio.nextgenbedwars.core.shop.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

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
    private final ItemShopAction itemShopUtil;
    private final Player player;

    public final List<Category> categories;

    public ItemShopPage(GameInstance gameInstance, Player player, Component title, List<Category> categories) { // TODO: i18n
        super(title, 6);
        this.levelManager = gameInstance.get(LevelManager.class);
        this.itemShopUtil = new ItemShopAction(gameInstance.get(ShopExecutor.class));
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
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0f);
                                return;
                            }

                            int targetLevel = hasPurchased ? currentLevel + 1 : 0;

                            if (itemShopUtil.buyItem(player, shopItem, targetLevel)) {
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
}
