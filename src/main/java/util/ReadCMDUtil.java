package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class ReadCMDUtil {

	public static final String KEY_SETTINGS_LOCATION = "SETTINGS";
	public static final String KEY_VALUE_SEPARATOR = "=";

	public static Map<String, String> ARGUMENTS;

	public static void parseCommandLineArguments(String filename) throws IOException {
		Map<String, String> args = new HashMap<String, String>();

		BufferedReader br = new BufferedReader(new FileReader(filename.replace("\\", "\\\\")));
		try {
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty() || line.trim().charAt(0) == '#') {
					// comment
					continue;
				}

				String[] array = line.split(KEY_VALUE_SEPARATOR);
				String key = array[0];
				String value = array.length == 1 ? "" : array[1];
				args.put(key, value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}

		ARGUMENTS = args;
	}

	public static void readCommandLindArguments(String[] args) throws IOException {
		for (String arg : args) {
			if (arg.contains(KEY_SETTINGS_LOCATION)) {
				parseCommandLineArguments(arg.split(KEY_VALUE_SEPARATOR)[1]);
				return;
			}
		}

		parseCommandLineArguments("saved_settings/Default.txt");
	}
}
