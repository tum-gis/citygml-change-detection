package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class ClientUtil {
	private static long count = 1;

	public static void sendHttpPost(String url, StringBuilder xmlContent, Logger logger) throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);

		// Request parameters and other properties.
		// List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		// params.add(new BasicNameValuePair("xml", xmlContent.toString()));
		// httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		httppost.setEntity(new ByteArrayEntity(xmlContent.toString().getBytes("UTF-8")));

		StringBuilder logging = new StringBuilder();
		logging.append("\n--------------------------- " + (count++) + ". HTTP-POST REQUEST ---------------------------\n" + xmlContent.toString() + "\n");

		if (logger != null) {
			logger.info(logging.toString());
		} else {
			System.out.println(logging.toString());
		}

		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream instream = entity.getContent();
			try {
				BufferedReader rd = new BufferedReader(
						new InputStreamReader(instream));

				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line + "\n");
				}

				logging = new StringBuilder();
				logging.append("\n------------------------------- WFS RESPONSE ---------------------------------\n" + result.toString() + "\n");
			} finally {
				instream.close();
			}
		}

		if (logger != null) {
			logger.info(logging.toString());
		} else {
			System.out.println(logging.toString());
		}
	}
}
