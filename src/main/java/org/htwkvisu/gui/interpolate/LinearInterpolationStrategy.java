package org.htwkvisu.gui.interpolate;

import javafx.scene.paint.Color;

public class LinearInterpolationStrategy implements InterpolationStrategy {

    private Color interpolateBiLinear(Color[] cols, int xSize, int y, int x, float xNorm, float yNorm) {
        Color upperCol = cols[y * xSize + x].interpolate(cols[(y - 1) * xSize + x], yNorm);
        Color lowerCol = cols[y * xSize + x - 1].interpolate(cols[(y - 1) * xSize + x - 1], yNorm);

        return lowerCol.interpolate(upperCol, xNorm);
    }

    @Override
    public Color interpolate(InterpolateConfig config) {
        return interpolateBiLinear(config.getCols(), config.getxSize(), config.getY()
                , config.getX(), config.getxNorm(), config.getyNorm());
    }
}
