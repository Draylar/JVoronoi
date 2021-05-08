package draylar.jvoronoi;

import javafx.util.Pair;

import java.util.*;

public class JVoronoi {

    public final OpenSimplex2F noise;
    public final OpenSimplex2F yNoise;
    public final Map<Position, Position> cachedGridFeatures = new HashMap<>();

    private final int size;

    public JVoronoi(long seed, int size) {
        noise = new OpenSimplex2F(seed);
        yNoise = new OpenSimplex2F(seed + 1);
        this.size = size;
    }

    public Position getNearestFeature(double x, double y, boolean getSecond) {
        Pair<Double, Position> first = new Pair<>(Double.MAX_VALUE, null);
        Pair<Double, Position> second = new Pair<>(Double.MAX_VALUE, null);

        // Check features in the 8 surrounding cells.
        // Because each cell has 1 feature, we are 100% sure the closest
        // point will be in this area.
        Set<Position> surroundingFeatures = getSurroundingFeatures(x, y);
        for (Position feature : surroundingFeatures) {
            double distance = Math.sqrt(Math.pow(feature.x - x, 2) + Math.pow(feature.y - y, 2));

            // If it is the closest feature we have seen so far, save it.
            if(distance <= first.getKey()) {
                // second is always going to be the last first
                if(first.getValue() != null) {
                    second = new Pair<>(first.getKey(), first.getValue());
                }

                first = new Pair<>(distance, feature);
            } else if (distance < second.getKey()) {
                second = new Pair<>(distance, feature);
            }
        }

        return getSecond ? second.getValue() : first.getValue();
    }

    public Set<Position> getSurroundingFeatures(double x, double y) {
        Set<Position> surrounding = new HashSet<>();

        for(int cellX = x <= size ? - 2 : -1; cellX <= 1; cellX++) {
            for (int cellY = y <= size ? -2 : -1; cellY <= 1; cellY++) {
                Position feature = getFeature(x + cellX * size, y + cellY * size);
                surrounding.add(feature);
            }
        }

        return surrounding;
    }

    /**
     * Returns the feature closest to the given x/y coordinate.
     *
     * <p>
     * Each grid in this Voronoi implementation has a single feature.
     * This feature is somewhere inside grid, and represents the center of a cell.
     * Every coordinate inside the cell will return the center feature with this method.
     *
     * @param x x-coordinate to evaluate at
     * @param y y-coordinate to evaluate at
     * @return the nearest feature position to the given x/y coordinates
     */
    public Position evaluate(double x, double y) {
        return getNearestFeature(x, y, false);
    }

    /**
     * Evaluates a portion of a Voronoi tesselation using the given x/y coordinates.
     *
     * @param x x-coordinate to evaluate at
     * @param y y-coordinate to evaluate at
     * @return the nearest feature position to the given x/y coordinates
     */
    public double tesselate(double x, double y) {
        Position nearestFeature = evaluate(x, y);
        return (noise.noise2(nearestFeature.x, nearestFeature.y) + 1) / 2;
    }

    public double tesselateWithEdge(double x, double y, double leeway) {
        Position nearestFeature = getNearestFeature(x, y, false);
        Position secondNearestFeature = getNearestFeature(x, y, true);

        double distanceToFirst = Math.sqrt(Math.pow(nearestFeature.x - x, 2) + Math.pow(nearestFeature.y - y, 2));
        double distanceToSecond = Math.sqrt(Math.pow(secondNearestFeature.x - x, 2) + Math.pow(secondNearestFeature.y - y, 2));

        double close = distanceToSecond - distanceToFirst;
        if(Math.abs(close) < leeway) {
            return 0;
        }

        return (noise.noise2(nearestFeature.x, nearestFeature.y) + 1) / 2;
    }

    /**
     * Returns the feature from the cell containing the given coordinates.
     *
     * <p>
     * In the context of Voronoi/Worley noise, the "feature" is defined as an unchanging point
     *  somewhere in the center of a cell. Each cell contains a single feature, which is used
     *  to improve performance when checking for closest features.
     *
     * The parameters passed down are rounded down to the closest factor of this {@link JVoronoi}'s size.
     * For example, if the voronoi cell size is {@code 50}, and {@code 55, 160} is passed in, the cell checked
     *  will be {@code 50, 150}.
     *
     * @param x
     * @param y
     * @return
     */
    public Position getFeature(double x, double y) {
        // round down to a multiple of the grid size
        int gridX = ((int) x / size) * size;
        int gridY = ((int) y / size) * size;

        // determine feature inside grid
        Position key = new Position(gridX, gridY);
        if(cachedGridFeatures.containsKey(key)) {
            return cachedGridFeatures.get(key);
        } else {
            Position calculated = new Position(
                    gridX + evaluateNormalizedNoise(noise, gridX, gridY) * size,
                    gridY + evaluateNormalizedNoise(yNoise, gridX, gridY) * size);

            cachedGridFeatures.put(key, calculated);
            return calculated;
        }
    }

    private double evaluateNormalizedNoise(OpenSimplex2F noise, double x, double y) {
        if(x == 0) {
            x = 0.001;
        } else if (y == 0) {
            y = 0.001;
        }

        return (noise.noise2(x, y) + 1) / 2;
    }

    // debug method
    public List<Position> getFeatures() {
        List<Position> features = new ArrayList<>();

        for(int x = -8; x < 8; x++) {
            for(int y = -8; y < 8; y++) {
                features.add(getFeature(x * size, y * size));
            }
        }

        return features;
    }
}
