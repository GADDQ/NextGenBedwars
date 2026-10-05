package top.earthstudio.nextgenbedwars.api.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public final class ConfigManager {
    static private JavaPlugin plugin;
    static private Map<String, CacheEntry> cache;

    private ConfigManager() {}

    static public void initialize() {
        plugin = BedwarsAPI.getInstance().getPlugin();
        cache = new Object2ObjectOpenHashMap<>();
    }

    static public void bootstrap(Config config) {
        reloadFromFile(config);
    }

    /**
     * 写入内存并立即持久化落盘。
     * 强契约：value 不可为 null —— 要清除 key 请用 {@link #remove}。
     */
    static public void write(Config config, String key, Object value) {
        Objects.requireNonNull(value, "value");
        YamlConfiguration yaml = getOrLoadYaml(config);
        yaml.set(key, value);
        save(config);
    }

    /**
     * 移除本层的 key（等价于恢复 fallback 语义），并落盘。
     */
    static public void remove(Config config, String key) {
        YamlConfiguration yaml = getOrLoadYaml(config);
        yaml.set(key, null);
        save(config);
    }

    static public Object read(Config config, String key) {
        return getOrLoadYaml(config).get(key);
    }

    static public void reloadFromFile(Config config) {
        File file = getConfigFile(config);
        if (!file.exists()) {
            ensureFileExists(config, file);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        cache.put(idOf(config), new CacheEntry(yaml, file));
    }

    static public void save(Config config) {
        String id = idOf(config);
        CacheEntry entry = cache.get(id);

        if (entry == null)
            throw new IllegalStateException("Config not loaded into cache: " + id);

        try {
            entry.file.getParentFile().mkdirs();
            entry.yaml.save(entry.file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config: " + entry.file.getName(), e);
        }
    }

    static public void release(Config config) {
        cache.remove(idOf(config));
    }

    static public void shutdown() {
        cache.clear();
        cache = null;
        plugin = null;
    }

    // ================= 内部物理定位与自动释放 =================

    static private YamlConfiguration getOrLoadYaml(Config config) {
        String id = idOf(config);
        CacheEntry entry = cache.get(id);
        if (entry == null) {
            reloadFromFile(config);
            entry = cache.get(id);
        }
        return entry.yaml;
    }

    /** 身份 = 规范化物理路径。null parentFolder 会被解析为 plugin.getDataFolder()。 */
    static private String idOf(Config config) {
        return getConfigFile(config).getAbsolutePath();
    }

    static private File getConfigFile(Config config) {
        File baseDir = config.parentFolder != null ? config.parentFolder : plugin.getDataFolder();
        return new File(baseDir, config.name);
    }

    static private void ensureFileExists(Config config, File file) {
        file.getParentFile().mkdirs();

        // 如果是插件根目录下的文件，尝试从 Jar 包 res 里原汁原味掏出来！
        if (config.parentFolder == null && plugin.getResource(config.name) != null) {
            plugin.saveResource(config.name, false);
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create file: " + file.getAbsolutePath(), e);
            }
        }
    }

    static private final class CacheEntry {
        final YamlConfiguration yaml;
        final File file;

        CacheEntry(YamlConfiguration yaml, File file) {
            this.yaml = yaml;
            this.file = file;
        }
    }
}