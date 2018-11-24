package co.dporn.gmd.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;

import co.dporn.gmd.servlet.mongodb.MongoDpornoCo;
import co.dporn.gmd.servlet.utils.ResponseWithHeaders;
import co.dporn.gmd.servlet.utils.ServerRestClient;
import co.dporn.gmd.servlet.utils.ServerSteemConnect;
import co.dporn.gmd.servlet.utils.SteemJInstance;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.DpornCoApi;
import co.dporn.gmd.shared.IpfsHashResponse;
import co.dporn.gmd.shared.PingResponse;
import co.dporn.gmd.shared.Post;
import co.dporn.gmd.shared.PostListResponse;
import co.dporn.gmd.shared.SuggestTagsResponse;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("1.0")
public class DpornCoApiImpl implements DpornCoApi {

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;
	@Context
	protected HttpHeaders headers;

	@Override
	public PingResponse ping() {
		return new PingResponse(true);
	}

	@Override
	public PostListResponse posts(String startId, int count) {
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		List<Post> posts = MongoDpornoCo.listPosts(startId, count);
		Set<String> accountNameList = new HashSet<>();
		Set<String> blacklist = new HashSet<>(SteemJInstance.get().getBlacklist());
		posts.forEach(p -> {
			if (blacklist.contains(p.getAuthor())) {
				p.setCoverImage(null);
				p.setPosterImagePath(null);
				p.setPermlink(null);
				p.setScore(-1000);
				p.setTitle(null);
				p.setVideoPath(null);
			}
		});
		posts.forEach(p -> accountNameList.add(p.getAuthor()));
		Map<String, AccountInfo> infoMap = SteemJInstance.get().getBlogDetails(accountNameList);
		PostListResponse response = new PostListResponse();
		response.setPosts(posts);
		response.setInfoMap(infoMap);
		return response;
	}

	@Override
	public PostListResponse posts(int count) {
		return posts("", count);
	}

	@Override
	public ActiveBlogsResponse blogsRecent() {
		List<String> active = SteemJInstance.get().getActiveNsfwVerifiedList();
		List<String> sublist = active.subList(0, Math.min(active.size(), 16));
		ActiveBlogsResponse activeBlogsResponse = new ActiveBlogsResponse(sublist);
		activeBlogsResponse.setInfoMap(SteemJInstance.get().getBlogDetails(sublist));
		return activeBlogsResponse;
	}

	@Override
	public PostListResponse postsFor(String username, String startId, int count) {
		PostListResponse response = new PostListResponse();
		Set<String> blacklist = new HashSet<>(SteemJInstance.get().getBlacklist());
		if (blacklist.contains(username)) {
			response.setInfoMap(new HashMap<>());
			response.setPosts(new ArrayList<>());
			return response;
		}
		System.out.println("=== postFor: " + username);
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		List<Post> posts = MongoDpornoCo.listPostsFor(username, startId, count);
		Set<String> accountNameList = new HashSet<>();
		posts.forEach(p -> accountNameList.add(p.getAuthor()));
		Map<String, AccountInfo> infoMap = SteemJInstance.get().getBlogDetails(accountNameList);
		response.setPosts(posts);
		response.setInfoMap(infoMap);
		return response;
	}

	@Override
	public PostListResponse postsFor(String username, int count) {
		return postsFor(username, "", count);
	}

	@Override
	public Map<String, String> embed(String author, String permlink) {
		Map<String, String> embed = new HashMap<>();
		embed.put("embed", DpornCoEmbed.getEmbedHtml(author, permlink));
		return embed;
	}

	@Override
	public ActiveBlogsResponse blogInfo(String username) {
		System.out.println("=== blogInfo: " + username);

		ActiveBlogsResponse response = new ActiveBlogsResponse();
		Set<String> blacklist = new HashSet<>(SteemJInstance.get().getBlacklist());
		if (blacklist.contains(username)) {
			response.setAuthors(new ArrayList<>());
			response.setInfoMap(new HashMap<>());
			return response;
		}
		response.setInfoMap(SteemJInstance.get().getBlogDetails(Arrays.asList(username)));
		response.setAuthors(new ArrayList<>(response.getInfoMap().keySet()));
		return response;
	}

	@Override
	public SuggestTagsResponse suggest(String tag) {
		if (tag == null) {
			tag = "";
		}
		tag = tag.trim().toLowerCase();
		SuggestTagsResponse response = new SuggestTagsResponse();
		response.setTags(MongoDpornoCo.getMatchingTags(tag));
		return response;
	}

	@Override
	public SuggestTagsResponse suggest() {
		return suggest("");
	}

	private static long _counter = System.currentTimeMillis();

	private static synchronized long nextCounter() {
		return (_counter = Math.max(_counter + 1, System.currentTimeMillis()));
	}

	protected String safeFilename(String filename) {
		filename = filename.trim();
		filename = filename.replace(" ", "-");
		filename = filename.toLowerCase();
		filename = filename.replaceAll("[^a-z0-9\\.-_]", "");
		filename = filename.replaceAll("-+", "-");
		if (filename.trim().isEmpty()) {
			filename = String.valueOf(nextCounter());
		}
		return filename;
	}

	private boolean isAuthorized(String username, String authorization) {
		if (username == null) {
			System.err.println("isAuthorized: username is null");
			return false;
		}
		String meUsername = ServerSteemConnect.username(authorization);
		System.err.println("isAuthorized: meUsername = " + meUsername);
		boolean authorized = username.equalsIgnoreCase(meUsername);
		return authorized;
	}

	private void setResponseAsUnauthorized() {
		response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
		response.setContentType(MediaType.TEXT_PLAIN);
		try {
			response.getWriter().println("NOT AUTHORIZED");
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
		}
	}

	private static final String IPFS_EMPTY_DIR = "QmUNLLsPACCz1vLxQVkXqqLX5R1X345qqfHbsf67hvA3Nn";
	private static final String IPFS_GATEWAY = "http://localhost:8008/ipfs/";

	@Override
	public IpfsHashResponse ipfsPut(InputStream is, String username, String authorization, String filename) {
		if (!isAuthorized(username, authorization)) {
			setResponseAsUnauthorized();
			return null;
		}
		filename = safeFilename(filename);
		ResponseWithHeaders putResponse = ServerRestClient.putStream(IPFS_GATEWAY + IPFS_EMPTY_DIR + "/" + filename, is,
				null);
		List<String> ipfsHashes = putResponse.getHeaders().get("ipfs-hash");
		List<String> locations = putResponse.getHeaders().get("location");
		IpfsHashResponse response = new IpfsHashResponse();
		response.setFilename(filename);
		if (!ipfsHashes.isEmpty()) {
			response.setIpfsHash(ipfsHashes.get(ipfsHashes.size() - 1));
		}
		if (!locations.isEmpty()) {
			response.setLocation(locations.get(locations.size() - 1));
		}
		return response;
	}
}
