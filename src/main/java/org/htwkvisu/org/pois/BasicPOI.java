package org.htwkvisu.org.pois;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.htwkvisu.gui.MapCanvas;
import org.htwkvisu.org.IMapDrawable;

public class BasicPOI implements IMapDrawable {
    private final ScoreType type;
    private final Point2D position;
    private static final double POINT_SIZE = 2;
    private final Color color;

    public BasicPOI(ScoreType type, Point2D position) {
        this.type = type;
        this.position = position;
        this.color = type.getCategory().getColor();
    }

    @Override
    public String getName() {
        return type.name();
    }

    @Override
    public double getMinDrawScale() {
        return 0;
    }

    @Override
    public void draw(GraphicsContext gc, MapCanvas canvas) {
        Point2D lclPt = canvas.transferCoordinateToPixel(position);
        gc.setFill(color);
        gc.fillOval(lclPt.getX() - POINT_SIZE / 2, lclPt.getY() - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
    }

    @Override
    public boolean showDuringGrab() {
        return true;
    }

    @Override
    public Point2D getCoordinates() {
        return position;
    }
}
