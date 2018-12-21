package co.dporn.gmd.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import co.dporn.gmd.servlet.mongodb.MongoDpornCo;
import co.dporn.gmd.shared.BlogEntry;
import co.dporn.gmd.shared.BlogEntryType;

@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.TEXT_HTML)
@Path("/")
public class DpornCoEmbed {
	private static final long _30_DAYS_ms = 1000l * 60l * 60l * 24l * 30l;
	private static final int _30_DAYS_sec = 60 * 60 * 24 * 30;
	private static String htmlTemplateVideo;
	private static String htmlTemplateBlog;

	public static String htmlTemplateVideo() {
		if (htmlTemplateVideo == null) {
			try {
				htmlTemplateVideo = IOUtils.toString(DpornCoEmbed.class.getResourceAsStream("/embed/embed-player.html"));
			} catch (IOException e) {
			}
		}
		return htmlTemplateVideo;
	}
	
	private static String htmlTemplateBlog() {
		if (htmlTemplateBlog == null) {
			try {
				htmlTemplateBlog = IOUtils.toString(DpornCoEmbed.class.getResourceAsStream("/embed/embed-blog.html"));
			} catch (IOException e) {
			}
		}
		return htmlTemplateBlog;
	}

	@Context
	private HttpServletResponse response;
	@Context
	private HttpServletRequest request;
	private static final long LAST_MODIFIED = System.currentTimeMillis();

	private static final Map<String, String> CACHE = Collections.synchronizedMap(new LRUMap<>(256));

	@Produces(MediaType.TEXT_HTML)
	@Path("@{authorname}/{permlink}")
	@HEAD
	public Response head(@PathParam("authorname") String author, @PathParam("permlink") String permlink,
			@QueryParam("base-url") String baseUrl) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.TEXT_HTML);
		response.addDateHeader("Last-Modified", LAST_MODIFIED);
		response.addHeader("Cache-Control", "max-age=" + _30_DAYS_sec + ", must-revalidate");
		response.addDateHeader("Expires", System.currentTimeMillis() + _30_DAYS_ms);
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifModifiedSince >= LAST_MODIFIED) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return Response.notModified().build();
		}
		return Response.ok().build();
	}

	@Produces(MediaType.APPLICATION_JSON)
	@Path("json/@{authorname}/{permlink}")
	@GET
	public Map<String, String> getJson(@PathParam("authorname") String author, @PathParam("permlink") String permlink) {
		Map<String, String> embed = new HashMap<>();
		embed.put("embed", get(author, permlink));
		return embed;
	}

	@Produces(MediaType.TEXT_HTML)
	@Path("@{authorname}/{permlink}")
	@GET
	public String get(@PathParam("authorname") String author, @PathParam("permlink") String permlink) {
		response.setCharacterEncoding("UTF-8");
		response.addDateHeader("Last-Modified", LAST_MODIFIED);
		response.addHeader("Cache-Control", "max-age=" + _30_DAYS_sec + ", must-revalidate");
		response.addDateHeader("Expires", System.currentTimeMillis() + _30_DAYS_ms);
		String embedHtml = getEmbedHtml(author, permlink);
		if (embedHtml == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return "NOT FOUND";
		}
		return embedHtml;
	}

	public static String getEmbedHtml(String author, String permlink) {
		String key = author.trim() + "|" + permlink.trim();
		String cached = CACHE.get(key);
		if (cached != null) {
			return cached;
		}
		BlogEntry entry = MongoDpornCo.getEntry(author, permlink);
		if (entry == null) {
			return null;
		}
		if (!author.equals(entry.getUsername())) {
			return null;
		}
		String embedHtml = null;
		if (BlogEntryType.VIDEO == entry.getEntryType() || entry.getVideoPath()!=null) {
			embedHtml = getVideoEmbedHtml(entry);
		} else {
			embedHtml = getGenericEmbedHtml(entry);
		}
		
		if (embedHtml!=null) {
			CACHE.put(key, embedHtml);
		}
		return embedHtml;
	}

	private static String getGenericEmbedHtml(BlogEntry entry) {
		String content = entry.getContent();
		if (content==null) {
			return null;
		}
		content=content.trim();
		if (content.toLowerCase().startsWith("<html>")) {
			content = content.substring("<html>".length());
		}
		if (content.toLowerCase().endsWith("</html>")) {
			content = content.substring(0, content.length()-"</html>".length());
		}
		String html = htmlTemplateBlog();
		String title = entry.getTitle();
		html = html.replace("__TITLE__",StringEscapeUtils.escapeXml10(title==null?"":title));
		html = html.replace("__CONTENT__",content);
		
		return html;
	}

	private static String getVideoEmbedHtml(BlogEntry entry) {
		String posterImagePath = entry.getPosterImagePath();
		if (posterImagePath == null) {
			return null;
		}
		posterImagePath = posterImagePath.trim();
		String lcPosterImageIpfs = posterImagePath.toLowerCase();
		boolean isPath = lcPosterImageIpfs.startsWith("/ipfs/");
		boolean isHttp = lcPosterImageIpfs.startsWith("http://");
		boolean isHttps = lcPosterImageIpfs.startsWith("https://");
		if (!isPath && !isHttp && !isHttps) {
			posterImagePath = "/ipfs/QmTTtAi3ZwLdGpEzy2LHpFKQHFqLUrHy61miko9LSbVp72";
		}
		String videoPath = entry.getVideoPath();
		if (videoPath == null) {
			return null;
		}
		videoPath = videoPath.trim();
		if (!videoPath.startsWith("/ipfs/")) {
			return null;
		}
		String embedHtml = htmlTemplateVideo();
		embedHtml = embedHtml.replace("__TITLE__", StringEscapeUtils.escapeXml10(entry.getTitle()));
		embedHtml = embedHtml.replace("__POSTERPATH__", StringEscapeUtils.escapeXml10(posterImagePath));
		embedHtml = embedHtml.replace("__VIDEOPATH__", StringEscapeUtils.escapeXml10(videoPath));
		// hack to deal with non IPFS path images or videos
		if (isHttp || isHttps) {
			embedHtml = embedHtml.replace("https://steemitimages.com/400x400/https://ipfs.dporn.co/ipfs" + posterImagePath,
					"https://steemitimages.com/400x400/" + posterImagePath);
		}
		return embedHtml;
	}

}
