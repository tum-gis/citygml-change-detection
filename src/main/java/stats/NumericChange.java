package stats;

import logger.LogUtil;
import mapper.EnumClasses;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

import java.util.logging.Logger;

public class NumericChange extends Change {

    public NumericChange() {
        super();
        this.map.put(GMLClass.MEASURE.toString(), new Long(0));
        this.map.put(GMLClass.LENGTH.toString(), new Long(0));
    }

    @Override
    public void printMap(Logger logger) {
        LogUtil.logMap(logger, this.map, "Numeric Changes");
    }
}
