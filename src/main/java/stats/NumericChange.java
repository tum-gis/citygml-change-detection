package stats;

import org.citygml4j.model.gml.GMLClass;

public class NumericChange extends Change {

    public NumericChange() {
        super();
        this.initMapEntry(GMLClass.MEASURE.toString());
        this.initMapEntry(GMLClass.LENGTH.toString());
    }

    @Override
    public String getLabel() {
        return "Numeric Changes";
    }

}
