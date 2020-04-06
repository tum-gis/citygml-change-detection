package stats;

import logger.LogUtil;

import java.util.logging.Logger;

public class GeometricChange extends Change {

    public GeometricChange() {
        super();
    }

    @Override
    public void printMap(Logger logger) {
        LogUtil.logMap(logger, this.map, "Geometric Changes");
    }
}
