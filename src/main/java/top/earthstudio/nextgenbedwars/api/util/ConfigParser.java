package top.earthstudio.nextgenbedwars.api.util;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import org.joml.Vector3i;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigParser {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final Pattern HEX_LEGACY_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ConfigParser() {}

    public static String parseString(Map<?, ?> map, String key, String tag) {
        Object val = map.get(key);
        if (val == null) throw new IllegalStateException(tag + " missing field '" + key + "'");
        if (!(val instanceof String string)) throw new IllegalStateException(tag + " field '" + key + "' must be string, got " + val.getClass().getSimpleName());
        return string;
    }

    public static int parseInt(Map<?, ?> map, String key, String tag) {
        Object val = map.get(key);
        if (val == null) throw new IllegalStateException(tag + " missing field '" + key + "'");
        if (!(val instanceof Number number)) throw new IllegalStateException(tag + " field '" + key + "' must be number, got " + val.getClass().getSimpleName());
        return number.intValue();
    }

    public static double parseDouble(Map<?, ?> map, String key, String tag) {
        Object val = map.get(key);
        if (val == null) throw new IllegalStateException(tag + " missing field '" + key + "'");
        if (!(val instanceof Number number)) throw new IllegalStateException(tag + " field '" + key + "' must be number, got " + val.getClass().getSimpleName());
        return number.doubleValue();
    }

    public static Component parseComponent(Object raw, String tag) {
        if (raw == null) throw new IllegalStateException(tag + " missing text component");
        String s = raw.toString();

        if (s.indexOf('&') == -1) {
            return MINI_MESSAGE.deserialize(s);
        }

        String converted = convertLegacyToMiniMessage(s);
        return MINI_MESSAGE.deserialize(converted);
    }

    private static String convertLegacyToMiniMessage(String input) {
        Matcher matcher = HEX_LEGACY_PATTERN.matcher(input);
        input = matcher.replaceAll("<#$1>");

        return input
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>")

                // UPPER_CASE Support
                .replace("&A", "<green>")
                .replace("&B", "<aqua>")
                .replace("&C", "<red>")
                .replace("&D", "<light_purple>")
                .replace("&E", "<yellow>")
                .replace("&F", "<white>")
                .replace("&K", "<obfuscated>")
                .replace("&L", "<bold>").replace("&M", "<strikethrough>")
                .replace("&N", "<underlined>")
                .replace("&O", "<italic>").replace("&R", "<reset>");
    }

    public static Color parseColor(Object raw, String tag) {
        if (raw == null) throw new IllegalStateException(tag + " missing 'color'");
        String string = raw.toString().trim().replace('§', '&');

        if (string.startsWith("&") && string.length() == 2) {
            TextColor textColor = LEGACY_AMPERSAND.deserialize(string).color();
            if (textColor != null) return Color.fromRGB(textColor.value());
        }

        NamedTextColor named = NamedTextColor.NAMES.value(string.toLowerCase().replace(" ", "_"));
        if (named != null) {
            return Color.fromRGB(named.value());
        }

        String hex = string.startsWith("#") ? string.substring(1) : string;
        try {
            return Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(tag + " invalid color '" + string + "'. Supports '#RRGGBB', NamedTextColor (e.g. RED, BLUE) or '&c'");
        }
    }

    public static Location parseLocation(Object raw, String tag) {
        if (raw == null) throw new IllegalStateException(tag + " missing location");
        if (!(raw instanceof Map<?, ?> map)) throw new IllegalStateException(tag + " location must be a {x, y, z} map");

        double x = parseDouble(map, "x", tag);
        double y = parseDouble(map, "y", tag);
        double z = parseDouble(map, "z", tag);
        float yaw = map.containsKey("yaw") ? ((Number) map.get("yaw")).floatValue() : 0f;
        float pitch = map.containsKey("pitch") ? ((Number) map.get("pitch")).floatValue() : 0f;

        return new Location(null, x, y, z, yaw, pitch);
    }

    public static Material parseMaterial(Object raw, String tag) {
        if (raw == null) throw new IllegalStateException(tag + " missing material");
        Material material = Material.matchMaterial(raw.toString());
        if (material == null) throw new IllegalStateException(tag + " unknown material '" + raw + "'");
        return material;
    }

    public static EntityType parseEntityType(Object raw, String tag) {
        if (raw == null) throw new IllegalStateException(tag + " missing 'entityType'");
        try {
            return EntityType.valueOf(raw.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(tag + " unknown entityType '" + raw + "'");
        }
    }

    public static ItemStack parseItemStack(Object raw, String tag, String field) {
        String itemTag = tag + " field '" + field + "'";
        if (!(raw instanceof Map<?, ?> map)) throw new IllegalStateException(itemTag + " must be a map");

        Material material = parseMaterial(map.get("material"), itemTag + ".material");
        int amount = map.containsKey("amount") ? ((Number) map.get("amount")).intValue() : 1;

        ItemStack item = new ItemStack(material, amount);

        Object nameObj = map.get("name");
        Object loreObj = map.get("lore");

        if (nameObj != null || loreObj != null) {
            item.editMeta(meta -> {
                if (nameObj != null) {
                    meta.displayName(parseComponent(nameObj, itemTag + ".name"));
                }
                if (loreObj instanceof List<?> list) {
                    meta.lore(list.stream().map(l -> parseComponent(l, itemTag + ".lore")).toList());
                }
            });
        }
        return item;
    }

    public static Vector3i parseVector3i(Object raw, String tag) {
        if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalStateException(tag + " must be a {x, y, z} map");
        }
        return new Vector3i(
                parseInt(map, "x", tag),
                parseInt(map, "y", tag),
                parseInt(map, "z", tag)
        );
    }

    public static ObjectObjectImmutablePair<Vector3i, Vector3i> parseRegion(Object raw, String tag) {
        if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalStateException(tag + " must be a {min, max} map");
        }
        return new ObjectObjectImmutablePair<>(
                parseVector3i(map.get("min"), tag + ".min"),
                parseVector3i(map.get("max"), tag + ".max")
        );
    }
}