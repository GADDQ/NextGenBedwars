package top.earthstudio.nextgenbedwars.api.util;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.joml.Vector3i;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class ConfigUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final Pattern HEX_LEGACY_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ConfigUtil() {}

    // ================= Basic =================

    public static String parseString(Map<?, ?> map, String key, String tag) {
        return parseString(map, key, tag, "");
    }

    public static String parseString(Map<?, ?> map, String key, String tag, String defaultValue) {
        if (map == null) return defaultValue;
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    public static int parseInt(Map<?, ?> map, String key, String tag) {
        return parseInt(map, key, tag, 0);
    }

    public static int parseInt(Map<?, ?> map, String key, String tag, int defaultValue) {
        if (map == null) return defaultValue;
        Object val = map.get(key);
        if (val instanceof Number number) return number.intValue();
        if (val != null) {
            try {
                return Integer.parseInt(val.toString());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static double parseDouble(Map<?, ?> map, String key, String tag) {
        return parseDouble(map, key, tag, 0.0);
    }

    public static double parseDouble(Map<?, ?> map, String key, String tag, double defaultValue) {
        if (map == null) return defaultValue;
        Object val = map.get(key);
        if (val instanceof Number number) return number.doubleValue();
        if (val != null) {
            try {
                return Double.parseDouble(val.toString());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    // ================= Component =================

    public static Component parseComponent(Object raw, String tag) {
        if (raw == null) return Component.empty();
        String s = raw.toString();
        if (s.isEmpty()) return Component.empty();

        if (s.indexOf('&') == -1) {
            return MINI_MESSAGE.deserialize(s);
        }

        String converted = convertLegacyToMiniMessage(s);
        return MINI_MESSAGE.deserialize(converted);
    }

    public static String buildComponent(Component component) {
        if (component == null) return "";
        return MINI_MESSAGE.serialize(component);
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
                .replace("&L", "<bold>")
                .replace("&M", "<strikethrough>")
                .replace("&N", "<underlined>")
                .replace("&O", "<italic>")
                .replace("&R", "<reset>");
    }

    // ================= Color =================

    public static Color parseColor(Object raw, String tag) {
        if (raw == null) return Color.WHITE;
        String string = raw.toString().trim().replace('§', '&');
        if (string.isEmpty()) return Color.WHITE;

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
        } catch (NumberFormatException ignored) {
            return Color.WHITE;
        }
    }

    public static String buildColor(Color color) {
        if (color == null) return "#FFFFFF";
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    // ================= Location =================

    public static Location parseLocation(Object raw, String tag) {
        Map<?, ?> map = asMap(raw);
        if (map == null) {
            return new Location(null, 0.0, 0.0, 0.0, 0f, 0f);
        }

        double x = parseDouble(map, "x", tag, 0.0);
        double y = parseDouble(map, "y", tag, 0.0);
        double z = parseDouble(map, "z", tag, 0.0);
        float yaw = map.containsKey("yaw") ? (float) parseDouble(map, "yaw", tag, 0.0) : 0f;
        float pitch = map.containsKey("pitch") ? (float) parseDouble(map, "pitch", tag, 0.0) : 0f;

        return new Location(null, x, y, z, yaw, pitch);
    }

    public static Map<String, Object> buildLocation(Location loc) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (loc == null) {
            map.put("x", 0.0);
            map.put("y", 0.0);
            map.put("z", 0.0);
            return map;
        }

        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());
        if (loc.getYaw() != 0f) map.put("yaw", (double) loc.getYaw());
        if (loc.getPitch() != 0f) map.put("pitch", (double) loc.getPitch());
        return map;
    }

    // ================= Material =================

    public static Material parseMaterial(Object raw, String tag) {
        if (raw == null) return Material.AIR;
        Material material = Material.matchMaterial(raw.toString());
        return material != null ? material : Material.AIR;
    }

    public static String buildMaterial(Material material) {
        return material != null ? material.name() : "AIR";
    }

    // ================= Entity =================

    public static EntityType parseEntityType(Object raw, String tag) {
        return parseEntityType(raw, tag, EntityType.VILLAGER);
    }

    public static EntityType parseEntityType(Object raw, String tag, EntityType def) {
        if (raw == null) return def;
        try {
            return EntityType.valueOf(raw.toString().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return def;
        }
    }

    public static String buildEntityType(EntityType entityType) {
        return entityType != null ? entityType.name() : "VILLAGER";
    }

    // ================= ItemStack =================

    public static ItemStack parseItemStack(Object raw, String tag, String field) {
        String itemTag = tag + " field '" + field + "'";
        Map<?, ?> map = asMap(raw);
        if (map == null) {
            return new ItemStack(Material.AIR);
        }

        Material material = parseMaterial(map.get("material"), itemTag + ".material");
        int amount = parseInt(map, "amount", itemTag + ".amount", 1);
        if (amount <= 0) amount = 1;

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

    public static Map<String, Object> buildItemStack(ItemStack item) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (item == null || item.getType().isAir()) {
            map.put("material", "AIR");
            return map;
        }

        map.put("material", item.getType().name());
        if (item.getAmount() > 1) {
            map.put("amount", item.getAmount());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName() && meta.displayName() != null) {
                map.put("name", buildComponent(meta.displayName()));
            }
            if (meta.hasLore() && meta.lore() != null) {
                map.put("lore", meta.lore().stream().map(ConfigUtil::buildComponent).toList());
            }
        }
        return map;
    }

    // ================= Vector =================

    public static Vector3i parseVector3i(Object raw, String tag) {
        Map<?, ?> map = asMap(raw);
        if (map == null) {
            return new Vector3i(0, 0, 0);
        }
        return new Vector3i(
                parseInt(map, "x", tag, 0),
                parseInt(map, "y", tag, 0),
                parseInt(map, "z", tag, 0)
        );
    }

    public static Map<String, Object> buildVector3i(Vector3i vec) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (vec == null) {
            map.put("x", 0);
            map.put("y", 0);
            map.put("z", 0);
            return map;
        }
        map.put("x", vec.x);
        map.put("y", vec.y);
        map.put("z", vec.z);
        return map;
    }

    // ================= Region =================

    public static ObjectObjectImmutablePair<Vector3i, Vector3i> parseRegion(Object raw, String tag) {
        Map<?, ?> map = asMap(raw);
        if (map == null) {
            return new ObjectObjectImmutablePair<>(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0));
        }
        return new ObjectObjectImmutablePair<>(
                parseVector3i(map.get("min"), tag + ".min"),
                parseVector3i(map.get("max"), tag + ".max")
        );
    }

    public static Map<String, Object> buildRegion(Pair<Vector3i, Vector3i> region) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (region == null) return map;
        map.put("min", buildVector3i(region.left()));
        map.put("max", buildVector3i(region.right()));
        return map;
    }

    private static Map<?, ?> asMap(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            return map;
        }
        if (raw instanceof ConfigurationSection section) {
            return section.getValues(false);
        }
        return null;
    }
}