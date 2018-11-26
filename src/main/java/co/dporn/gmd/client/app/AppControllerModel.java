package co.dporn.gmd.client.app;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import co.dporn.gmd.client.presenters.RoutePresenter;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.BlogEntry;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.PostListResponse;
import co.dporn.gmd.shared.TagSet;
import elemental2.dom.Blob;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;
import steem.model.DiscussionComment;

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

	//CompletableFuture<PostListResponse> listPosts(int count);
	CompletableFuture<PostListResponse> listPosts(BlogEntryType entryType, int count);
	
	//CompletableFuture<PostListResponse> listPosts(String startId, int count);
	CompletableFuture<PostListResponse> listPosts(BlogEntryType entryType, String startId, int count);

	CompletableFuture<ActiveBlogsResponse> listFeatured();
	CompletableFuture<ActiveBlogsResponse> blogInfo(String username);
	
	CompletableFuture<PostListResponse> featuredPosts(int count);
	default CompletableFuture<PostListResponse> featuredPosts() {
		return featuredPosts(4);
	}

	CompletableFuture<PostListResponse> postsFor(String username);

	CompletableFuture<PostListResponse> postsFor(String username, String startId, int count);

	CompletableFuture<DiscussionComment> getDiscussionComment(String username, String permlink);
	
	CompletableFuture<BlogEntry> getBlogEntry(String username, String permlink);
	
	CompletableFuture<Void> autoLogin();

	boolean isLoggedIn();

	void login();

	void fireRouteState();

	void setRoutePresenter(RoutePresenter presenter);

	void showAccountSettings();

	void logout();

	CompletableFuture<List<String>> tagsOracle(String query, int limit);

	CompletableFuture<String> postBlobToIpfsFile(String filename, Blob blob, OnprogressFn onprogress);
	
	CompletableFuture<List<TagSet>> recentTagSets(String mustHaveTag);
	
	CompletableFuture<List<String>> sortTagsByNetVoteDesc(List<String> tags);
	
	CompletableFuture<String> postBlogEntry(BlogEntryType erotica, double width, String title, List<String> tags, String content);
	
}
