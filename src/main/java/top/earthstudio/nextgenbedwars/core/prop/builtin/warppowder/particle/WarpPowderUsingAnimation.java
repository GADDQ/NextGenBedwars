package top.earthstudio.nextgenbedwars.core.prop.builtin.warppowder.particle;

import org.bukkit.Location;

import top.earthstudio.nextgenbedwars.api.particle.IParticleComplexAnimation;

public class WarpPowderUsingAnimation extends IParticleComplexAnimation {
    public WarpPowderUsingAnimation(Location location) {
        super(location);

        // Total Time = (layers - 1) * layerInterval + ringStayTicks
        int circleElements = 20;
        double radius = 1.5;
        int layers = 15;
        double totalHeight = 2.5;
        int layerInterval = 6;
        int ringStayTicks = 16;

        for (int layer = 0; layer < layers; layer++) {
            int triggerFrame = layer * layerInterval;
            double y = (totalHeight / layers) * layer;

            for (int i = 0; i < circleElements; i++) {
                double angle = Math.toRadians((360.0 / circleElements) * i);
                double x = radius * Math.sin(angle);
                double z = radius * Math.cos(angle);

                put(triggerFrame, new WarpPowderRingSequenceBase(x, y, z, ringStayTicks).get(location));
            }
        }
    }
}