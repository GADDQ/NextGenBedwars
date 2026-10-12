package top.earthstudio.nextgenbedwars.core.gui.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.gui.IGuiHolder;
import top.earthstudio.nextgenbedwars.api.gui.IGuiPage;
import top.earthstudio.nextgenbedwars.core.gui.GuiManagerImpl;

public class GuiInteractListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGuiClick(InventoryClickEvent event) {
        IGuiHolder guiHolder = GuiManagerImpl.getGuiHolder(event.getView());
        if (guiHolder == null)
            return;

        IGuiPage page = guiHolder.guiPage;

        // 点击发生在上方 GUI 视窗内
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);

            if (event.getClick() == org.bukkit.event.inventory.ClickType.DOUBLE_CLICK)
                return;

            if (event.getWhoClicked() instanceof Player player)
                guiHolder.handleClick(event.getSlot(), player, event.getClick());

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
                event.setCancelled(true);

                if (event.getWhoClicked() instanceof Player player)
                    handleManualCollect(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGuiDrag(InventoryDragEvent event) {
        IGuiHolder guiHolder = GuiManagerImpl.getGuiHolder(event.getView());
        if (guiHolder == null)
            return;

        IGuiPage page = guiHolder.guiPage;

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGuiClose(InventoryCloseEvent event) {
        IGuiHolder guiHolder = GuiManagerImpl.getGuiHolder(event.getView());
        if (guiHolder == null)
            return;

        event.getView().getTopInventory().clear();

        GuiManagerImpl.close(guiHolder.inventoryView);

        if (event.getPlayer() instanceof Player player)
            guiHolder.handleClose(player);
    }

    private void handleManualCollect(Player player) {
        ItemStack cursor = player.getItemOnCursor();
        if (cursor.getType().isAir()) return;

        int maxStack = cursor.getMaxStackSize();
        int need = maxStack - cursor.getAmount();
        if (need <= 0) return; // 已满

        // 获取背包存储区
        ItemStack[] storage = player.getInventory().getStorageContents();
        boolean modified = false;

        for (int i = 0; i < storage.length; i++) {
            ItemStack item = storage[i];
            // 寻找完全相同（材质、Meta、附魔一致）的物品
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

                if (need <= 0) break; // 吸满
            }
        }

        if (modified) {
            player.getInventory().setStorageContents(storage);
            player.setItemOnCursor(cursor);
        }
    }
}
