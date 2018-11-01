package co.dporn.gmd.client.app;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import co.dporn.gmd.client.RestClient;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.Post;
import co.dporn.gmd.shared.SortField;

public class AppControllerModelImpl implements AppControllerModel {

	@Override
	public CompletableFuture<List<Post>> listPosts(int i, SortField byDate) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public CompletableFuture<ActiveBlogsResponse> listFeatured() {
		return RestClient.get().listFeatured();
	}

}
