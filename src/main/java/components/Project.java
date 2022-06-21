package components;

import com.google.gson.Gson;
import conf.Conf;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Project {
    // TODO Directory with or without /
    // TODO Auto determine consumers
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    public static Conf conf = null;

    public static void init(String filename) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        logger.info("Project configurations read from file `conf.json`:\n{}", content);
        Gson gson = new Gson();
        conf = gson.fromJson(new JSONObject(content).toString(), Conf.class);
    }
}
