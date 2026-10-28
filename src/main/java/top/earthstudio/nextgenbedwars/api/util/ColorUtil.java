package top.earthstudio.nextgenbedwars.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责字符串与 TextComponent 之间双向解析的颜色工具类。
 * <ul>
 *     <li>String -> TextComponent: 混合兼容解析传统颜色代码 (& / §, 包含十六进制) 与新版 MiniMessage 标签</li>
 *     <li>TextComponent -> String: 完整序列化为规范的 MiniMessage 格式字符串</li>
 *     <li>Color ↔ String: Bukkit Color 与标准 #RRGGBB 十六进制字符串互转</li>
 * </ul>
 */
public final class ColorUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    // 匹配 Bungee 风格十六进制: &x&r&r&g&g&b&b 或 §x§r§r§g§g§b§b
    private static final Pattern BUNGEE_HEX_PATTERN = Pattern.compile(
            "[§&]x[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])"
    );

    // 匹配现代十六进制: &#RRGGBB 或 §#RRGGBB
    private static final Pattern MODERN_HEX_PATTERN = Pattern.compile("[§&]#([0-9a-fA-F]{6})");

    // 匹配传统单字符颜色与样式代码: &0-&f, &k-&r, §0-§f, §k-§r
    private static final Pattern LEGACY_PATTERN = Pattern.compile("[§&]([0-9a-fk-orA-FK-OR])");

    private ColorUtil() {}

    /**
     * 将混合了传统颜色代码 (& / §) 与 MiniMessage 标签的字符串解析为 {@link TextComponent}。
     *
     * @param input 包含颜色或格式标记的文本（支持 &a, &#ff0000, <red>, <gradient:...> 等混合格式）
     * @return 解析后的 TextComponent（若 input 为空则返回 {@link Component#empty()}）
     */
    @NotNull
    public static TextComponent toTextComponent(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        String miniMessageFormatted = legacyToMiniMessage(input);
        Component component = MINI_MESSAGE.deserialize(miniMessageFormatted);

        if (component instanceof TextComponent tc) {
            return tc;
        }
        return Component.text().append(component).build();
    }

    /**
     * 将 {@link Component}（包括 TextComponent）序列化为 MiniMessage 格式的 String。
     *
     * @param component 待序列化的 Component
     * @return MiniMessage 格式的字符串（若 component 为 null 则返回空字符串）
     */
    @NotNull
    public static String toMiniMessage(@Nullable Component component) {
        if (component == null) {
            return "";
        }
        return MINI_MESSAGE.serialize(component);
    }

    /**
     * 将文本中的老版传统颜色代码 (& / §, 支持六位十六进制及 Bungee 格式) 统一预处理为 MiniMessage 标签。
     * 已有的 MiniMessage 标签保持不变。
     *
     * @param text 待转换的文本
     * @return 纯 MiniMessage 格式的文本
     */
    @NotNull
    public static String legacyToMiniMessage(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 快速短路检查：如果不含 & 和 §，无需任何正则匹配，零分配直接返回
        if (text.indexOf('&') == -1 && text.indexOf('§') == -1) {
            return text;
        }

        // 1. 处理 Bungee 风格十六进制: &x&r&r&g&g&b&b -> <reset><#rrggbb>
        Matcher bungeeMatcher = BUNGEE_HEX_PATTERN.matcher(text);
        if (bungeeMatcher.find()) {
            text = bungeeMatcher.replaceAll("<reset><#$1$2$3$4$5$6>");
        }

        // 2. 处理现代十六进制: &#rrggbb -> <reset><#rrggbb>
        Matcher hexMatcher = MODERN_HEX_PATTERN.matcher(text);
        if (hexMatcher.find()) {
            text = hexMatcher.replaceAll("<reset><#$1>");
        }

        // 3. 处理标准单字符代码 (&a, &l, &r 等)
        Matcher legacyMatcher = LEGACY_PATTERN.matcher(text);
        if (!legacyMatcher.find()) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text.length() + 32);
        do {
            char code = legacyMatcher.group(1).charAt(0);
            legacyMatcher.appendReplacement(sb, mapLegacyCode(code));
        } while (legacyMatcher.find());
        legacyMatcher.appendTail(sb);

        return sb.toString();
    }

    private static String mapLegacyCode(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> "<reset><black>";
            case '1' -> "<reset><dark_blue>";
            case '2' -> "<reset><dark_green>";
            case '3' -> "<reset><dark_aqua>";
            case '4' -> "<reset><dark_red>";
            case '5' -> "<reset><dark_purple>";
            case '6' -> "<reset><gold>";
            case '7' -> "<reset><gray>";
            case '8' -> "<reset><dark_gray>";
            case '9' -> "<reset><blue>";
            case 'a' -> "<reset><green>";
            case 'b' -> "<reset><aqua>";
            case 'c' -> "<reset><red>";
            case 'd' -> "<reset><light_purple>";
            case 'e' -> "<reset><yellow>";
            case 'f' -> "<reset><white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> "";
        };
    }

    /**
     * 将十六进制字符串（如 "#FF00AA" 或 "FF00AA"）或标准颜色名解析为 Bukkit {@link org.bukkit.Color}。
     *
     * @param input 颜色字符串
     * @param defaultColor 解析失败时的默认回退颜色
     * @return 解析出的 Color 对象
     */
    @NotNull
    public static org.bukkit.Color parseColor(@Nullable String input, @NotNull org.bukkit.Color defaultColor) {
        if (input == null || input.isEmpty()) {
            return defaultColor;
        }

        String hex = input.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        try {
            return org.bukkit.Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            try {
                return org.bukkit.DyeColor.valueOf(hex.toUpperCase()).getColor();
            } catch (IllegalArgumentException ignored) {
                return defaultColor;
            }
        }
    }

    /**
     * 将 Bukkit {@link org.bukkit.Color} 对象转换为标准 "#RRGGBB" 格式的十六进制字符串。
     *
     * @param color Bukkit Color 对象
     * @return 十六进制字符串，如 "#FF00AA"
     */
    @NotNull
    public static String colorToHex(@NotNull org.bukkit.Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * 安全解析 DyeColor 枚举。
     *
     * @param name 颜色名称
     * @return 对应的 DyeColor，若无效则返回 null
     */
    @Nullable
    public static org.bukkit.DyeColor parseDyeColor(@Nullable String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return org.bukkit.DyeColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}