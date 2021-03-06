package co.dporn.gmd.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;

import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.BlogEntryListResponse;
import co.dporn.gmd.shared.BlogEntryResponse;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.CheckEntryResponse;
import co.dporn.gmd.shared.CommentConfirmResponse;
import co.dporn.gmd.shared.DpornCoApi;
import co.dporn.gmd.shared.HtmlSanitizedResponse;
import co.dporn.gmd.shared.IsVerifiedResponse;
import co.dporn.gmd.shared.NotificationsResponse;
import co.dporn.gmd.shared.PingResponse;
import co.dporn.gmd.shared.SuggestTagsResponse;
import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import elemental2.dom.XMLHttpRequest;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;

public class ClientRestClient {

	private static ClientRestClient _instance;

	public static ClientRestClient get() {
		return _instance == null ? _instance = new ClientRestClient() : _instance;
	}

	private String serviceRoot;

	public ClientRestClient() {
		this("/dpornco_application/api/1.0");
	}

	// "/dpornco_application/api/1.0";
	public ClientRestClient(String serviceRoot) {
		this.serviceRoot = serviceRoot;
		Defaults.setServiceRoot(serviceRoot);
		rest = GWT.create(DpornCoRestApi.class);
	}

	public static interface DpornCoRestApi extends DpornCoApi, DirectRestService {

	}

	private final DpornCoRestApi rest;

	public CompletableFuture<IsVerifiedResponse> getIsVerified(String username) {
		MethodCallbackAsFuture<IsVerifiedResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).getIsVerified(username);
		return callback.getFuture();
	}
	
	public CompletableFuture<NotificationsResponse> getNotifications(String username, String authorization) {
		MethodCallbackAsFuture<NotificationsResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).getNotifications(username, authorization);
		return callback.getFuture();
	}
	
	public CompletableFuture<HtmlSanitizedResponse> getHtmlSanitized(String username, String authorization,
			String html) {
		MethodCallbackAsFuture<HtmlSanitizedResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).getHtmlSanitized(username, authorization, html);
		return callback.getFuture();
	}

	public CompletableFuture<CommentConfirmResponse> commentConfirm(String username, String authorization,
			String permlink) {
		MethodCallbackAsFuture<CommentConfirmResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).commentConfirm(username, authorization, permlink);
		return callback.getFuture();
	}

	public CompletableFuture<String> postBlobToIpfs(String username, String authorization, String filename, Blob blob,
			OnprogressFn onprogress) {
		CompletableFuture<String> future = new CompletableFuture<>();
		filename = URL.encode(filename);
		String xhrUrl = serviceRoot + "/ipfs/put/" + filename;
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.upload.onprogress = onprogress;
		xhr.onloadend = (e) -> future.complete(xhr.responseText);
		xhr.onerror = (e) -> future.completeExceptionally(new RuntimeException("IPFS XHR FAILED"));
		xhr.open("PUT", xhrUrl, true);
		xhr.setRequestHeader("Authorization", authorization);
		xhr.setRequestHeader("username", username);
		xhr.send(blob);
		return future;
	}

	public CompletableFuture<String> postBlobToIpfsHlsVideo(String username, String authorization, String filename,
			Blob blob, int videoWidth, int videoHeight, OnprogressFn onprogress) {
		CompletableFuture<String> future = new CompletableFuture<>();
		filename = URL.encode(filename);
		String xhrUrl = serviceRoot + "/ipfs/video/put/" + filename;
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.upload.onprogress = onprogress;
		xhr.onloadend = (e) -> future.complete(xhr.responseText);
		xhr.onreadystatechange = (e) -> {
			return e;
		};
		xhr.onerror = (e) -> {
			future.completeExceptionally(new RuntimeException("IPFS XHR FAILED"));
			return e;
		};
		xhr.open("PUT", xhrUrl);
		xhr.setRequestHeader("Authorization", authorization);
		xhr.setRequestHeader("Username", username);
		xhr.setRequestHeader("videoWidth", String.valueOf(videoWidth));
		xhr.setRequestHeader("videoHeight", String.valueOf(videoHeight));
		xhr.send(blob);
		return future;
	}

	public CompletableFuture<SuggestTagsResponse> suggest(String prefix) {
		MethodCallbackAsFuture<SuggestTagsResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).suggest(prefix);
		return callback.getFuture();
	}

	public CompletableFuture<PingResponse> ping() {
		MethodCallbackAsFuture<PingResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).ping();
		return callback.getFuture();
	}

	public CompletableFuture<ActiveBlogsResponse> listFeatured() {
		MethodCallbackAsFuture<ActiveBlogsResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).blogsRecent();
		return callback.getFuture();
	}

	public CompletableFuture<ActiveBlogsResponse> blogInfo(String username) {
		GWT.log("-> blogInfo: " + username);
		MethodCallbackAsFuture<ActiveBlogsResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).blogInfo(username);
		return callback.getFuture();
	}

	public CompletableFuture<BlogEntryListResponse> listBlogEntries(BlogEntryType entryType, int count) {
		MethodCallbackAsFuture<BlogEntryListResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).blogEntries(entryType, count);
		return callback.getFuture();
	}

	public CompletableFuture<BlogEntryListResponse> listBlogEntries(BlogEntryType entryType, String startId,
			int count) {
		DomGlobal.console.log("listBlogEntries: "+entryType+", startId="+String.valueOf(startId)+", count="+count);
		MethodCallbackAsFuture<BlogEntryListResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).blogEntries(entryType, startId, count);
		return callback.getFuture();
	}

	public CompletableFuture<BlogEntryListResponse> listBlogEntriesFor(String username, int count) {
		MethodCallbackAsFuture<BlogEntryListResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).blogEntriesFor(username, count);
		return callback.getFuture();
	}

	public CompletableFuture<BlogEntryListResponse> listBlogEntriesFor(String username, String startId, int count) {
		MethodCallbackAsFuture<BlogEntryListResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).blogEntriesFor(username, startId, count);
		return callback.getFuture();
	}

	public CompletableFuture<Map<String, String>> embed(String username, String permlink) {
		GWT.log("-> embed html");
		MethodCallbackAsFuture<Map<String, String>> callback = new MethodCallbackAsFuture<>();
		call(callback).embed(username, permlink);
		return callback.getFuture();
	}

	private <T> DpornCoRestApi call(MethodCallbackAsFuture<T> callback) {
		return REST.withCallback(callback.callback()).call(rest);
	}

	public static class MethodCallbackAsFuture<T> {
		private CompletableFuture<T> future = new CompletableFuture<>();

		public CompletableFuture<T> getFuture() {
			return future;
		}

		public MethodCallback<T> callback() {
			return new MethodCallback<T>() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					GWT.log(" [api] onFailure", exception);
					future.completeExceptionally(new RuntimeException(method.getResponse().getStatusText(), exception));
				}

				@Override
				public void onSuccess(Method method, T response) {
					future.complete(response);
				}
			};
		}
	}

	public CompletableFuture<CheckEntryResponse> check(String username, String permlink) {
		GWT.log("-> embed html");
		MethodCallbackAsFuture<CheckEntryResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).check(username, permlink);
		return callback.getFuture();
	}

	public CompletableFuture<BlogEntryResponse> getBlogEntry(String username, String permlink) {
		GWT.log("-> embed html");
		MethodCallbackAsFuture<BlogEntryResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).getBlogEntry(username, permlink);
		return callback.getFuture();
	}
}
