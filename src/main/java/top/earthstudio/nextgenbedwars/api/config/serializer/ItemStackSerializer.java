package top.earthstudio.nextgenbedwars.api.config.serializer;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

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
                    Enchantment ench = Registry.ENCHANTMENT.get(key);
                    if (ench != null) {
                        item.addUnsafeEnchantment(ench, level);
                    }
                }
            }
        }

        if (node.node("unbreakable").getBoolean(false)) {
            item.editMeta(meta -> meta.setUnbreakable(true));
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