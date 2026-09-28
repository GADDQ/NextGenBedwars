package top.earthstudio.nextgenbedwars.core.gui.page;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;
import top.earthstudio.nextgenbedwars.api.gui.GuiUtil;

public class TestPage extends GuiPage {
    public TestPage() {
        super(Component.text("Test Gui"), 1, true);
        add(
                4,
                GuiUtil.button(
                        Material.GREEN_BANNER,
                        Component.text("测试").color(NamedTextColor.GREEN),
                        Component.text("这是 GUI 系统的测试按钮")
                ),
                this::test
        );
    }

    private void test(Player player, ClickType clickType) {
        player.sendMessage("测试信息: " + clickType);
    }
}
