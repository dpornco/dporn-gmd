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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import co.dporn.gmd.shared.Post;

@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.TEXT_HTML)
@Path("/")
public class DpornCoEmbed {
	private static String _template;

	private static String template() {
		if (_template == null) {
			try {
				_template = IOUtils.toString(DpornCoEmbed.class.getResourceAsStream("/embed/embed-player.html"));
			} catch (IOException e) {
			}
		}
		return _template;
	}

	@Context
	private HttpServletResponse response;
	@Context
	private HttpServletRequest request;
	private static final long LAST_MODIFIED = System.currentTimeMillis();

	private static Map<String, String> cache = Collections.synchronizedMap(new HashMap<>());

	@Produces(MediaType.TEXT_HTML)
	@Path("@{authorname}/{permlink}")
	@HEAD
	public Response head(@PathParam("authorname") String author, @PathParam("permlink") String permlink,
			@QueryParam("base-url") String baseUrl) {
		String key = author.trim() + "|" + permlink.trim();
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.TEXT_HTML);
		response.addDateHeader("Last-Modified", LAST_MODIFIED);
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifModifiedSince >= LAST_MODIFIED) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			System.out.print("CLIENT CACHED IFRAME: " + key);
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
		String embedHtml = getEmbedHtml(author, permlink);
		if (embedHtml == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return "NOT FOUND";
		}
		return embedHtml;
	}

	public static String getEmbedHtml(String author, String permlink) {
		String key = author.trim() + "|" + permlink.trim();
		synchronized (cache) {
			if (cache.containsKey(key)) {
				return cache.get(key);
			}
		}
		Post post = MongoDpornoCo.getPost(author, permlink);
		if (post == null) {
			return null;
		}
		if (!author.equals(post.getAuthor())) {
			return null;
		}
		String coverImageIpfs = post.getCoverImageIpfs();
		if (coverImageIpfs == null) {
			return null;
		}
		coverImageIpfs = coverImageIpfs.trim();
		if (coverImageIpfs.length() != 46) {
			return null;
		}
		String videoIpfs = post.getVideoIpfs();
		if (videoIpfs == null) {
			return null;
		}
		videoIpfs = videoIpfs.trim();
		if (videoIpfs.length() != 46) {
			return null;
		}
		String embedHtml = template();
		embedHtml = embedHtml.replace("__TITLE__", StringEscapeUtils.escapeXml10(post.getTitle()));
		embedHtml = embedHtml.replace("__POSTERHASH__", StringEscapeUtils.escapeXml10(coverImageIpfs));
		embedHtml = embedHtml.replace("__VIDEOHASH__", StringEscapeUtils.escapeXml10(videoIpfs));
		synchronized (cache) {
			if (cache.size() > 1024) {
				cache.clear();
			}
			cache.put(key, embedHtml);
		}
		return embedHtml;
	}

}
