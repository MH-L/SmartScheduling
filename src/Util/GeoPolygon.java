package Util;

import java.util.ArrayList;
import java.util.List;

public class GeoPolygon {

    public static final double EARTH_RADIUS = 6378.245;
    private List<LatLon> vertices;

    private int layer;
    private int order;

    public GeoPolygon(List<LatLon> vertices) {
        this.vertices = vertices;
    }

    /**
     * Ordering: North, East, South, West
     *
     * @return bounds of the polygon
     */
    public double[] getBounds() {
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double maxLon = Double.MIN_VALUE;
        for (LatLon vertex : vertices) {
            if (vertex.getLat() > maxLat) {
                maxLat = vertex.getLat();
            } else if (vertex.getLat() < minLat) {
                minLat = vertex.getLat();
            }

            if (vertex.getLon() > maxLon) {
                maxLon = vertex.getLon();
            } else if (vertex.getLon() < minLon) {
                minLon = vertex.getLon();
            }
        }

        return new double[] { maxLat, maxLon, minLat, minLon };
    }

    private static boolean isPointInRect(double[] rectBounds, LatLon point) {
        return point.getLat() <= rectBounds[0] && point.getLat() >= rectBounds[2] && point.getLon() <= rectBounds[1]
                && point.getLon() >= rectBounds[3];
    }

    public boolean isPointInPolygon(LatLon p) {
        // First check if the point is in the out bounds
        double[] polygonBounds = this.getBounds();
        if (!isPointInRect(polygonBounds, p)) {
            return false;
        }

        int N = vertices.size();
        int intersectCount = 0;// cross points count of x
        double precision = 2e-10;
        LatLon p1, p2; // neighbour bound vertices

        p1 = vertices.get(0);// left vertex
        for (int i = 1; i <= N; ++i) {// check all rays
            if (p.equals(p1)) {
                return true;// p is a vertex
            }

            p2 = vertices.get(i % N);// right vertex
            if (p.getLat() < Math.min(p1.getLat(), p2.getLat()) || p.getLat() > Math.max(p1.getLat(), p2.getLat())) {
                p1 = p2;
                continue;
            }

            if (p.getLat() > Math.min(p1.getLat(), p2.getLat()) && p.getLat() < Math.max(p1.getLat(), p2.getLat())) {
                if (p.getLon() <= Math.max(p1.getLon(), p2.getLon())) {
                    if (p1.getLat() == p2.getLat() && p.getLon() >= Math.min(p1.getLon(), p2.getLon())) {
                        return true;
                    }

                    if (p1.getLon() == p2.getLon()) {// ray is vertical
                        if (p1.getLon() == p.getLon()) {// overlies on a
                            // vertical ray
                            return true;
                        } else {// before ray
                            ++intersectCount;
                        }
                    } else {// cross point on the left side
                        double xinters = (p.getLat() - p1.getLat()) * (p2.getLon() - p1.getLon())
                                / (p2.getLat() - p1.getLat()) + p1.getLon();
                        if (Math.abs(p.getLon() - xinters) < precision) {
                            return true;
                        }

                        if (p.getLon() < xinters) {// before ray
                            ++intersectCount;
                        }
                    }
                }
            } else {// special case when ray is crossing through the vertex
                if (p.getLat() == p2.getLat() && p.getLon() <= p2.getLon()) {
                    LatLon p3 = vertices.get((i + 1) % N); // next vertex
                    if (p.getLat() >= Math.min(p1.getLat(), p3.getLat())
                            && p.getLat() <= Math.max(p1.getLat(), p3.getLat())) {
                        ++intersectCount;
                    } else {
                        intersectCount += 2;
                    }
                }
            }
            p1 = p2; // next ray left point
        }

        return intersectCount % 2 != 0;
    }

    public int getLayer() {
        return layer;
    }

    public int getOrder() {
        return order;
    }

    public static double getEarthDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        return s;
    }

    public static double getEarthDistance(String lat1, String lon1, String lat2, String lon2) {
        return getEarthDistance(Double.parseDouble(lat1), Double.parseDouble(lon1),
                Double.parseDouble(lat2), Double.parseDouble(lon2));
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
}
