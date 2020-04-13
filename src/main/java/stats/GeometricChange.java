package stats;

import mapper.EnumClasses;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

public class GeometricChange extends Change {

    public GeometricChange() {
        super();
        // the test for geometric changes must occur AFTER
        // procedural, thematic, and appearance changes, ...
        // since the following node types can also contain some of these changes

        this.initMapEntry("VALUE");

        // node types from the function matchGeometry(...) in Matcher
        this.initMapEntry(CityGMLClass.CITY_OBJECT_MEMBER.toString());
        this.initMapEntry(CityGMLClass.BUILDING.toString());
        this.initMapEntry(CityGMLClass.BUILDING_PART_PROPERTY.toString());
        this.initMapEntry(CityGMLClass.BUILDING_PART.toString());

        this.initMapEntry(CityGMLClass.BUILDING_BOUNDARY_SURFACE_PROPERTY.toString());
        this.initMapEntry(CityGMLClass.BUILDING_CEILING_SURFACE.toString());
        this.initMapEntry(CityGMLClass.BUILDING_CLOSURE_SURFACE.toString());
        this.initMapEntry(CityGMLClass.BUILDING_FLOOR_SURFACE.toString());
        this.initMapEntry(CityGMLClass.BUILDING_GROUND_SURFACE.toString());
        this.initMapEntry(CityGMLClass.INTERIOR_BUILDING_WALL_SURFACE.toString());
        this.initMapEntry(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE.toString());
        this.initMapEntry(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE.toString());
        this.initMapEntry(CityGMLClass.BUILDING_ROOF_SURFACE.toString());
        this.initMapEntry(CityGMLClass.BUILDING_WALL_SURFACE.toString());

        this.initMapEntry(GMLClass.SURFACE_PROPERTY.toString());
        this.initMapEntry(GMLClass.POLYGON.toString());
        this.initMapEntry(GMLClass.LINEAR_RING.toString());
        this.initMapEntry(GMLClass.RING.toString());
        this.initMapEntry(GMLClass.ENVELOPE.toString());
        this.initMapEntry(GMLClass.POINT.toString());
        this.initMapEntry(GMLClass.POINT_PROPERTY.toString());
        this.initMapEntry(GMLClass.POINT_REP.toString());
        this.initMapEntry(GMLClass.DIRECT_POSITION.toString());
        this.initMapEntry(GMLClass.COORD.toString());
        this.initMapEntry(GMLClass.DIRECT_POSITION_LIST.toString());
        this.initMapEntry(GMLClass.COORDINATES.toString());
        this.initMapEntry(GMLClass.LINE_STRING_PROPERTY.toString());
        this.initMapEntry(GMLClass.LINE_STRING.toString());
        this.initMapEntry(GMLClass.LINE_STRING.toString());

        // additional node types and property names
        this.initMapEntry(CityGMLClass.TEX_COORD_LIST.toString());
        this.initMapEntry(GMLClass.POS_OR_POINT_PROPERTY_OR_POINT_REP.toString());
        this.initMapEntry(GMLClass.POS_OR_POINT_PROPERTY_OR_POINT_REP_OR_COORD.toString());
        this.initMapEntry(GMLClass.MULTI_CURVE.toString());
        this.initMapEntry(GMLClass.CURVE_PROPERTY.toString());
        this.initMapEntry(GMLClass.MULTI_CURVE_PROPERTY.toString());
        this.initMapEntry(GMLClass.INTERIOR.toString());
        this.initMapEntry(CityGMLClass.SURFACE_DATA_PROPERTY.toString());
        this.initMapEntry(GMLClass.MULTI_SURFACE.toString());
        this.initMapEntry(GMLClass.COMPOSITE_SURFACE.toString());
        this.initMapEntry(GMLClass.SOLID.toString());
        this.initMapEntry(GMLClass.SOLID_PROPERTY.toString());
        this.initMapEntry(GMLClass.BOUNDING_SHAPE.toString());

        // additional relationship types (for INSERT_NODE edit operations)
        this.initMapEntry(EnumClasses.GMLRelTypes.SURFACE_MEMBER.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.SURFACE_MEMBERS.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.OBJECT.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.BOUNDED_BY.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.BOUNDED_BY_SURFACE.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.LOD2_SOLID.toString());
    }

    @Override
    public String getLabel() {
        return "Geometric Changes";
    }
}
