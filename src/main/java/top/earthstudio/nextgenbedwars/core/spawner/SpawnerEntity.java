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

        leftTick = spawner.spawnerType.spawnTick.get(spawner.level);

        itemDisplay = spawner.location.getWorld().spawn(spawner.location, ItemDisplay.class, entity -> {
            entity.setBrightness(new Display.Brightness(15, 15));

            entity.setItemStack(new ItemStack(
                    (spawner.spawnerType.holoMaterial == null || !spawner.spawnerType.isShowHolo) ?
                            Material.AIR :
                            spawner.spawnerType.holoMaterial
            ));

            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            entity.setShadowRadius(0);
            entity.setShadowStrength(0);
            entity.setTransformation(new Transformation(
                    new Vector3f(0, 2.3f, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(0.75f, 0.75f, 0.75f),
                    new AxisAngle4f()
            ));
        });

        textDisplay = spawner.location.getWorld().spawn(spawner.location, TextDisplay.class, entity -> {
            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setBillboard(Display.Billboard.VERTICAL);
            entity.setShadowed(true);
            entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            entity.setTransformation(new Transformation(
                    new Vector3f(0, 1.375f, 0), // translation
                    new AxisAngle4f(0, 0, 1, 0), // leftRotation
                    new Vector3f(0.75f, 0.75f, 0.75f), // scale
                    new AxisAngle4f() // rightRotation
            ));
            entity.text(SpawnerDisplayUtil.updateTextDisplay((leftTick - 1) / 20 + 1, spawner));
        });
    }

    public void update() {
        if (leftTick % 20 == 0 || leftTick == spawner.spawnerType.spawnTick.get(spawner.level)) {
            int seconds = (leftTick - 1) / 20 + 1;
            textDisplay.text(SpawnerDisplayUtil.updateTextDisplay(seconds, spawner));
        }

        animator.update(itemDisplay, spawner);

        leftTick--;
        if (leftTick > 0)
            return;
        leftTick = spawner.spawnerType.spawnTick.get(spawner.level);

        if (canSpawn(spawner)) {
            spawner.location.getWorld().dropItem(spawner.location, new ItemStack(spawner.spawnerType.material), entity -> {
                entity.setVelocity(ZERO_VELOCITY);
                entity.getPersistentDataContainer().set(PdcKeys.spawner.drop, PersistentDataType.BOOLEAN, true);
                entity.setUnlimitedLifetime(true);

                if (!spawner.spawnerType.isAllowMerge)
                    entity.getPersistentDataContainer().set(PdcKeys.spawner.preventMerge, PersistentDataType.BOOLEAN, true);
            });
        }

        if (!spawner.spawnerType.isShowHolo)
            return;

        Material targetMaterial = spawner.spawnerType.holoMaterial == null ? Material.AIR : spawner.spawnerType.holoMaterial;
        if (itemDisplay.getItemStack().getType() != targetMaterial)
            itemDisplay.setItemStack(new ItemStack(targetMaterial));
    }

    public void destroy() {
        itemDisplay.remove();
        textDisplay.remove();
        animator = null;
        spawner = null;
    }

    public static boolean canSpawn(Spawner spawner) {
        if (spawner.spawnerType.maxSpawnCount <= 0) {
            return true;
        }

        int counter = 0;
        for (Entity entity : spawner.location.getNearbyEntities(0.75, 0.75, 0.75)) {
            if (entity instanceof Item item && item.getItemStack().getType() == spawner.spawnerType.material && item.getPersistentDataContainer().has(PdcKeys.spawner.drop)) {
                int itemAmount = item.getItemStack().getAmount();
                counter += (itemAmount == 0 ? 1 : itemAmount);
            }
        }

        return counter < spawner.spawnerType.maxSpawnCount;
    }
}
