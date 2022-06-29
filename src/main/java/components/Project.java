package components;

import com.google.gson.Gson;
import conf.Conf;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Project {
    // TODO Directory with or without /
    // TODO Auto determine consumers
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    public static Conf conf = null;

    public static void init(String confFilename, String confInfoFilename) throws IOException {
        String content = new String(Files.readAllBytes(Path.of(confFilename)));
        logger.info("Project configurations read from file `conf.json`:\n{}\n{} {}", content,
                "For more information please refer to", confInfoFilename);
        Gson gson = new Gson();
        conf = gson.fromJson(new JSONObject(content).toString(), Conf.class);
    }
}
