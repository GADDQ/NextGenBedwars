package top.earthstudio.nextgenbedwars.core.shop.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiPage;
import top.earthstudio.nextgenbedwars.api.shop.item.Category;
import top.earthstudio.nextgenbedwars.api.shop.item.ItemShopManager;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;

import java.util.List;
import java.util.Locale;

public class ItemShopPage extends ChestGuiPage {
    private static final Style DEFAULT_LORE_STYLE = Style.style()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .build();

    private final boolean isCategoryFullSize = false; // TODO: configurable
    private final boolean isResetMainPageWhenOpen = true; // TODO: configurable
    private int activeCategoryIndex = 0;
    private int currentCategoryIconPage = 0;
    private int currentCategoryContentPage = 0;

    private final List<Category> categories;

    private final ItemShopManager itemShopManager;

    public ItemShopPage(ItemShopManager itemShopManager, Player player) {
        super(Component.translatable("nextgenbedwars.shop.item_shop_title"), 6, false);

        this.itemShopManager = itemShopManager;

        this.categories = itemShopManager.getTemplateCategories();

        buildPage(player);
    }

    @Override
    public void onOpen(Player player) {
        if (isResetMainPageWhenOpen) {
            activeCategoryIndex = 0;
            currentCategoryIconPage = 0;
            currentCategoryContentPage = 0;
        }

        buildPage(player);
        update();
    }

    private void buildPage(Player player) {
        clearCategoryIconSlots();
        clearCategoryContentSlots();
        clearPageDivider();
        buildPageDivider(player, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        buildCategoryList(player, currentCategoryIconPage);
        buildCategoryContent(player, categories.get(activeCategoryIndex), currentCategoryContentPage, isCategoryFullSize);
    }

    private void buildCategoryList(Player player, int page) {
        final int startIndex = page * 9;

        for (int i = 0; i <= 8; i++) {
            int targetIndex = startIndex + i;
            if (targetIndex >= categories.size())
                break;

            Category category = categories.get(targetIndex);
            if (category != null) {
                add(
                        i,
                        0,
                        translateItem(category.categoryIcon, player.locale()),
                        (p, clickType) -> {
                            currentCategoryContentPage = 0;
                            activeCategoryIndex = targetIndex;
                            buildPage(p);
                            update();
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        }
                );
            }
        }

        buildCategoryIconFlipButton();
    }

    private void buildPageDivider(Player player, ItemStack divider) {
        for (int x = 0; x <= 8; x++) {
            if (x == 4) {
                add(x, 1, translateItem(categories.get(activeCategoryIndex).categoryIcon, player.locale()));
            } else {
                add(x, 1, divider);
            }
            add(x, 5, divider);
        }
    }

    private void buildCategoryContent(Player player, Category category, int page, boolean isFullSize) {
        int minX = isFullSize ? 0 : 1;
        int maxX = isFullSize ? 8 : 7;
        int categorySize = isFullSize ? 27 : 21;

        int index = page * categorySize;
        loop: for (int y = 2; y <= 4; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (index >= category.items.size())
                    break loop;

                ShopItem shopItem = category.items.get(index);
                int targetLevel = itemShopManager.getTargetLevel(player, shopItem);
                ItemStack finalShowItem = renderShopItem(player, shopItem, targetLevel);

                add(
                        x,
                        y,
                        finalShowItem,
                        (p, clickType) -> {
                            int times = switch (clickType) {
                                case LEFT -> 1;
                                case SHIFT_LEFT -> itemShopManager.getBatchTimes(shopItem, true);
                                case RIGHT -> itemShopManager.getBatchTimes(shopItem, false);
                                default -> 0;
                            };
                            if (times <= 0) return;

                            if (itemShopManager.buyItem(p, shopItem, times)) {
                                buildPage(p);
                                update();
                            }
                        }
                );
                index++;
            }
        }

        buildCategoryContentFlipButton(page, categorySize, category);
    }

    private ItemStack renderShopItem(Player player, ShopItem shopItem, int targetLevel) {
        var finalShowItem = shopItem.getShowItem(targetLevel).clone();

        List<Component> lore = finalShowItem.lore() != null
                ? new ObjectArrayList<>(finalShowItem.lore())
                : new ObjectArrayList<>();

        var rawResourceName = itemShopManager.getResourceNames().get(shopItem.coinType);
        Component resourceName = rawResourceName != null
                ? GlobalTranslator.render(rawResourceName, player.locale())
                : Component.translatable(shopItem.coinType);

        lore.addFirst(Component.translatable(
                "nextgenbedwars.shop.price",
                Component.text(shopItem.price).color(resourceName.color()),
                resourceName
        ));

        if (itemShopManager.isMaxLevel(player, shopItem)) {
            lore.add(Component.translatable("nextgenbedwars.shop.max_level"));
        } else if (itemShopManager.canAfford(player, shopItem)) {
            lore.add(Component.translatable("nextgenbedwars.shop.require_resource_enough"));
        } else {
            lore.add(Component.translatable("nextgenbedwars.shop.require_resource_missing", resourceName));
        }

        finalShowItem.lore(lore);
        return translateItem(finalShowItem, player.locale());
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
            add(8, 1, new ItemStack(Material.ARROW), (p, clickType) -> {
                currentCategoryIconPage++;
                buildPage(p);
                update();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            });
            return;
        }

        if (currentCategoryIconPage < (categories.size() - 1) / 9) {
            add(0, 1, new ItemStack(Material.ARROW), (p, clickType) -> {
                currentCategoryIconPage--;
                buildPage(p);
                update();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            });
            add(8, 1, new ItemStack(Material.ARROW), (p, clickType) -> {
                currentCategoryIconPage++;
                buildPage(p);
                update();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            });
            return;
        }

        add(0, 1, new ItemStack(Material.ARROW), (p, clickType) -> {
            currentCategoryIconPage--;
            buildPage(p);
            update();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        });
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
            add(8, 5, new ItemStack(Material.ARROW), (p, clickType) -> {
                currentCategoryContentPage++;
                buildPage(p);
                update();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            });
            return;
        }

        if (page < (category.items.size() - 1) / categorySize) {
            add(0, 5, new ItemStack(Material.ARROW), (p, clickType) -> {
                currentCategoryContentPage--;
                buildPage(p);
                update();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            });
            add(8, 5, new ItemStack(Material.ARROW), (p, clickType) -> {
                currentCategoryContentPage++;
                buildPage(p);
                update();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            });
            return;
        }

        add(0, 5, new ItemStack(Material.ARROW), (p, clickType) -> {
            currentCategoryContentPage--;
            buildPage(p);
            update();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        });
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

    private ItemStack translateItem(ItemStack item, Locale locale) {
        ItemStack copy = item.clone();
        copy.editMeta(meta -> {
            if (meta.hasItemName()) {
                meta.itemName();
                meta.itemName(
                        GlobalTranslator.render(meta.itemName(), locale)
                );
            }
            if (meta.hasLore() && meta.lore() != null) {
                meta.lore(meta.lore().stream()
                        .map(line -> GlobalTranslator.render(line, locale)
                                .applyFallbackStyle(DEFAULT_LORE_STYLE))
                        .toList());
            }
        });
        return copy;
    }
}