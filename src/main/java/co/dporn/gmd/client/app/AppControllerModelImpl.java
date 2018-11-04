package co.dporn.gmd.client.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import co.dporn.gmd.client.RestClient;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.Post;
import co.dporn.gmd.shared.PostListResponse;
import steem.SteemApi;
import steem.model.Vote;

public class AppControllerModelImpl implements AppControllerModel {
	private static final int FEATURED_POST_POOL_SIZE = 16;
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

	@Override
	public CompletableFuture<PostListResponse> featuredPosts(int count) {
		CompletableFuture<PostListResponse> finalFuture=new CompletableFuture<>();
		listPosts(FEATURED_POST_POOL_SIZE).thenAccept((response)->{
			List<CompletableFuture<List<Vote>>> voteFutures = new ArrayList<>(); 
			List<Post> list = new ArrayList<>();
			double mul = 1.0d;
			for (int ix=0; ix< response.getPosts().size(); ix++) {
				mul = mul * .9d;
				final double weight = mul;
				Post post=response.getPosts().get(ix);
				CompletableFuture<List<Vote>> voteFuture = SteemApi.getActiveVotes(post.getAuthor(), post.getPermlink());
				voteFuture.thenAccept((v)->{
					post.setScore((double)v.size()*weight);
					synchronized (list) {
						list.add(post);
					}
				});
				voteFutures.add(voteFuture);
			}
			CompletableFuture.allOf(voteFutures.toArray(new CompletableFuture<?>[0])).thenRun(()->{
				//desc by score
				Collections.sort(response.getPosts(), (a,b)->-Double.compare(a.getScore(), b.getScore()));
				int size = response.getPosts().size();
				response.getPosts().subList(Math.min(count, size), size).clear();
				deferred(()->finalFuture.complete(response));
			});
		});
		return finalFuture;
	}

	private void deferred(ScheduledCommand cmd) {
		Scheduler.get().scheduleDeferred(cmd);
	}

}
