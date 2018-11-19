package co.dporn.gmd.servlet.utils;

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

import org.apache.commons.io.IOUtils;


public class ServerRestClient {

	protected static String createUrlWithQuerystring(String url, Map<String, String> queryParams)
			throws UnsupportedEncodingException {
		if (queryParams==null) {
			return url;
		}
		boolean isFirst = true;
		StringBuilder sb = new StringBuilder(url);
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

	public static ResponseWithHeaders putStream(String url, InputStream is, Map<String, String> params) {
		ResponseWithHeaders response = new ResponseWithHeaders();
		HttpURLConnection urlConnection = null;
		try {
			url = createUrlWithQuerystring(url, params);
			URL endpoint = new URL(url);
			urlConnection = (HttpURLConnection) endpoint.openConnection();
			urlConnection.setRequestMethod("PUT");
			urlConnection.setRequestProperty("User-Agent", "dpornco.app/2.0");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			IOUtils.copy(is, urlConnection.getOutputStream());
			try (InputStream inputStream = urlConnection.getInputStream()) {
				response.setBody(getString(inputStream));
				response.getHeaders().putAll(urlConnection.getHeaderFields());
				return response;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			urlConnection.disconnect();
		}
	}
	
	public static ResponseWithHeaders postStream(String url, InputStream is, Map<String, String> params) {
		ResponseWithHeaders response = new ResponseWithHeaders();
		HttpURLConnection urlConnection = null;
		try {
			url = createUrlWithQuerystring(url, params);
			URL endpoint = new URL(url);
			urlConnection = (HttpURLConnection) endpoint.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("User-Agent", "dpornco.app/2.0");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			IOUtils.copy(is, urlConnection.getOutputStream());
			try (InputStream inputStream = urlConnection.getInputStream()) {
				response.setBody(getString(inputStream));
				response.getHeaders().putAll(urlConnection.getHeaderFields());
				return response;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			urlConnection.disconnect();
		}
	}
	
	public static ResponseWithHeaders delete(String url, Map<String, String> params) {
		ResponseWithHeaders response = new ResponseWithHeaders();
		HttpURLConnection urlConnection = null;
		try {
			url = createUrlWithQuerystring(url, params);
			URL endpoint = new URL(url);
			urlConnection = (HttpURLConnection) endpoint.openConnection();
			urlConnection.setRequestMethod("DELETE");
			urlConnection.setRequestProperty("User-Agent", "dpornco.app/2.0");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			try (InputStream inputStream = urlConnection.getInputStream()) {
				response.setBody(getString(inputStream));
				response.getHeaders().putAll(urlConnection.getHeaderFields());
				return response;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			urlConnection.disconnect();
		}
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
	
	public static ResponseWithHeaders head(String url, Map<String, String> params) {
		HttpURLConnection urlConnection = null;
		try {
			url = createUrlWithQuerystring(url, params);
			URL endpoint = new URL(url);
			urlConnection = (HttpURLConnection) endpoint.openConnection();
			urlConnection.setRequestProperty("User-Agent", "dpornco.app/2.0");
			try (InputStream inputStream = urlConnection.getInputStream()) {
				ResponseWithHeaders response = new ResponseWithHeaders();
				response.setBody(getString(inputStream));
				response.getHeaders().putAll(urlConnection.getHeaderFields());
				return response;
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
