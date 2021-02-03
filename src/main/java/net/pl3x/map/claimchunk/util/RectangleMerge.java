package net.pl3x.map.claimchunk.util;

import net.pl3x.map.api.Point;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.Polygon;
import net.pl3x.map.claimchunk.data.Claim;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// https://stackoverflow.com/a/56326866

public class RectangleMerge {

    public static Polygon getPoly(List<Claim> claims) {
        List<Point> combined = new ArrayList<>();
        for (Claim claim : claims) {
            int x = claim.getX() << 4;
            int z = claim.getZ() << 4;
            List<Point> points = Arrays.asList(
                    Point.of(x, z),
                    Point.of(x, z + 16),
                    Point.of(x + 16, z + 16),
                    Point.of(x + 16, z)
            );
            if (combined.isEmpty()) {
                combined = points;
            } else {
                combined = merge(combined, points);
            }
        }
        return Marker.polygon(combined);
    }

    private static List<Point> merge(List<Point> p1, List<Point> p2) {
        Area area = new Area(toShape(p1));
        area.add(new Area(toShape(p2)));
        return toPoints(area);
    }

    private static Shape toShape(List<Point> points) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (i == 0) {
                path.moveTo(p.x(), p.z());
            } else {
                path.lineTo(p.x(), p.z());
            }
        }
        path.closePath();
        return path;
    }

    private static List<Point> toPoints(Shape shape) {
        List<Point> result = new ArrayList<>();
        PathIterator iter = shape.getPathIterator(null, 0.0);
        double[] coords = new double[6];
        while (!iter.isDone()) {
            int segment = iter.currentSegment(coords);
            switch (segment) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    result.add(Point.of(coords[0], coords[1]));
                    break;
            }
            iter.next();
        }
        return result;
    }

}

