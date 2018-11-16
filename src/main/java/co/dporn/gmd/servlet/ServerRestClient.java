package co.dporn.gmd.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;


public class ServerRestClient {

	protected static String createUrlWithQuerystring(String endpointUrl, Map<String, String> queryParams)
			throws UnsupportedEncodingException {
		boolean isFirst = true;
		StringBuilder sb = new StringBuilder(endpointUrl);
		Iterator<String> iter = queryParams.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (isFirst) {
				sb.append("?");
				isFirst = false;
			} else {
				sb.append("&");
			}
			sb.append(URLEncoder.encode(key, "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(queryParams.get(key), "UTF-8"));
		}
		return sb.toString();
	}

	public static String get(String url, Map<String, String> params) {
		HttpURLConnection urlConnection = null;
		try {
			url = createUrlWithQuerystring(url, params);
			URL endpoint = new URL(url);
			urlConnection = (HttpURLConnection) endpoint.openConnection();
			urlConnection.setRequestProperty("User-Agent", "dpornco.app/2.0");
			try (InputStream inputStream = urlConnection.getInputStream()) {
				return getString(inputStream);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			urlConnection.disconnect();
		}
	}

	/**
	 * Copy {@link InputStream} into a {@link String}. See <a href=
	 * "https://stackoverflow.com/a/35446009/1341731">https://stackoverflow.com/a/35446009/1341731</a>
	 * for performance comparisons for various ways to do this.
	 * 
	 * @param inputStream
	 * @return The input stream concatenated into a String.
	 * @throws IOException
	 */
	protected static String getString(InputStream inputStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while (result != -1) {
			buf.write((byte) result);
			result = bis.read();
		}
		return buf.toString(StandardCharsets.UTF_8.name());
	}
}
