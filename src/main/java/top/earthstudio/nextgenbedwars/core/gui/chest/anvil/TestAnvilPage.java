package top.earthstudio.nextgenbedwars.core.gui.chest.anvil;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;

import org.bukkit.entity.Player;
import top.earthstudio.nextgenbedwars.api.gui.anvil.AnvilGuiPage;

import java.util.Objects;

public class TestAnvilPage extends AnvilGuiPage {
    public TestAnvilPage() {
        super(Component.text("铁砧 Gui 测试"), "我是默认文字", Material.GLASS);
        this.allowPlayerInventoryInteraction = true;
    }

    @Override
    public void onClose(Player player) {
        if (Objects.equals(finalText, "")) {
            player.sendMessage(Component.text("玩家取消输入!"));
            return;
        }
        player.sendMessage(Component.text("你输入的文本是: ").append(Component.text(finalText)));
    }
}
