package top.earthstudio.nextgenbedwars.core.shop;

import net.kyori.adventure.text.Component;

import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;

import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiPage;
import top.earthstudio.nextgenbedwars.api.shop.Shop;

import java.util.UUID;

public class ShopEntity {
    public Shop shop;
    private Entity bodyEntity;
    private TextDisplay textDisplay;

    public ShopEntity(Shop shop) {
        this.shop = shop;

        this.bodyEntity = shop.location.getWorld().spawnEntity(shop.location, shop.entityType);
        this.bodyEntity.setPersistent(false); // TODO: FIXME: TEST ONLY

        if (bodyEntity instanceof LivingEntity living) {
            living.setAI(false);
            living.setInvulnerable(true);
            living.setSilent(true);
            living.setCollidable(false);
            living.setRemoveWhenFarAway(false);
            living.setGravity(false);
        }

        this.textDisplay = shop.location.getWorld().spawn(shop.location, TextDisplay.class, display -> {
            display.setPersistent(false);

            Component fullText = shop.title.appendNewline().append(shop.subTitle);
            display.text(fullText);

            display.setBillboard(Display.Billboard.VERTICAL);
            display.setAlignment(TextDisplay.TextAlignment.CENTER);
            display.setShadowed(true);
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            display.setBrightness(new Display.Brightness(15, 15));

            display.setTransformation(new Transformation( // TODO: ATTENTION! this tweak might need change with language
                    new Vector3f(-0.0325f, 2.125f, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f(0, 0, 0, 0)
            ));
        });
    }

    public UUID getUuid() {
        return bodyEntity.getUniqueId();
    }

    public ChestGuiPage getGui() {
        return shop.shopPageProvider.get();
    }

    public void destroy() {
        textDisplay.remove();
        textDisplay = null;
        bodyEntity.remove();
        bodyEntity = null;
        shop = null;
    }
}
