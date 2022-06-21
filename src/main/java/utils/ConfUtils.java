package utils;

import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ConfUtils {
    private static void jsonToClasses(URL inputJsonUrl, File outputJavaClassDirectory,
                                      String packageName, String javaClassName)
            throws IOException {
        JCodeModel jcodeModel = new JCodeModel();
        GenerationConfig config = new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() {
                return true;
            }

            @Override
            public SourceType getSourceType() {
                return SourceType.JSON;
            }
        };
        SchemaMapper mapper = new SchemaMapper(
                new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()),
                new SchemaGenerator());
        mapper.generate(jcodeModel, javaClassName, packageName, inputJsonUrl);
        jcodeModel.build(outputJavaClassDirectory);
    }

    public static void main(String[] args) throws IOException {
        // Call this after changing the structure of `conf.json` to create Java classes
        jsonToClasses(new File("conf.json").toURI().toURL(), new File("src/main/java"), "conf", "Conf");
    }
}
