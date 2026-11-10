package top.earthstudio.nextgenbedwars.api.config.serializer;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import top.earthstudio.nextgenbedwars.api.util.ColorUtil;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {

    @Override
    public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.virtual()) {
            return new ItemStack(Material.AIR);
        }

        String matStr = node.node("material").getString("AIR");
        Material material = Material.matchMaterial(matStr);
        if (material == null || material.isAir()) {
            return new ItemStack(Material.AIR);
        }

        int amount = node.node("amount").getInt(1);
        if (amount <= 0) amount = 1;

        ItemStack item = new ItemStack(material, amount);

        Component name = node.node("name").get(Component.class);
        List<Component> lore = node.node("lore").getList(Component.class);

        if (name != null || (lore != null && !lore.isEmpty())) {
            item.editMeta(meta -> {
                if (name != null) meta.displayName(name);
                if (lore != null && !lore.isEmpty()) meta.lore(lore);
            });
        }

        ConfigurationNode enchantsNode = node.node("enchantments");
        if (!enchantsNode.virtual()) {
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : enchantsNode.childrenMap().entrySet()) {
                String enchantName = entry.getKey().toString().toLowerCase();
                int level = entry.getValue().getInt(1);

                NamespacedKey key = enchantName.contains(":")
                        ? NamespacedKey.fromString(enchantName)
                        : NamespacedKey.minecraft(enchantName);

                if (key != null) {
                    Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
                    if (enchantment != null) {
                        item.addUnsafeEnchantment(enchantment, level);
                    }
                }
            }
        }

        if (node.node("unbreakable").getBoolean(false)) {
            item.editMeta(meta -> meta.setUnbreakable(true));
        }

        if (node.node("customModelData").getInt(0) != 0) {
            int cmd = node.node("customModelData").getInt();
            item.editMeta(meta -> meta.setCustomModelData(cmd));
        }

        if (item.getItemMeta() instanceof PotionMeta) {
            ConfigurationNode potionNode = node.node("potion");
            if (!potionNode.virtual()) {
                item.editMeta(PotionMeta.class, potionMeta -> {
                    String colorStr = potionNode.node("color").getString();
                    if (colorStr != null && !colorStr.isEmpty()) {
                        potionMeta.setColor(ColorUtil.parseColor(colorStr));
                    }

                    String baseTypeStr = potionNode.node("type").getString();
                    if (baseTypeStr != null && !baseTypeStr.isEmpty()) {
                        NamespacedKey typeKey = baseTypeStr.contains(":")
                                ? NamespacedKey.fromString(baseTypeStr)
                                : NamespacedKey.minecraft(baseTypeStr.toLowerCase());
                        if (typeKey != null) {
                            PotionType baseType = RegistryAccess.registryAccess()
                                    .getRegistry(RegistryKey.POTION)
                                    .get(typeKey);
                            if (baseType != null) {
                                potionMeta.setBasePotionType(baseType);
                            }
                        }
                    }

                    ConfigurationNode effectsNode = potionNode.node("effects");
                    if (!effectsNode.virtual()) {
                        for (Map.Entry<Object, ? extends ConfigurationNode> entry : effectsNode.childrenMap().entrySet()) {
                            String effectName = entry.getKey().toString().toLowerCase();
                            ConfigurationNode effectNode = entry.getValue();

                            NamespacedKey effectKey = effectName.contains(":")
                                    ? NamespacedKey.fromString(effectName)
                                    : NamespacedKey.minecraft(effectName);

                            if (effectKey != null) {
                                PotionEffectType effectType = RegistryAccess.registryAccess()
                                        .getRegistry(RegistryKey.MOB_EFFECT)
                                        .get(effectKey);

                                if (effectType == null) {
                                    effectType = PotionEffectType.getByKey(effectKey);
                                }

                                if (effectType != null) {
                                    int duration = effectNode.node("duration").getInt(600); // 默认 tick
                                    int amplifier = effectNode.node("amplifier").getInt(0); // 0 = I级, 1 = II级
                                    boolean ambient = effectNode.node("ambient").getBoolean(false);
                                    boolean particles = effectNode.node("particles").getBoolean(true);
                                    boolean icon = effectNode.node("icon").getBoolean(true);

                                    potionMeta.addCustomEffect(new PotionEffect(
                                            effectType, duration, amplifier, ambient, particles, icon
                                    ), true);
                                }
                            }
                        }
                    }
                });
            }
        }

        if (item.getItemMeta() instanceof LeatherArmorMeta) {
            String colorStr = node.node("color").getString();
            if (colorStr != null && !colorStr.isEmpty()) {
                item.editMeta(LeatherArmorMeta.class, leatherMeta ->
                        leatherMeta.setColor(ColorUtil.parseColor(colorStr))
                );
            }
        }

        return item;
    }

    @Override
    public void serialize(Type type, ItemStack item, ConfigurationNode node) throws SerializationException {
        if (item == null || item.getType().isAir()) {
            node.raw(null);
            return;
        }

        node.node("material").set(item.getType().name());
        if (item.getAmount() > 1) {
            node.node("amount").set(item.getAmount());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                node.node("name").set(Component.class, meta.displayName());
            }

            if (meta.hasLore()) {
                node.node("lore").setList(Component.class, meta.lore());
            }

            if (meta.isUnbreakable()) {
                node.node("unbreakable").set(true);
            }

            if (meta.hasCustomModelData()) {
                node.node("customModelData").set(meta.getCustomModelData());
            }

            if (meta instanceof PotionMeta potionMeta) {
                ConfigurationNode potionNode = node.node("potion");
                if (potionMeta.hasColor()) {
                    potionNode.node("color").set(ColorUtil.colorToHex(potionMeta.getColor()));
                }
                if (potionMeta.getBasePotionType() != null) {
                    potionNode.node("type").set(potionMeta.getBasePotionType().getKey().getKey());
                }
                if (potionMeta.hasCustomEffects()) {
                    ConfigurationNode effectsNode = potionNode.node("effects");
                    for (PotionEffect effect : potionMeta.getCustomEffects()) {
                        String effectKey = effect.getType().getKey().getKey();
                        ConfigurationNode effectNode = effectsNode.node(effectKey);
                        effectNode.node("duration").set(effect.getDuration());
                        effectNode.node("amplifier").set(effect.getAmplifier());
                    }
                }
            }

            if (meta instanceof LeatherArmorMeta leatherMeta) {
                node.node("color").set(ColorUtil.colorToHex(leatherMeta.getColor()));
            }
        }

        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            ConfigurationNode enchantsNode = node.node("enchantments");
            for (Map.Entry<Enchantment, Integer> entry : item.getItemMeta().getEnchants().entrySet()) {
                String enchantKey = entry.getKey().getKey().getKey();
                enchantsNode.node(enchantKey).set(entry.getValue());
            }
        }
    }
}