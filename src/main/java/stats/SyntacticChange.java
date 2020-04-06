package stats;

import logger.LogUtil;
import mapper.EnumClasses;

import java.util.HashMap;
import java.util.logging.Logger;

public class SyntacticChange extends Change {

    public SyntacticChange() {
        super();
    }

    @Override
    public void printMap(Logger logger) {
        LogUtil.logMap(logger, this.map, "Syntactic Changes");
    }
}
