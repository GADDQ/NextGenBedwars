package top.earthstudio.nextgenbedwars.api.shop;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.gui.GuiPage;
import top.earthstudio.nextgenbedwars.api.gui.GuiUtil;

import java.util.List;

public class ShopPage extends GuiPage {
    private boolean isCategoryFullSize = false;
    private int activeCategoryIndex = 0;
    private int currentCategoryIconPage = 0;
    private int currentCategoryContentPage = 0;

    private final List<Category> categories;

    public ShopPage(Component title) {
        super(title, 6);
        this.allowPlayerInventoryInteraction = true;
        categories = new ObjectArrayList<>();

        // FIXME: TEST ONLY
        test();

        buildPageDivider(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        buildPage();
    }

    private void buildPage() {
        clearCategoryIconSlots();
        clearCategoryContentSlots(isCategoryFullSize);
        clearPageDivider();
        buildPageDivider(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        buildCategoryList(currentCategoryIconPage);
        buildCategoryContent(categories.get(activeCategoryIndex), currentCategoryContentPage, isCategoryFullSize);
    }

    public void addCategory(Category category) {
        categories.add(category);
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

        int index = page * categorySize;
        loop: for (int y = 2; y <= 4; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (index >= category.items.size())
                    break loop;

                int finalIndex = index;
                add(
                        x,
                        y,
                        category.items.get(index).showItem,
                        (player, clickType) -> { // TODO: add to quick buy, buy x64, buy x32
                            if (clickType != ClickType.LEFT)
                                return;

                            buyItem(player, category.items.get(finalIndex));
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

        if (page > 0 && page < category.items.size() / categorySize) {
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

    private void clearCategoryContentSlots(boolean isFullSize) {
        for (int x = 0; x <= 8; x++) {
            for (int y = 2; y <= 4; y++) {
                remove(x, y);
            }
        }
    }

    private void buyItem(Player player, ShopItem shopItem) { // TODO: i18n
        ItemStack cost = new ItemStack(shopItem.coinType, shopItem.price);

        // 检查货币数量
        if (!player.getInventory().containsAtLeast(cost, shopItem.price)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
            player.sendMessage(Component.text("货币不足！需要 " + shopItem.price + " 个 ").color(NamedTextColor.RED).append(Component.translatable(shopItem.coinType.getItemTranslationKey())));
            return;
        }

        ItemStack toGive;
        if (shopItem.actualItem != null)
            toGive = shopItem.actualItem.clone();
        else
            toGive = new ItemStack(shopItem.showItem.getType(), shopItem.showItem.getAmount());

        if (!canFit(player, toGive)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(Component.text("购买失败！背包已满！").color(NamedTextColor.RED));
            return;
        }

        player.getInventory().removeItem(cost);
        player.getInventory().addItem(toGive);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        player.sendMessage(Component.text("购买成功！").color(NamedTextColor.GREEN)); // TODO: replace text "你购买了 什么 多少"
    }

    private boolean canFit(Player player, ItemStack toAdd) {
        var inv = player.getInventory();

        if (inv.firstEmpty() != -1)
            return true;

        if (toAdd.getMaxStackSize() <= 1)
            return false;

        int remaining = toAdd.getAmount();
        // 只扫描玩家背包
        for (ItemStack item : inv.getStorageContents()) {
            if (item != null && item.isSimilar(toAdd)) {
                remaining -= (item.getMaxStackSize() - item.getAmount());
                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private void test() {
        Category testPage1 = new Category(GuiUtil.button(Material.GRASS_BLOCK, Component.text("测试栏位"), Component.text("这是个测试!")));
        testPage1.items.add(new ShopItem(new ItemStack(Material.COPPER_BLOCK), Material.COPPER_INGOT, 4));
        Category testPage2 = new Category(GuiUtil.button(Material.DIRT, Component.text("栏位测试"), Component.text("测试在这里!")));
        testPage2.items.add(new ShopItem(new ItemStack(Material.GOLD_BLOCK), Material.IRON_INGOT, 4));
        Category testPage3 = new Category(GuiUtil.button(Material.NOTE_BLOCK, Component.text("CategoryContent 翻页测试"), Component.text("它能用吗?")));
        for (int i = 0; i < 100; i++) {
            testPage3.items.add(new ShopItem(new ItemStack(Material.DIAMOND_BLOCK), Material.GOLD_INGOT, 4));
        }
        categories.add(testPage1);
        categories.add(testPage2);
        categories.add(testPage3);
        for (int i = 0; i < 50; i++) {
            categories.add(new Category(GuiUtil.button(Material.BARRIER, Component.text("CategoryIcon 翻页测试"), Component.text("这个也能用吗?"))));
        }
    }
}
