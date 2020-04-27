package stats;

import mapper.EnumClasses;
import matcher.Matcher;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

import java.util.HashMap;

public class StructuralChange extends Change {

    public StructuralChange() {
        super();
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


        // additional relationship types (for INSERT_NODE edit operations)
        this.initMapEntry(EnumClasses.GMLRelTypes.BOUNDED_BY.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.BOUNDED_BY_SURFACE.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.LOD1_TERRAIN_INTERSECTION.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.LOD1_SOLID.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.LOD2_SOLID.toString());
    }

    @Override
    public boolean contains(String key, Matcher.EditOperators editOperator) {
        // Only accept edit operators on node levels, i.e. INSERT_NODE and DELETE_NODE
        if (!editOperator.equals(Matcher.EditOperators.INSERT_NODE)
                && !editOperator.equals(Matcher.EditOperators.DELETE_NODE)) {
            return false;
        }

        HashMap<Matcher.EditOperators, Long> value = this.map.get(key);

        if (value == null) {
            return false;
        }

        value.put(editOperator, value.get(editOperator) + 1);
        return true;
    }

    @Override
    public String getLabel() {
        return "Structural Changes";
    }


}
