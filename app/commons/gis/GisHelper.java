package commons.gis;

import ch.helin.messages.commons.AssertUtils;
import ch.helin.messages.dto.way.Position;
import ch.helin.messages.dto.way.Waypoint;
import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.codec.Wkb;
import org.geolatte.geom.codec.WkbDecoder;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Contains common static methods related to post gis
 */
public class GisHelper {

    private static final CoordinateReferenceSystem<?> WGS84_REFERENCE_SYSTEM =
            CrsRegistry.getCoordinateReferenceSystemForEPSG(4326, CoordinateReferenceSystems.PROJECTED_2D_METER);

    private static final double ROUNDOFF_PRECISION = 0.00000000001; // below 1mm - so it should be equal


    private GisHelper() {
    }


    /*
     * Can be used to access the WGS84 reference system
     */
    public static CoordinateReferenceSystem<?> getReferenceSystem(){
        return WGS84_REFERENCE_SYSTEM;
    }


    /*
     * Can be used to access the WGS84 reference system
     */
    public static double getRoundoffPrecision(){ return ROUNDOFF_PRECISION; }


    /*
     * Converts the point to WKT without the SRID information
     * ( SRID = Spatial Reference Identifier )
     */
    public static String toWktStringWithoutSrid(Geometry<?> geometry) {
        AssertUtils.throwExceptionIfNull(geometry);

        String wktWith = Wkt.toWkt(geometry);
        return wktWith.replace("SRID=4326;", "");
    }


    /**
     * Create point from longitude and latitude using WGS-84
     * ( see http://spatialreference.org/ref/epsg/wgs-84/ )
     */
    public static Point createPoint(double longitude, double latitude) {
        Geometry<?> geometry = Wkt.fromWkt("SRID=4326; POINT (" + longitude + " " + latitude + ")");
        return (Point) geometry;
    }

    public static Position createPosition(String wktWithoutSRID) {
        String wktWithSRID = "SRID=4326; " + wktWithoutSRID;
        Geometry<?> geometry = Wkt.fromWkt(wktWithSRID);
        Point point = (Point) geometry;
        return createPosition(point);
    }

    public static Position createPosition(Point point) {
        Position position = new Position();
        position.setLat(point.getPosition().getCoordinate(1));
        position.setLon(point.getPosition().getCoordinate(0));
        return position;
    }

    /**
     *
     * @param wkbString - a WellKnownBinary String
     * @return GeoLatte.geometry - parsed to a simple WKT format
     */

    public static Geometry convertFromWkbToGeometry(String wkbString) {
        if (wkbString == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.from(wkbString);
        WkbDecoder decoder = Wkb.newDecoder(Wkb.Dialect.POSTGIS_EWKB_1);
        return decoder.decode(buffer);
    }

    /**
     *
     * @param wktString - a WellKnownBinary String
     * @return GeoLatte.geometry - parsed to a simple WKT format
     */

    public static Geometry convertFromWktToGeometry(String wktString) {
        if (wktString == null) {
            return null;
        }

        return Wkt.fromWkt(wktString, WGS84_REFERENCE_SYSTEM);
    }

    public static List<Waypoint> getWaypointListFromPositions(List<org.geolatte.geom.Position> positionList){
        List<Waypoint> returnWaypointList = new LinkedList<Waypoint>();

        for (org.geolatte.geom.Position position : positionList) {
            org.geolatte.geom.Position p  = position;
            double lon = p.getCoordinate(0); // <<-- this 0 sucks, but is the x component
            double lat = p.getCoordinate(1);

            Waypoint waypoint = new Waypoint();
            waypoint.setId(UUID.randomUUID());
            waypoint.setPosition(new ch.helin.messages.dto.way.Position(lat, lon, 0));
            returnWaypointList.add(waypoint);
        }

        return returnWaypointList;
    }

}
