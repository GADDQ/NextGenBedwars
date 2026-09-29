package top.earthstudio.nextgenbedwars.core.gui.page;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;
import top.earthstudio.nextgenbedwars.api.gui.GuiUtil;

import java.util.Random;

public class TestPage extends GuiPage {
    private final Random random = new Random();

    public TestPage() {
        super(Component.text("Test Gui"), 1);
        this.allowPlayerInventoryInteraction = true;
        add(
                4,
                0,
                GuiUtil.button(
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
                GuiUtil.button(
                        Material.GREEN_BANNER,
                        Component.text("测试").color(NamedTextColor.GREEN),
                        Component.text("这是 GUI 系统的测试按钮")
                ),
                this::test
        );
        update();
    }
}
