package co.dporn.gmd.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.DpornCoApi;
import co.dporn.gmd.shared.PingResponse;
import co.dporn.gmd.shared.Post;
import co.dporn.gmd.shared.PostListResponse;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("1.0")
public class DpornCoApiImpl implements DpornCoApi {

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
		Set<String> blacklist = new HashSet<>(SteemJInstance.getBlacklist());
		posts.forEach(p->{
			if (blacklist.contains(p.getAuthor())) {
				p.setCoverImage(null);
				p.setCoverImageIpfs(null);
				p.setPermlink(null);
				p.setScore(-1000);
				p.setTitle(null);
				p.setVideoIpfs(null);
			}
		});
		posts.forEach(p -> accountNameList.add(p.getAuthor()));
		Map<String, AccountInfo> infoMap = SteemJInstance.getBlogDetails(accountNameList);
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
		List<String> active = SteemJInstance.getActiveNsfwVerifiedList();
		List<String> sublist = active.subList(0, Math.min(active.size(), 16));
		ActiveBlogsResponse activeBlogsResponse = new ActiveBlogsResponse(sublist);
		activeBlogsResponse.setInfoMap(SteemJInstance.getBlogDetails(sublist));
		return activeBlogsResponse;
	}

	@Override
	public PostListResponse postsFor(String username, String startId, int count) {
		PostListResponse response = new PostListResponse();
		Set<String> blacklist = new HashSet<>(SteemJInstance.getBlacklist());
		if (blacklist.contains(username)) {
			response.setInfoMap(new HashMap<>());
			response.setPosts(new ArrayList<>());
			return response;
		}
		System.out.println("=== postFor: "+username);
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		List<Post> posts = MongoDpornoCo.listPostsFor(username, startId, count);
		Set<String> accountNameList = new HashSet<>();
		posts.forEach(p -> accountNameList.add(p.getAuthor()));
		Map<String, AccountInfo> infoMap = SteemJInstance.getBlogDetails(accountNameList);
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

	//TODO: implement short term memory cache
	@Override
	public ActiveBlogsResponse blogInfo(String username) {
		System.out.println("=== blogInfo: "+username);
		ActiveBlogsResponse response = new ActiveBlogsResponse();
		Set<String> blacklist = new HashSet<>(SteemJInstance.getBlacklist());
		if (blacklist.contains(username)) {
			response.setAuthors(new ArrayList<>());
			response.setInfoMap(new HashMap<>());
			return response;
		}
		response.setInfoMap(SteemJInstance.getBlogDetails(Arrays.asList(username)));
		response.setAuthors(new ArrayList<>(response.getInfoMap().keySet()));
		return response;
	}
}
