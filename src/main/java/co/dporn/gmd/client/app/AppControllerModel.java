package co.dporn.gmd.client.app;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import co.dporn.gmd.client.presenters.RoutePresenter;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.BlogEntry;
import co.dporn.gmd.shared.BlogEntryListResponse;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.HtmlSanitizedResponse;
import co.dporn.gmd.shared.NotificationsResponse;
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

	CompletableFuture<BlogEntryListResponse> listBlogEntries(BlogEntryType blogEntryType, int count);
	CompletableFuture<BlogEntryListResponse> listBlogEntries(BlogEntryType blogEntryType, String startId, int count);
	CompletableFuture<ActiveBlogsResponse> listFeaturedBlogs();
	CompletableFuture<ActiveBlogsResponse> getBlogInfo(String username);
	CompletableFuture<HtmlSanitizedResponse> getHtmlSanitized(String html);
	
	CompletableFuture<BlogEntryListResponse> listFeaturedBlogEntries(int count);
	default CompletableFuture<BlogEntryListResponse> listFeaturedBlogEntries() {
		return listFeaturedBlogEntries(4);
	}

	CompletableFuture<BlogEntryListResponse> listBlogEntriesFor(String username);

	CompletableFuture<BlogEntryListResponse> listBlogEntriesFor(String username, String startId, int count);

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
	CompletableFuture<String> postBlobToIpfsHlsVideo(String filename, Blob blob, int videoWidth, int videoHeight, OnprogressFn onprogress);
	
	CompletableFuture<List<TagSet>> recentTagSets(String mustHaveTag);
	
	CompletableFuture<List<String>> sortTagsByNetVoteDesc(List<String> tags);
	
	CompletableFuture<String> newBlogEntry(BlogEntryType blogEntryType, double width, String title, List<String> tags, String content);
	CompletableFuture<String> newBlogEntry(BlogEntryType blogEntryType, double width, String title, List<String> tags,
			String content, String posterImage, String videoLink, List<String> photoGalleryImages);
	CompletableFuture<NotificationsResponse> getNotifications();
	CompletableFuture<Boolean> isVerified();
	
}
