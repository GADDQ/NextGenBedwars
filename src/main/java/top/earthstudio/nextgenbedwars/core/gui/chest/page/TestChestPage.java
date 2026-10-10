package top.earthstudio.nextgenbedwars.core.gui.chest.page;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiPage;
import top.earthstudio.nextgenbedwars.api.util.gui.chest.ChestGuiUtil;

import java.util.Random;

public class TestChestPage extends ChestGuiPage { // TODO: this need remove after docs finish, show as example in docs
    private final Random random = new Random();

    public TestChestPage() {
        super(Component.text("Test Gui"), 1);
        this.allowPlayerInventoryInteraction = true;
        add(
                4,
                0,
                ChestGuiUtil.button(
                        Material.GREEN_BANNER,
                        Component.text("测试").color(NamedTextColor.GREEN),
                        Component.text("这是 GUI 系统的测试按钮")
                ),
                this::test
        );

        add(
                1,
                0,
                new ItemStack(Material.GRASS_BLOCK)
        );
    }

    private void test(Player player, ClickType clickType) {
        if (clickType.isRightClick())
            player.sendMessage("测试信息: " + clickType);

        this.buttons.clear();
        add(
                random.nextInt(8),
                0,
                ChestGuiUtil.button(
                        Material.GREEN_BANNER,
                        Component.text("测试").color(NamedTextColor.GREEN),
                        Component.text("这是 GUI 系统的测试按钮")
                ),
                this::test
        );
        update();
    }
}
