package top.earthstudio.nextgenbedwars.api.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Color;
import org.bukkit.DyeColor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 负责纯色彩与染料相关的工具类。
 * <ul>
 *     <li>Bukkit {@link Color} ↔ 16 进制字符串 (#RRGGBB) 互转</li>
 *     <li>解析 {@link DyeColor} 枚举</li>
 *     <li>Bukkit {@link Color} 转换为 Adventure {@link TextColor} 与最接近的 {@link NamedTextColor}</li>
 * </ul>
 */
@SuppressWarnings("unused")
public final class ColorUtil {

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
}