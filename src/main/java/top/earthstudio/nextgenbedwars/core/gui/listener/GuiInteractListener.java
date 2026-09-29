package top.earthstudio.nextgenbedwars.core.gui.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import org.bukkit.inventory.ItemStack;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;

import top.earthstudio.nextgenbedwars.core.gui.GuiHolder;

public class GuiInteractListener implements Listener {
    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiHolder guiHolder)) {
            return;
        }

        GuiPage page = guiHolder.guiPage;

        // 点击发生在上方 GUI 视窗内
        if (event.getClickedInventory() == event.getInventory()) {
            event.setCancelled(true);

            if (event.getClick() == org.bukkit.event.inventory.ClickType.DOUBLE_CLICK) {
                return;
            }

            if (event.getWhoClicked() instanceof Player player) {
                guiHolder.handleClick(event.getSlot(), player, event.getClick());
            }

            return;
        }

        // 点击发生在下方玩家背包内
        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            // 如果页面不允许操作玩家背包，拦截
            if (!page.allowPlayerInventoryInteraction) {
                event.setCancelled(true);
                return;
            }

            // 禁止 +Shift 操作
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }

            // 双击吸附收集修改
            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true); // 掐死原版（绝不允许它碰上方 0~53 号格子）
                if (event.getWhoClicked() instanceof Player player) {
                    // 手动只吸附玩家自己的背包！
                    handleManualCollect(player);
                }
                return;
            }
        }
    }

    @EventHandler
    public void onGuiDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiHolder guiHolder)) {
            return;
        }

        GuiPage page = guiHolder.guiPage;

        if (!page.allowPlayerInventoryInteraction) {
            event.setCancelled(true);
            return;
        }

        // 检查拖拽的轨迹是否碰到了上方 GUI 区域
        int topInventorySize = event.getInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topInventorySize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void handleManualCollect(Player player) {
        ItemStack cursor = player.getItemOnCursor();
        if (cursor.getType().isAir()) return;

        int maxStack = cursor.getMaxStackSize();
        int need = maxStack - cursor.getAmount();
        if (need <= 0) return; // 已经满了（64个），不需要吸

        // 只获取玩家的 36 格常规背包存储区（排除装备栏和副手，不干扰身上穿的衣服）
        ItemStack[] storage = player.getInventory().getStorageContents();
        boolean modified = false;

        for (int i = 0; i < storage.length; i++) {
            ItemStack item = storage[i];
            // 只寻找完全相同（材质、Meta、附魔一致）的物品
            if (item != null && !item.getType().isAir() && item.isSimilar(cursor)) {
                int count = item.getAmount();
                int take = Math.min(need, count);

                item.setAmount(count - take);
                if (item.getAmount() <= 0) {
                    storage[i] = null; // 扣空了就变空气
                }

                cursor.setAmount(cursor.getAmount() + take);
                need -= take;
                modified = true;

                if (need <= 0) break; // 手里的吸满了，收工
            }
        }

        // 一次性写回，网络发包同步给客户端
        if (modified) {
            player.getInventory().setStorageContents(storage);
            player.setItemOnCursor(cursor);
        }
    }
}
