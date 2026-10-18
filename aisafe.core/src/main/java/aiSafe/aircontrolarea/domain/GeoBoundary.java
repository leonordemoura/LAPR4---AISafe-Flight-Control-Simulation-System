package aiSafe.aircontrolarea.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import eapli.framework.domain.model.ValueObject;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Embeddable
public class GeoBoundary implements ValueObject {
    @Transient
    private List<Coordinate> coordinates;

    protected GeoBoundary() {
        // For ORM.
    }

    public GeoBoundary(final List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() != 4) {
            throw new IllegalArgumentException("GeoBoundary must have exactly 4 coordinates");
        }

        final Set<Coordinate> uniqueCoordinates = new HashSet<>(coordinates);
        if (uniqueCoordinates.size() != 4) {
            throw new IllegalArgumentException("GeoBoundary coordinates must be distinct");
        }

        final Set<Double> latitudes = new HashSet<>();
        final Set<Double> longitudes = new HashSet<>();
        for (Coordinate coordinate : coordinates) {
            latitudes.add(coordinate.latitude());
            longitudes.add(coordinate.longitude());
        }

        // For this US we model a rectangle aligned to latitude/longitude axes.
        if (latitudes.size() != 2 || longitudes.size() != 2) {
            throw new IllegalArgumentException("GeoBoundary must form a valid rectangle");
        }

        final double minLat = latitudes.stream().min(Double::compareTo).orElseThrow();
        final double maxLat = latitudes.stream().max(Double::compareTo).orElseThrow();
        final double minLon = longitudes.stream().min(Double::compareTo).orElseThrow();
        final double maxLon = longitudes.stream().max(Double::compareTo).orElseThrow();

        if ((maxLat - minLat) <= 0 || (maxLon - minLon) <= 0) {
            throw new IllegalArgumentException("GeoBoundary rectangle must have positive area");
        }

        final Set<Coordinate> rectangleCorners = Set.of(
                new Coordinate(minLat, minLon),
                new Coordinate(minLat, maxLon),
                new Coordinate(maxLat, minLon),
                new Coordinate(maxLat, maxLon)
        );
        if (!rectangleCorners.equals(uniqueCoordinates)) {
            throw new IllegalArgumentException("GeoBoundary coordinates do not define a valid rectangle");
        }

        this.coordinates = List.copyOf(coordinates);
    }

    public List<Coordinate> coordinates() {
        return List.copyOf(coordinates);
    }

    /** Axis-aligned bounding box (rectangle in lat/lon), derived from corner coordinates. */
    public double minLatitude() {
        return coordinates.stream().mapToDouble(Coordinate::latitude).min().orElseThrow();
    }

    public double maxLatitude() {
        return coordinates.stream().mapToDouble(Coordinate::latitude).max().orElseThrow();
    }

    public double minLongitude() {
        return coordinates.stream().mapToDouble(Coordinate::longitude).min().orElseThrow();
    }

    public double maxLongitude() {
        return coordinates.stream().mapToDouble(Coordinate::longitude).max().orElseThrow();
    }

    public boolean overlapsWith(final GeoBoundary otherBoundary) {
        final double thisMinLat = minLatitude();
        final double thisMaxLat = maxLatitude();
        final double thisMinLon = minLongitude();
        final double thisMaxLon = maxLongitude();

        final double otherMinLat = otherBoundary.minLatitude();
        final double otherMaxLat = otherBoundary.maxLatitude();
        final double otherMinLon = otherBoundary.minLongitude();
        final double otherMaxLon = otherBoundary.maxLongitude();

        final double overlapLat = Math.min(thisMaxLat, otherMaxLat) - Math.max(thisMinLat, otherMinLat);
        final double overlapLon = Math.min(thisMaxLon, otherMaxLon) - Math.max(thisMinLon, otherMinLon);

        // Overlap requires a positive intersection area (touching borders is allowed).
        return overlapLat > 0 && overlapLon > 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoBoundary that)) return false;
        return coordinates.equals(that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates);
    }

    //for us052 - register an airport (see if airport coordinates are valid for choosen aircontrolarea)
    public boolean contains(final Coordinate coordinate) {
        return coordinate.latitude() >= minLatitude()
                && coordinate.latitude() <= maxLatitude()
                && coordinate.longitude() >= minLongitude()
                && coordinate.longitude() <= maxLongitude();
    }
}
