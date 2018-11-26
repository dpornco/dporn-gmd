package co.dporn.gmd.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

import co.dporn.gmd.servlet.mongodb.MongoDpornCo;
import co.dporn.gmd.servlet.utils.Mapper;
import co.dporn.gmd.servlet.utils.ResponseWithHeaders;
import co.dporn.gmd.servlet.utils.ServerRestClient;
import co.dporn.gmd.servlet.utils.ServerSteemConnect;
import co.dporn.gmd.servlet.utils.steemj.DpornMetadata;
import co.dporn.gmd.servlet.utils.steemj.SJCommentMetadata;
import co.dporn.gmd.servlet.utils.steemj.SteemJInstance;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.BlogEntry;
import co.dporn.gmd.shared.BlogEntryResponse;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.CommentConfirmResponse;
import co.dporn.gmd.shared.DpornCoApi;
import co.dporn.gmd.shared.IpfsHashResponse;
import co.dporn.gmd.shared.MongoDate;
import co.dporn.gmd.shared.PingResponse;
import co.dporn.gmd.shared.PostListResponse;
import co.dporn.gmd.shared.SuggestTagsResponse;
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion;

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
	public PostListResponse posts(BlogEntryType entryType, String startId, int count) {
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		List<BlogEntry> posts = MongoDpornCo.listEntries(entryType, startId, count);
		Set<String> accountNameList = new HashSet<>();
		Set<String> blacklist = new HashSet<>(SteemJInstance.get().getBlacklist());
		posts.forEach(p -> {
			if (blacklist.contains(p.getUsername())) {
				p.setPosterImagePath(null);
				p.setPosterImagePath(null);
				p.setPermlink(null);
				p.setScore(-1000);
				p.setTitle(null);
				p.setVideoPath(null);
			}
		});
		posts.forEach(p -> accountNameList.add(p.getUsername()));
		Map<String, AccountInfo> infoMap = SteemJInstance.get().getBlogDetails(accountNameList);
		PostListResponse response = new PostListResponse();
		response.setPosts(posts);
		response.setInfoMap(infoMap);
		return response;
	}

	@Override
	public PostListResponse posts(BlogEntryType entryType, int count) {
		return posts(entryType, "", count);
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
		List<BlogEntry> posts = MongoDpornCo.listEntriesFor(username, startId, count);
		Set<String> accountNameList = new HashSet<>();
		posts.forEach(p -> accountNameList.add(p.getUsername()));
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
		try {
			response.setInfoMap(SteemJInstance.get().getBlogDetails(Arrays.asList(username)));
		} catch (Exception e) {
			return response;
		}
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
		response.setTags(MongoDpornCo.getMatchingTags(tag));
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

	@Override
	public CommentConfirmResponse commentConfirm(String username, String authorization, String permlink) {
		if (!isAuthorized(username, authorization)) {
			setResponseAsUnauthorized();
			return null;
		}
		if (username.equals(MongoDpornCo.getEntry(username, permlink).getUsername())) {
			return new CommentConfirmResponse(true);
		}
		;
		Discussion content = SteemJInstance.get().getContent(username, permlink);
		SJCommentMetadata metadata;
		try {
			metadata = Mapper.get().readValue(content.getJsonMetadata(), SJCommentMetadata.class);
		} catch (IOException e) {
			e.printStackTrace();
			return new CommentConfirmResponse(false);
		}
		if (metadata == null) {
			return new CommentConfirmResponse(false);
		}
		DpornMetadata dpornMetadata = metadata.getDpornMetadata();
		if (dpornMetadata == null) {
			return new CommentConfirmResponse(false);
		}

		BlogEntry entry = new BlogEntry();
		Set<String> extractedTags = new LinkedHashSet<>(Arrays.asList(metadata.getTags()));
		extractedTags.add("@" + username);

		entry.setId(null);
		entry.setTitle(content.getTitle());
		entry.setPermlink(permlink);
		entry.setContent(content.getBody());
		entry.setPostTags(new ArrayList<>(extractedTags));
		entry.setCommunityTags(new ArrayList<>(extractedTags));
		entry.setGalleryImagePaths(dpornMetadata.getPhotoGalleryImagePaths());
		entry.setVideoPath(dpornMetadata.getVideoPath());
		entry.setPosterImagePath(dpornMetadata.getPosterImagePath());
		entry.setUsername(username);
		entry.setCommentJsonMetadata(content.getJsonMetadata());
		entry.setCreated(new MongoDate(content.getCreated().getDateTimeAsDate()));
		entry.setModified(entry.getCreated());
		entry.setEntryType(dpornMetadata.getBlogEntryType());
		entry.setGalleryImageThumbPaths(dpornMetadata.getPhotoGalleryImagePaths());
		entry.setMigrated(false);
		entry.setApp(dpornMetadata.getApp());
		entry.setEmbed(dpornMetadata.getEmbed());

		return new CommentConfirmResponse(MongoDpornCo.insertEntry(entry));
	}

	@Override
	public void check(String username, String permlink) {
		if (username == null || permlink == null) {
			return;
		}
		if (username.trim().isEmpty() || permlink.trim().isEmpty()) {
			return;
		}
		username = username.toLowerCase().trim();
		permlink = permlink.trim();
		BlogEntry entry = MongoDpornCo.getEntry(username, permlink);
		if (entry==null || !username.equals(entry.getUsername())) {
			return;
		}
		synchronized (DpornCoApiImpl.class) {
			System.out.println("=== CHECK: " + username + " | " + permlink);
			Discussion content;
			try {
				content = SteemJInstance.get().getContent(username, permlink);
				if (content == null || content.getAuthor() == null || !username.equals(content.getAuthor().getName())) {
					System.err.println("BAD ENTRY: " + username + " | " + permlink);
					MongoDpornCo.deleteEntry(username, permlink);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public BlogEntryResponse blogEntry(String username, String permlink) {
		BlogEntryResponse response = new BlogEntryResponse();
		response.setBlogEntry(MongoDpornCo.getEntry(username, permlink));
		return response;
	}
}
