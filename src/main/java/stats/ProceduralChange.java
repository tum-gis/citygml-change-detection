package stats;

import logger.LogUtil;
import mapper.EnumClasses;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

import java.util.HashMap;
import java.util.logging.Logger;

public class ProceduralChange extends Change {

    public ProceduralChange() {
        super();
        this.map.put("id", new Long(0));
        this.map.put("creationDate", new Long(0));
    }

    @Override
    public void printMap(Logger logger) {
        LogUtil.logMap(logger, this.map, "Procedural Changes");
    }
}
