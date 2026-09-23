package top.earthstudio.nextgenbedwars.core.spawner;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.spawner.Spawner;

import static top.earthstudio.nextgenbedwars.core.spawner.SpawnerDisplayUtil.ZERO_VELOCITY;

public class SpawnerEntity {
    public Spawner spawner;
    public SpawnerAnimator animator;

    private final ItemDisplay itemDisplay;
    private final TextDisplay textDisplay;

    private int leftTick;

    public SpawnerEntity(Spawner spawner) {
        this.spawner = spawner;
        this.animator = new SpawnerAnimator();
        spawner.location.setPitch(0);
        spawner.location.setYaw(0);

        leftTick = spawner.spawnTick;

        itemDisplay = spawner.location.getWorld().spawn(spawner.location, ItemDisplay.class, entity -> {
            entity.setPersistent(false); // TODO: DEBUG only, remove in release

            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setItemStack(new ItemStack(spawner.holoMaterial == null ? Material.AIR : spawner.holoMaterial));
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            entity.setShadowRadius(0);
            entity.setShadowStrength(0);
        });

        textDisplay = spawner.location.getWorld().spawn(spawner.location, TextDisplay.class, entity -> {
            entity.setPersistent(false); // TODO: DEBUG only, remove in release

            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setBillboard(Display.Billboard.VERTICAL);
            entity.setShadowed(true);
            entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            entity.setTransformation(new Transformation(
                    new Vector3f(0, 1.375f, 0), // translation
                    new AxisAngle4f(0, 0, 0, 0), // leftRotation
                    new Vector3f(0.75f, 0.75f, 0.75f), // scale
                    new AxisAngle4f(0, 0, 0, 0) // rightRotation
            ));
        });
    }

    public void update() {
        if (leftTick % 20 == 0 || leftTick == spawner.spawnTick) {
            int seconds = (leftTick - 1) / 20 + 1;
            textDisplay.text(SpawnerDisplayUtil.updateTextDisplay(seconds, spawner));
        }

        animator.update(itemDisplay, spawner);

        leftTick--;
        if (leftTick > 0)
            return;
        leftTick = spawner.spawnTick;

        if (SpawnerDisplayUtil.canSpawn(spawner)) {
            Item item = spawner.location.getWorld().dropItem(spawner.location, new ItemStack(spawner.material));
            item.setVelocity(ZERO_VELOCITY);

            item.getPersistentDataContainer().set(PdcKeys.spawner.drop, PersistentDataType.BOOLEAN, true);
            if (!spawner.isAllowMerge)
                item.getPersistentDataContainer().set(PdcKeys.spawner.preventMerge, PersistentDataType.BOOLEAN, true);
        }

        Material targetMaterial = spawner.holoMaterial == null ? Material.AIR : spawner.holoMaterial;
        if (itemDisplay.getItemStack().getType() != targetMaterial)
            itemDisplay.setItemStack(new ItemStack(targetMaterial));
    }

    public void destroy() {
        itemDisplay.remove();
        textDisplay.remove();
        animator = null;
        spawner = null;
    }
}
