package co.dporn.gmd.servlet;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
	public PostListResponse posts(int page) {
		List<Post> posts = MongoDpornoCo.listPosts(page);
		PostListResponse response = new PostListResponse();
		response.setPosts(posts);
		return response;
	}

	@Override
	public ActiveBlogsResponse blogsRecent() {
		List<String> active = SteemJInstance.getActiveNsfwVerifiedList();
		List<String> sublist = active.subList(0, Math.min(active.size(), 16));
		ActiveBlogsResponse activeBlogsResponse = new ActiveBlogsResponse(sublist);
		activeBlogsResponse.setInfoMap(SteemJInstance.getBlogDetails(sublist));
		return activeBlogsResponse;
	}
}
