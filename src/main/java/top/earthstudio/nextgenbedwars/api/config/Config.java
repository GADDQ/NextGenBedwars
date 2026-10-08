package top.earthstudio.nextgenbedwars.api.config;

import java.io.File;

public class Config {
    public final String name;
    public final File parentFolder;
    protected final Config fallback;   // 可以是 null

    public Config(String name) {
        this(null, name, null);
    }

    public Config(File parentFolder, String name) {
        this(parentFolder, name, null);
    }

    public Config(File parentFolder, String name, Config fallback) {
        this.parentFolder = parentFolder;
        this.name = name.endsWith(".yml") ? name : name + ".yml";
        this.fallback = fallback;

        ConfigManager.bootstrap(this);
    }

    public void write(String key, Object value) {
        ConfigManager.write(this, key, value);
    }

    /** 移除本层的 key，使 read 回落到 fallback */
    public void remove(String key) {
        ConfigManager.remove(this, key);
    }

    @SuppressWarnings("unchecked")
    public <T> T read(String key) {
        return (T) ConfigManager.read(this, key);
    }

    @SuppressWarnings("unchecked")
    public <T> T read(String key, T defaultValue) {
        T val = read(key);
        return val != null ? val : defaultValue;
    }

    /** 从 cache 中摘除该 Config 的内存副本（不删除磁盘文件），下次访问时重新 lazy 加载。 */
    public void release() {
        ConfigManager.release(this);
    }

    public void reload() { ConfigManager.reloadFromFile(this); }
    public void save()   { ConfigManager.save(this); }
}