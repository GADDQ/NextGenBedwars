package top.earthstudio.nextgenbedwars.api.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 负责纯色彩与染料相关的工具类。
 * <ul>
 *     <li>Bukkit {@link Color} ↔ 16 进制字符串 (#RRGGBB) 互转</li>
 *     <li>解析 {@link DyeColor} 枚举</li>
 *     <li>Bukkit {@link Color} 转换为 Adventure {@link TextColor} 与最接近的 {@link NamedTextColor}</li>
 * </ul>
 */
@SuppressWarnings({"unused", "UnstableApiUsage"})
public final class ColorUtil {
    private static final Set<String> COLORABLE_BASE_SUFFIXES = Set.of(
            "WOOL",
            "TERRACOTTA",
            "STAINED_GLASS",
            "STAINED_GLASS_PANE",
            "CONCRETE",
            "CONCRETE_POWDER",
            "CARPET",
            "GLAZED_TERRACOTTA",
            "SHULKER_BOX",
            "BED",
            "CANDLE",
            "BANNER"
    );

    private ColorUtil() {}

    /**
     * 将十六进制字符串（如 "#FF00AA" 或 "FF00AA"）或标准颜色名解析为 Bukkit {@link Color}。
     *
     * @param input 颜色字符串
     * @param defaultColor 解析失败时的默认回退颜色
     * @return 解析出的 Color 对象
     */
    @NotNull
    public static Color parseColor(@Nullable String input, @NotNull Color defaultColor) {
        if (input == null || input.isEmpty()) {
            return defaultColor;
        }

        String hex = input.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        try {
            return Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            try {
                return DyeColor.valueOf(hex.toUpperCase()).getColor();
            } catch (IllegalArgumentException ignored) {
                return defaultColor;
            }
        }
    }

    /**
     * 将十六进制字符串解析为 Bukkit {@link Color}，失败默认返回白色。
     */
    @NotNull
    public static Color parseColor(@Nullable String input) {
        return parseColor(input, Color.WHITE);
    }

    /**
     * 将 Bukkit {@link Color} 对象转换为标准 "#RRGGBB" 格式的十六进制字符串。
     *
     * @param color Bukkit Color 对象
     * @return 十六进制字符串，如 "#FF00AA"
     */
    @NotNull
    public static String colorToHex(@NotNull Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * 安全解析 {@link DyeColor} 枚举。
     *
     * @param name 颜色名称
     * @return 对应的 DyeColor，若无效则返回 null
     */
    @Nullable
    public static DyeColor parseDyeColor(@Nullable String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return DyeColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 将 Bukkit {@link Color} 转换为 Adventure {@link TextColor}。
     *
     * @param color Bukkit Color
     * @return Adventure TextColor
     */
    @NotNull
    public static TextColor toTextColor(@NotNull Color color) {
        return TextColor.color(color.asRGB());
    }

    /**
     * 将 Bukkit {@link Color} 映射为最匹配的 Adventure 原版 16 色 {@link NamedTextColor}。
     *
     * @param color Bukkit Color
     * @return 最相近的 NamedTextColor（适用于 Scoreboard Team color、Nametag 等）
     */
    @NotNull
    public static NamedTextColor toNamedTextColor(@NotNull Color color) {
        return NamedTextColor.nearestTo(toTextColor(color));
    }

    @Nullable
    public static NamedTextColor toExactNamedTextColor(@NotNull Color color) {
        return NamedTextColor.namedColor(color.asRGB());
    }

    @Nullable
    public static String toNamedTextColorName(@NotNull Color color) {
        NamedTextColor named = NamedTextColor.namedColor(color.asRGB());
        return named != null ? NamedTextColor.NAMES.key(named) : null;
    }

    public static boolean isRgbDyeable(Material material) {
        return Tag.ITEMS_DYEABLE.isTagged(material);
    }

    public static boolean isColorableVariant(Material material) {
        return extractColorableSuffix(material) != null;
    }

    public static boolean isDyeable(Material material) {
        return isRgbDyeable(material) || isColorableVariant(material);
    }

    public static ItemStack dyeItem(ItemStack item, Color rgbColor) {
        Material type = item.getType();
        if (isRgbDyeable(type)) {
            ItemStack copy = item.clone();
            copy.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(rgbColor));
            return copy;
        }
        String suffix = extractColorableSuffix(type);
        if (suffix != null) {
            Material colored = Material.matchMaterial(toNearestDyeColor(rgbColor) + "_" + suffix);
            if (colored != null) {
                return item.withType(colored);
            }
        }
        return item;
    }

    private static String extractColorableSuffix(Material material) {
        if (!material.isItem()) {
            return null;
        }
        String name = material.name();
        if (name.equals("GLASS")) {
            return "STAINED_GLASS";
        }
        if (name.equals("GLASS_PANE")) {
            return "STAINED_GLASS_PANE";
        }
        if (COLORABLE_BASE_SUFFIXES.contains(name)) {
            return name;
        }
        for (DyeColor color : DyeColor.values()) {
            String prefix = color.name() + "_";
            if (name.startsWith(prefix)) {
                String suffix = name.substring(prefix.length());
                if (COLORABLE_BASE_SUFFIXES.contains(suffix)) {
                    return suffix;
                }
            }
        }
        return null;
    }

    @NotNull
    public static DyeColor toNearestDyeColor(@Nullable Color color) {
        return toNearestDyeColor(color, DyeColor.WHITE);
    }

    @NotNull
    public static DyeColor toNearestDyeColor(@Nullable Color color, @NotNull DyeColor defaultDyeColor) {
        if (color == null) {
            return defaultDyeColor;
        }

        DyeColor exact = DyeColor.getByColor(color);
        if (exact != null) {
            return exact;
        }

        DyeColor closest = defaultDyeColor;
        int minDistance = Integer.MAX_VALUE;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        for (DyeColor dyeColor : DyeColor.values()) {
            Color target = dyeColor.getColor();
            int dr = target.getRed() - r;
            int dg = target.getGreen() - g;
            int db = target.getBlue() - b;
            int distance = dr * dr + dg * dg + db * db;
            if (distance < minDistance) {
                minDistance = distance;
                closest = dyeColor;
            }
        }
        return closest;
    }

    @NotNull
    public static Color toColor(@Nullable DyeColor dyeColor) {
        return toColor(dyeColor, Color.WHITE);
    }

    @NotNull
    public static Color toColor(@Nullable DyeColor dyeColor, @NotNull Color defaultColor) {
        return dyeColor != null ? dyeColor.getColor() : defaultColor;
    }
}