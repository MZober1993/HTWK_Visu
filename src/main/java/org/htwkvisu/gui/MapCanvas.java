package org.htwkvisu.gui;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.htwkvisu.org.IMapDrawable;
import org.htwkvisu.org.pois.BasicPOI;
import org.htwkvisu.org.pois.NormalizedColorCalculator;
import org.htwkvisu.org.pois.ScoringCalculator;
import org.htwkvisu.utils.MathUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Canvas for map.
 */
public class MapCanvas extends BasicCanvas {

    private Point2D mapCenter = new Point2D(50.832222, 12.92416666); //Chemnitz
    private GraphicsContext gc = getGraphicsContext2D();
    private double widthDistance = 0;
    private double heightDistance = 0;
    private int displayedElems = 0;
    private CheckBox colorModeCheckBox;

    /**
     * Construct and init canvas
     */
    public MapCanvas(ScoringConfig config) {
        super(config);

        // add test cities
        addDrawableElement(new City(new Point2D(51.340333, 12.37475), "Leipzig", 0));
        addDrawableElement(new City(new Point2D(51.049259, 13.73836112), "Dresden", 0));
        addDrawableElement(new City(new Point2D(50.832222, 12.92416666), "Chemnitz", 0));
        addDrawableElement(new City(new Point2D(50.718888, 12.492222), "Zwickau", 0));
    }

    @Override
    protected void drawInfo() {
        gc.setFill(Color.GRAY);
        gc.fillText("Center: " + MathUtils.roundToDecimalsAsString(mapCenter.getX(), 5) + " " +
                MathUtils.roundToDecimalsAsString(mapCenter.getY(), 5), 10, 20);
        gc.fillText("Distance: " + MathUtils.roundToDecimalsAsString(widthDistance, 3) + " km x " + MathUtils.roundToDecimalsAsString(heightDistance, 3) + " km", 10, 40);
        gc.fillText("Elements displayed: " + displayedElems, 10, 60);
        gc.fillText("Scale: " + MathUtils.roundToDecimalsAsString(scale, 2), 10, 80);
        gc.fillText("Bounds: " + getCoordsBoundsAsString() , 10, 100);
    }

    @Override
    public void drawScoringValues() {
// get sample points for canvas
        // sample points will be drawn every "samplingPixelDensity" pixels in x and y direction
        List<List<Point2D>> gridPoints = calculateGrid();
        // save previous colors
        final Paint curFillPaint = gc.getFill();
        final Paint curStrokePaint = gc.getStroke();

        NormalizedColorCalculator norm = new NormalizedColorCalculator(this, colorModeCheckBox.isSelected());
        // now calculate the values
        for (List<Point2D> line : gridPoints) {
            for (Point2D pt : line) {
                gc.setFill(norm.calculateColor(pt));

                Point2D pixelPos = transferCoordinateToPixel(pt);
                gc.fillRect(pixelPos.getX(), pixelPos.getY(), config.getSamplingPixelDensity()
                        , config.getSamplingPixelDensity());
            }
        }

        // interpolate between the samples points with simple linear interpolation in our matrix/grid

        //Restore previous colors
        gc.setFill(curFillPaint);
        gc.setStroke(curStrokePaint);
    }

    @Override
    public void drawGrid() {
        double northPos;
        double eastPos;
        gc.setStroke(Color.BLACK);

        // Latitude
        double x = coordsBounds.getMinX();
        while (x < coordsBounds.getMaxX()) {
            double fullVal = Math.ceil(x);
            northPos = transferCoordinateToPixel(new Point2D(fullVal, Math.ceil(x))).getY();
            gc.strokeLine(0, northPos, tmpWidth, northPos);
            gc.fillText(MathUtils.roundToDecimalsAsString(fullVal, 0), 10, northPos - 10);
            x++;
        }

        // Longitude
        double y = coordsBounds.getMinY();
        while (y < coordsBounds.getMaxY()) {
            double fullVal = Math.ceil(y);
            eastPos = transferCoordinateToPixel(new Point2D(Math.ceil(y), fullVal)).getX();
            gc.strokeLine(eastPos, 0, eastPos, tmpHeight);
            gc.fillText(MathUtils.roundToDecimalsAsString(fullVal, 0), eastPos + 10, tmpHeight - 10);
            y++;
        }
    }

    @Override
    public void addDrawableElement(IMapDrawable elem) {
        if (elem == null) {
            throw new IllegalArgumentException("No valid element!");
        }

        drawables.add(elem);
    }


    private void drawPOIS() {
        final Paint curFillPaint = gc.getFill();
        final Paint curStrokePaint = gc.getStroke();

        for (BasicPOI poi : ScoringCalculator.generateEnabled()) {
            poi.draw(this.gc, this);
        }

        gc.setFill(curFillPaint);
        gc.setStroke(curStrokePaint);
    }


    @Override
    public void drawElements() {
        List<IMapDrawable> toDraw = drawables.parallelStream()
                .filter(p -> !isDragging || p.showDuringGrab())
                .filter(p -> p.getMinDrawScale() < scale)
                .filter(p -> coordsBounds.contains(p.getCoordinates()))
                .collect(Collectors.toList());

        displayedElems = toDraw.size();
        for (IMapDrawable elem : toDraw) {
            elem.draw(this.gc, this);
        }
    }

    @Override
    public void centerView(Point2D center) {
        mapCenter = center;

        redraw();
    }

    @Override
    public Point2D getCenter() {
        return mapCenter;
    }

    @Override
    public void redraw() {
        tmpWidth = getWidth();
        tmpHeight = getHeight();
        double coveredWidth = tmpWidth / scale;
        double coveredHeight = tmpHeight / scale;
        coordsBounds = new BoundingBox(mapCenter.getX() - coveredHeight / 2, mapCenter.getY() - coveredWidth / 2,
                coveredHeight, coveredWidth);
        heightDistance = MathUtils.convertUnitsToKilometres(coordsBounds.getWidth());
        widthDistance = MathUtils.convertUnitsToKilometres(coordsBounds.getHeight());

        // clear view
        gc.clearRect(0, 0, tmpWidth, tmpHeight);

        // draw map content
        drawScoringValues();
        drawInfo();
        drawGrid();
        drawPOIS();
        drawElements();

    }

    @Override
    public Point2D transferCoordinateToPixel(Point2D p) {
        return new Point2D((p.getY() - mapCenter.getY()) * scale + tmpWidth / 2,
                ((mapCenter.getX() - p.getX()) * scale + tmpHeight / 2));
    }

    @Override
    public Point2D transferPixelToCoordinate(double x, double y) {
        return new Point2D(coordsBounds.getMaxX() - (y / tmpHeight) * coordsBounds.getWidth(),
                coordsBounds.getMinY() + (x / tmpWidth) * coordsBounds.getHeight());
    }

    public int calculateMaxScore() {
        List<List<Point2D>> gridPoints = calculateGrid();
        //setMaxScoringValue calls redraw
        return (int) gridPoints.stream().flatMap(Collection::stream)
                .mapToDouble(ScoringCalculator::calculateEnabledScoreValue).max().orElse(0.0);
    }

    public List<List<Point2D>> calculateGrid() {
        Grid grid = new Grid(this);
        return grid.calcGridPoints(config.getSamplingPixelDensity());
    }

    public void setColorModeCheckBox(CheckBox colorModeCheckBox) {
        this.colorModeCheckBox = colorModeCheckBox;
    }

    public CheckBox getColorModeCheckBox() {
        return colorModeCheckBox;
    }
}
