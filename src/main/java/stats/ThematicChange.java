package stats;

import logger.LogUtil;
import mapper.EnumClasses;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

import java.util.HashMap;
import java.util.logging.Logger;

public class ThematicChange extends Change {

    public ThematicChange() {
        super();
        this.map.put("yearOfConstruction", new Long(0));
        this.map.put("yearOfDemolition", new Long(0));
        this.map.put("storeysAboveGround", new Long(0));
        this.map.put("storeysBelowGround", new Long(0));
        // class, function, usage, roof type, name
        this.map.put(GMLClass.CODE.toString(), new Long(0));
        // measured height
        this.map.put(GMLClass.LENGTH.toString(), new Long(0));
        // storey heights above/below ground
        this.map.put(GMLClass.QUANTITY_EXTENT.toString(), new Long(0));
        this.map.put(GMLClass.MEASURE_OR_NULL_LIST.toString(), new Long(0));
        // address
        this.map.put(CityGMLClass.ADDRESS_PROPERTY.toString(), new Long(0));
        // external reference
        this.map.put(CityGMLClass.EXTERNAL_REFERENCE.toString(), new Long(0));
        // generic string attribute
        this.map.put(CityGMLClass.STRING_ATTRIBUTE.toString(), new Long(0));
        // generic uri attribute
        this.map.put(CityGMLClass.URI_ATTRIBUTE.toString(), new Long(0));
        // generalizes to
        this.map.put(CityGMLClass.GENERALIZATION_RELATION.toString(), new Long(0));
        // location
        this.map.put(GMLClass.LOCATION_PROPERTY.toString(), new Long(0));
        // description
        this.map.put(GMLClass.STRING_OR_REF.toString(), new Long(0));
        this.map.put(GMLClass.META_DATA_PROPERTY.toString(), new Long(0));
    }

    @Override
    public void printMap(Logger logger) {
        LogUtil.logMap(logger, this.map, "Thematic Change");
    }
}
