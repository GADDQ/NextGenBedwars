package top.earthstudio.nextgenbedwars.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责字符串、Legacy 格式与 Adventure Component 之间双向解析与排版的文本工具类。
 * <ul>
 *     <li>String -> TextComponent: 混合兼容解析传统颜色代码 (& / §, 包含十六进制) 与新版 MiniMessage 标签</li>
 *     <li>Component -> String: 完整序列化为规范的 MiniMessage 格式字符串</li>
 *     <li>支持 MiniMessage TagResolver / 占位符解析，为 i18n 铺路</li>
 * </ul>
 */
@SuppressWarnings("unused")
public final class TextUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Pattern BUNGEE_HEX_PATTERN = Pattern.compile(
            "[§&]x[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])"
    );

    private static final Pattern MODERN_HEX_PATTERN = Pattern.compile("[§&]#([0-9a-fA-F]{6})");

    private static final Pattern LEGACY_PATTERN = Pattern.compile("[§&]([0-9a-fk-orA-FK-OR])");

    private TextUtil() {}

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
     * 解析字符串为 {@link Component} 的通用快捷方法。
     */
    @NotNull
    public static Component parse(@Nullable String input) {
        return toTextComponent(input);
    }

    /**
     * 解析字符串为 {@link Component}，并注入 MiniMessage {@link TagResolver}（用于占位符、i18n 变量替换）。
     *
     * @param input 待解析的文本（支持 Legacy 混合格式与 MiniMessage）
     * @param tagResolvers 动态占位符解析器
     * @return 解析后的 Component
     */
    @NotNull
    public static Component parse(@Nullable String input, @NotNull TagResolver... tagResolvers) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        String miniMessageFormatted = legacyToMiniMessage(input);
        return MINI_MESSAGE.deserialize(miniMessageFormatted, tagResolvers);
    }

    /**
     * 将 {@link Component} 序列化为 MiniMessage 格式的 String。
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

        if (text.indexOf('&') == -1 && text.indexOf('§') == -1) {
            return text;
        }

        Matcher bungeeMatcher = BUNGEE_HEX_PATTERN.matcher(text);
        if (bungeeMatcher.find()) {
            text = bungeeMatcher.replaceAll("<reset><#$1$2$3$4$5$6>");
        }

        Matcher hexMatcher = MODERN_HEX_PATTERN.matcher(text);
        if (hexMatcher.find()) {
            text = hexMatcher.replaceAll("<reset><#$1>");
        }

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
}