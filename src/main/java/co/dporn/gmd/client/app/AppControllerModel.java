
package co.dporn.gmd.client.app;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.GenericEvent;

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
	EventBus getEventBus();
	default void fireEvent(GenericEvent event) {
		getEventBus().fireEvent(event);
	}
	CompletableFuture<Void> autoLogin();
	CompletableFuture<Boolean> doVote(String username, String permlink, int percent);
	void fireRouteState();
	CompletableFuture<BlogEntry> getBlogEntry(String username, String permlink);
	CompletableFuture<ActiveBlogsResponse> getBlogInfo(String username);
	CompletableFuture<DiscussionComment> getDiscussionComment(String username, String permlink);
	CompletableFuture<HtmlSanitizedResponse> getHtmlSanitized(String html);
	CompletableFuture<NotificationsResponse> getNotifications();
	String getTimestampedPermlink(String title);
	String getUsername();
	boolean isLoggedIn();
	CompletableFuture<Boolean> isVerified();
	CompletableFuture<BlogEntryListResponse> listBlogEntries(BlogEntryType blogEntryType, int count);
	CompletableFuture<BlogEntryListResponse> listBlogEntries(BlogEntryType blogEntryType, String startId, int count);
	CompletableFuture<BlogEntryListResponse> listBlogEntriesFor(String username);
	CompletableFuture<BlogEntryListResponse> listBlogEntriesFor(String username, String startId, int count);
	default CompletableFuture<BlogEntryListResponse> listFeaturedBlogEntries() {
		return listFeaturedBlogEntries(4);
	}
	CompletableFuture<BlogEntryListResponse> listFeaturedBlogEntries(int count);
	CompletableFuture<ActiveBlogsResponse> listFeaturedBlogs();
	void login();
	void logout();
	CompletableFuture<String> newBlogEntry(BlogEntryType blogEntryType, double width, String title, List<String> tags, String content);
	CompletableFuture<String> newBlogEntry(BlogEntryType blogEntryType, double width, String title, List<String> tags,
			String content, String posterImage, String videoLink, List<String> photoGalleryImages);
	CompletableFuture<String> newBlogEntry(BlogEntryType blogEntryType, double width, String title, List<String> tags,
			String content, String posterImage, String videoLink, List<String> photoGalleryImages, String permlink);
	CompletableFuture<String> postBlobToIpfsFile(String filename, Blob blob, OnprogressFn onprogress);
	CompletableFuture<String> postBlobToIpfsHlsVideo(String filename, Blob blob, int videoWidth, int videoHeight, OnprogressFn onprogress);
	CompletableFuture<List<TagSet>> recentTagSets(String mustHaveTag);
	void setRoutePresenter(RoutePresenter presenter);
	void showAccountSettings();
	CompletableFuture<List<String>> tagsOracle(String query, int limit);
}
