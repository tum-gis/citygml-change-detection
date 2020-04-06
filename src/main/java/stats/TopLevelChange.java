package stats;

import logger.LogUtil;

import java.util.logging.Logger;

public class TopLevelChange extends Change {

    public TopLevelChange() {
        super();
    }

    @Override
    public void printMap(Logger logger) {
        LogUtil.logMap(logger, this.map, "Top-level Changes");
    }
}
