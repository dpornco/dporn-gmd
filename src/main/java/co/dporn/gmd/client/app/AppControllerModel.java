package co.dporn.gmd.client.app;

import java.util.concurrent.CompletableFuture;

import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.PostListResponse;

/**
 * MVP: Model: Models encapsulate the data MODEL and LOGIC. Does <b>NOT</b> know
 * or implement anything in regards to presenters, views, or routers! Are
 * usually single instance. Does know how to talk to any APIs such as
 * steem/dporn!
 * 
 * @author muksihs
 *
 */
public interface AppControllerModel {

	CompletableFuture<PostListResponse> listPosts(int count);
	
	CompletableFuture<PostListResponse> listPosts(String startId, int count);

	CompletableFuture<ActiveBlogsResponse> listFeatured();
	
	CompletableFuture<PostListResponse> featuredPosts(int count);
	default CompletableFuture<PostListResponse> featuredPosts() {
		return featuredPosts(4);
	}

}
