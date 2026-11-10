package top.earthstudio.nextgenbedwars.api.config;

import io.leangen.geantyref.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class IConfig<T> {
    private final File file;
    private final YamlConfigurationLoader loader;
    private final TypeToken<T> typeToken;
    private final String nodeKey;

    private final boolean hasGlobal;

    protected IConfig(@NotNull GameInstance gameInstance, @NotNull String fileName, boolean hasGlobal) {
        this(gameInstance, fileName, hasGlobal, null);
    }

    protected IConfig(@NotNull GameInstance gameInstance, @NotNull String fileName, boolean hasGlobal, @Nullable String nodeKey) {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType parameterized) {
            Type actualType = parameterized.getActualTypeArguments()[0];
            //noinspection unchecked
            this.typeToken = (TypeToken<T>) TypeToken.get(actualType);
        } else {
            throw new IllegalArgumentException("Subclass must specify a generic type parameter: " + getClass().getName());
        }

        this.nodeKey = nodeKey;
        this.hasGlobal = hasGlobal;

        File localFile = new File(GameFolder.of(gameInstance.getGame()), fileName);
        if (hasGlobal && !localFile.exists()) {
            this.file = new File(BedwarsAPI.getInstance().plugin().getDataFolder(), fileName);
        } else {
            this.file = localFile;
        }

        this.loader = ConfigurateHelper.loader(this.file);
    }

    protected T load() {
        if (!file.exists()) return null;

        try {
            CommentedConfigurationNode root = loader.load();
            ConfigurationNode target = (nodeKey != null && !nodeKey.isBlank()) ? root.node(nodeKey) : root;
            return target.get(typeToken);
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load config: " + file.getName(), e);
        }
    }

    protected void save(T object) {
        if (object == null)
            throw new IllegalArgumentException("Saved object cannot be null!");

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }

        try {
            CommentedConfigurationNode root = loader.load();
            ConfigurationNode target = (nodeKey != null && !nodeKey.isBlank()) ? root.node(nodeKey) : root;
            target.set(typeToken, object);
            loader.save(root);
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to save config: " + file.getName(), e);
        }
    }
}