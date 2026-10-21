package top.earthstudio.nextgenbedwars.core.spawner;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.bukkit.plugin.java.JavaPlugin;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.config.ConfigurateHelper;
import top.earthstudio.nextgenbedwars.api.config.IConfig;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;
import top.earthstudio.nextgenbedwars.api.spawner.SpawnerType;
import top.earthstudio.nextgenbedwars.api.util.GameFolder;

import java.io.File;
import java.util.List;

public class SpawnerConfig implements IConfig<Spawner> {
    private final File spawnersFile;
    private final YamlConfigurationLoader spawnersLoader;
    private final List<Spawner> spawners;
    private final List<String> spawnerTypes;

    private CommentedConfigurationNode effectiveTypesNode;

    public SpawnerConfig(GameInstance gameInstance) {
        File gameFolder = GameFolder.of(gameInstance.getGame());
        this.spawnersFile = new File(gameFolder, "spawners.yml");
        this.spawnersLoader = ConfigurateHelper.loader(spawnersFile);
        this.spawners = new ObjectArrayList<>();
        this.spawnerTypes = new ObjectArrayList<>();

        loadEffectiveSpawnerTypes(gameFolder);

        loadFromConfig();
    }

    private void loadEffectiveSpawnerTypes(File gameFolder) {
        JavaPlugin plugin = BedwarsAPI.getInstance().plugin();
        File globalTypesFile = new File(plugin.getDataFolder(), "spawner_types.yml");

        if (!globalTypesFile.exists()) {
            plugin.saveResource("spawner_types.yml", false);
        }

        File localTypesFile = new File(gameFolder, "spawner_types.yml");

        try {
            CommentedConfigurationNode globalNode = ConfigurateHelper.loader(globalTypesFile).load();
            if (localTypesFile.exists()) {
                CommentedConfigurationNode localNode = ConfigurateHelper.loader(localTypesFile).load();
                this.effectiveTypesNode = globalNode.copy().mergeFrom(localNode);
            } else {
                this.effectiveTypesNode = globalNode;
            }
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load spawner types configuration!", e);
        }
    }

    private void loadFromConfig() {
        if (!spawnersFile.exists()) return;

        try {
            CommentedConfigurationNode root = spawnersLoader.load();
            ConfigurationNode spawnersNode = root.node("spawners");

            if (spawnersNode.virtual() || !spawnersNode.isList()) {
                return;
            }

            for (ConfigurationNode spawnerNode : spawnersNode.childrenList()) {
                String typeName = spawnerNode.node("type").getString();
                if (typeName == null || typeName.isBlank()) {
                    throw new IllegalStateException("Missing 'type' field in spawners.yml node: " + spawnerNode);
                }

                spawnerTypes.add(typeName);

                CommentedConfigurationNode baseTypeNode = effectiveTypesNode.node(typeName);

                if (baseTypeNode.virtual()) {
                    throw new IllegalStateException("Unknown spawner type '" + typeName + "' defined in spawners.yml!");
                }

                CommentedConfigurationNode mergedTypeNode = baseTypeNode.copy();
                mergedTypeNode.mergeFrom(spawnerNode);

                SpawnerType effectiveType = mergedTypeNode.get(SpawnerType.class);

                Spawner spawner = spawnerNode.get(Spawner.class);
                if (spawner != null) {
                    spawner.type = typeName;
                    spawner.spawnerType = effectiveType;
                    spawners.add(spawner);
                }
            }
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load spawners.yml from " + spawnersFile.getAbsolutePath(), e);
        }
    }

    @Override
    public List<Spawner> getValues() {
        return spawners;
    }

    public List<String> getTypes() { return spawnerTypes; }

    @Override
    public void save(List<Spawner> values) {
        try {
            CommentedConfigurationNode root = spawnersLoader.load();
            ConfigurationNode spawnersNode = root.node("spawners");

            spawnersNode.raw(null);

            for (Spawner spawner : values) {
                ConfigurationNode node = spawnersNode.appendListNode();
                node.set(Spawner.class, spawner);
                node.node("type").set(spawner.type != null ? spawner.type : "iron");
            }

            spawnersLoader.save(root);

            spawners.clear();
            spawners.addAll(values);
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to save spawners.yml to " + spawnersFile.getAbsolutePath(), e);
        }
    }

    @Override
    public void release() {
        spawners.clear();
        effectiveTypesNode = null;
    }
}