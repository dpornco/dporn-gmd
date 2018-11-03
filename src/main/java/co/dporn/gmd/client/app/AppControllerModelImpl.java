package co.dporn.gmd.client.app;

import java.util.concurrent.CompletableFuture;

import co.dporn.gmd.client.RestClient;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.PostListResponse;

public class AppControllerModelImpl implements AppControllerModel {

	@Override
	public CompletableFuture<PostListResponse> listPosts(int count) {
		return RestClient.get().posts(count);
	}
	
	@Override
	public CompletableFuture<PostListResponse> listPosts(String startId, int count) {
		return RestClient.get().posts(startId==null?"":startId, count);
	}
	
	@Override
	public CompletableFuture<ActiveBlogsResponse> listFeatured() {
		return RestClient.get().listFeatured();
	}

}
