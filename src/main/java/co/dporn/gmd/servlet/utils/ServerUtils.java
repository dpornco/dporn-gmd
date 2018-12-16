package co.dporn.gmd.servlet.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class ServerUtils {

	protected static String createUrlWithQuerystring(String url, Map<String, String> queryParams)
			throws UnsupportedEncodingException {
		if (queryParams == null) {
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

	public static ResponseWithHeaders putFile(String url, File file, Map<String, String> params) throws IOException {
		try (FileInputStream is = new FileInputStream(file)){
			return putStream(url, is, params);
		}
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
			copyStream(is, urlConnection.getOutputStream());
			try (InputStream inputStream = urlConnection.getInputStream()) {
				response.setBody(getString(inputStream));
				Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
				for (String header : headerFields.keySet()) {
					List<String> value = headerFields.get(header);
					String lcHeader = header == null ? null : header.toLowerCase().trim();
					response.getHeaders().put(lcHeader, value);
				}
				;
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
			copyStream(is, urlConnection.getOutputStream());
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

	public static String get(String url, Map<String, String> params) throws IOException {
		HttpURLConnection urlConnection = null;
		try {
			url = createUrlWithQuerystring(url, params);
			URL endpoint = new URL(url);
			urlConnection = (HttpURLConnection) endpoint.openConnection();
			urlConnection.setRequestProperty("User-Agent", "dpornco.app/2.0");
			try (InputStream inputStream = urlConnection.getInputStream()) {
				return getString(inputStream);
			}
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
	
	/**
	     * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
	     * <code>OutputStream</code>.
	     * <p>
	     * This method uses the provided buffer, so there is no need to use a
	     * <code>BufferedInputStream</code>.
	     * <p>
	     *
	     * @param input the <code>InputStream</code> to read from
	     * @param output the <code>OutputStream</code> to write to
	     * @param buffer the buffer to use for the copy
	     * @return the number of bytes copied
	     * @throws NullPointerException if the input or output is null
	     * @throws IOException          if an I/O error occurs
	     * @since 2.2
	     */
	    public static long copyStream(final InputStream input, final OutputStream output)
	            throws IOException {
	    	byte[] buffer = new byte[4096];
	        long count = 0;
	        int n;
	        while (EOF != (n = input.read(buffer))) {
	            output.write(buffer, 0, n);
	            output.flush();
	            count += n;
	        }
	        return count;
	    }
	    public static final int EOF = -1;
}
