package top.earthstudio.nextgenbedwars.core.shop.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.shop.executor.ShopExecutor;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;

import static java.util.Objects.requireNonNull;

public class ItemShopAction {
    private final ShopExecutor shopExecutor;

    public ItemShopAction(ShopExecutor shopExecutor) {
        this.shopExecutor = shopExecutor;
    }

    public boolean buyItem(Player player, ShopItem shopItem, int targetLevel) { // TODO: i18n
        ItemStack cost = new ItemStack(shopItem.coinType, shopItem.price);

        if (!player.getInventory().containsAtLeast(cost, shopItem.price)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
            player.sendMessage(Component.text("货币不足！需要 " + shopItem.price + " 个 ")
                    .color(NamedTextColor.RED)
                    .append(Component.translatable(requireNonNull(shopItem.coinType.getItemTranslationKey()))));
            return false;
        }

        ItemStack toGive;
        if (shopItem.actualItems != null && !shopItem.actualItems.isEmpty())
            toGive = shopItem.getActualItem(targetLevel).clone();
        else
            toGive = new ItemStack(shopItem.getShowItem(targetLevel).getType(), shopItem.getShowItem(targetLevel).getAmount());

        int existingSlot = -1;
        if (shopItem.isUpgradeable()) {
            existingSlot = findSlotByShopItemId(player, shopItem.id);
        }

        if (existingSlot == -1 && !canFit(player, toGive, cost)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(Component.text("购买失败！背包已满！").color(NamedTextColor.RED));
            return false;
        }

        toGive.editMeta(itemMeta -> {
            var pdc = itemMeta.getPersistentDataContainer();
            pdc.set(PdcKeys.shop.itemId, PersistentDataType.STRING, shopItem.id);

            if (shopItem.isDecreaseLevelWhenPlayerDead)
                pdc.set(PdcKeys.shop.downgradeWhenDead, PersistentDataType.BOOLEAN, true);
            if (shopItem.isKeepWhenDead)
                pdc.set(PdcKeys.shop.keepWhenDead, PersistentDataType.BOOLEAN, true);
            if (!shopItem.isThrowable)
                pdc.set(PdcKeys.shop.throwable, PersistentDataType.BOOLEAN, false);
            if (shopItem.propId != null)
                pdc.set(PdcKeys.prop.id, PersistentDataType.STRING, shopItem.propId);
        });

        if (shopItem.executorId != null && !shopItem.executorId.isBlank()) {
            if (!shopExecutor.get(shopItem.executorId).execute(player, shopItem)) {
                player.sendMessage(Component.text("购买失败！").color(NamedTextColor.RED));
                return false;
            }
        }

        player.getInventory().removeItem(cost);

        if (existingSlot != -1) {
            player.getInventory().setItem(existingSlot, toGive);
        } else {
            player.getInventory().addItem(toGive);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        player.sendMessage(Component.text("购买成功！").color(NamedTextColor.GREEN)); // TODO: buy what x?
        return true;
    }

    public boolean canFit(Player player, ItemStack toAdd, ItemStack toRemove) {
        var inv = player.getInventory();
        ItemStack[] contents = inv.getStorageContents();

        ItemStack[] simulated = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            simulated[i] = contents[i] == null ? null : contents[i].clone();
        }

        if (toRemove != null && toRemove.getAmount() > 0) {
            int need = toRemove.getAmount();
            for (int i = 0; i < simulated.length && need > 0; i++) {
                ItemStack item = simulated[i];
                if (item != null && item.isSimilar(toRemove)) {
                    int take = Math.min(item.getAmount(), need);
                    item.setAmount(item.getAmount() - take);
                    need -= take;
                    if (item.getAmount() <= 0) {
                        simulated[i] = null;
                    }
                }
            }
        }

        int maxStack = toAdd.getMaxStackSize();
        int remaining = toAdd.getAmount();

        if (maxStack > 1) {
            for (ItemStack item : simulated) {
                if (item != null && item.isSimilar(toAdd)) {
                    remaining -= (maxStack - item.getAmount());
                    if (remaining <= 0) return true;
                }
            }
        }

        int emptySlots = 0;
        for (ItemStack item : simulated) {
            if (item == null) emptySlots++;
        }

        return remaining <= (long) emptySlots * maxStack;
    }

    public int findSlotByShopItemId(Player player, String itemId) {
        if (itemId == null) return -1;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            var meta = item.getItemMeta();
            if (meta == null) continue;
            String id = meta.getPersistentDataContainer().get(PdcKeys.shop.itemId, PersistentDataType.STRING);
            if (itemId.equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
