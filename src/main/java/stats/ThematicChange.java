package stats;

import mapper.EnumClasses;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.xal.XALClass;

public class ThematicChange extends Change {

    public ThematicChange() {
        super();
        this.initMapEntry("yearOfConstruction");
        this.initMapEntry("yearOfDemolition");
        this.initMapEntry("storeysAboveGround");
        this.initMapEntry("storeysBelowGround");
        this.initMapEntry("name");
        this.initMapEntry("value");
        // property of EXTERNAL_REFERENCE
        this.initMapEntry("informationSystem");

        // TODO more ADDRESS properties
        // thoroughfare of address
        this.initMapEntry("content");

        // TODO more APPEARANCE properties
        this.initMapEntry("imageURI");
        this.initMapEntry("theme");
        this.initMapEntry("shininess");
        this.initMapEntry("ring");
        this.initMapEntry("uri");
        this.initMapEntry("red");
        this.initMapEntry("green");
        this.initMapEntry("blue");
        this.initMapEntry("transparency");
        this.initMapEntry("TARGET");

        // class, function, usage, roof type, name
        this.initMapEntry(GMLClass.CODE.toString());
        // measured height
        this.initMapEntry(GMLClass.MEASURE.toString());
        this.initMapEntry(GMLClass.LENGTH.toString());
        // storey heights above/below ground
        this.initMapEntry(GMLClass.QUANTITY_EXTENT.toString());
        this.initMapEntry(GMLClass.MEASURE_OR_NULL_LIST.toString());
        // address
        this.initMapEntry(CityGMLClass.ADDRESS_PROPERTY.toString());
        // external reference
        this.initMapEntry(CityGMLClass.EXTERNAL_REFERENCE.toString());
        // generic attributes
        this.initMapEntry(CityGMLClass.STRING_ATTRIBUTE.toString());
        this.initMapEntry(CityGMLClass.URI_ATTRIBUTE.toString());
        this.initMapEntry(CityGMLClass.MEASURE_ATTRIBUTE.toString());
        this.initMapEntry(CityGMLClass.DATE_ATTRIBUTE.toString());
        this.initMapEntry(CityGMLClass.DOUBLE_ATTRIBUTE.toString());
        this.initMapEntry(CityGMLClass.INT_ATTRIBUTE.toString());
        this.initMapEntry(CityGMLClass.GENERIC_ATTRIBUTE_SET.toString());
        // generalizes to
        this.initMapEntry(CityGMLClass.GENERALIZATION_RELATION.toString());
        // location
        this.initMapEntry(GMLClass.LOCATION_PROPERTY.toString());
        // description
        this.initMapEntry(GMLClass.STRING_OR_REF.toString());
        this.initMapEntry(GMLClass.META_DATA_PROPERTY.toString());
        this.initMapEntry(CityGMLClass.EXTERNAL_OBJECT.toString());

        // additional relationship types (for INSERT_NODE edit operations)
        this.initMapEntry(EnumClasses.GMLRelTypes.GENERIC_ATTRIBUTE.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.COUNTRY_NAME.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.ROOF_TYPE.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.MEASURED_HEIGHT.toString());
        this.initMapEntry(EnumClasses.GMLRelTypes.NAME.toString());

        // TODO more ADDRESS node types
        this.initMapEntry(XALClass.ADDRESS.toString());
        this.initMapEntry(XALClass.THOROUGHFARE.toString());
        this.initMapEntry(XALClass.THOROUGHFARE_NAME.toString());
        this.initMapEntry(XALClass.THOROUGHFARE_NUMBER.toString());
        this.initMapEntry(XALClass.THOROUGHFARE_NUMBER_SUFFIX.toString());
        this.initMapEntry(XALClass.POSTAL_CODE_NUMBER.toString());
        this.initMapEntry(XALClass.POSTAL_CODE.toString());
        this.initMapEntry(XALClass.COUNTRY.toString());

        // TODO more APPEARANCE node types
        this.initMapEntry(CityGMLClass.APPEARANCE.toString());
        this.initMapEntry(CityGMLClass.PARAMETERIZED_TEXTURE.toString());
        this.initMapEntry(CityGMLClass.TEXTURE_COORDINATES.toString());
        this.initMapEntry(CityGMLClass._TEXTURED_SURFACE.toString());
        this.initMapEntry(CityGMLClass.X3D_MATERIAL.toString());
        this.initMapEntry(CityGMLClass.COLOR.toString());
    }

    @Override
    public String getLabel() {
        return "Thematic Changes";
    }
}
