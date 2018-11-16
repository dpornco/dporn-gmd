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
import co.dporn.gmd.shared.DpornCoApi;
import co.dporn.gmd.shared.PingResponse;
import co.dporn.gmd.shared.PostListResponse;
import co.dporn.gmd.shared.SuggestTagsResponse;
import elemental2.dom.Blob;
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

	public CompletableFuture<SuggestTagsResponse> suggest(String prefix) {
		GWT.log("-> suggest: " + prefix);
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
		GWT.log("-> listFeatured");
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

	public CompletableFuture<PostListResponse> posts(int count) {
		GWT.log("-> most recent posts");
		MethodCallbackAsFuture<PostListResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).posts(count);
		return callback.getFuture();
	}

	public CompletableFuture<PostListResponse> posts(String startId, int count) {
		GWT.log("-> posts starting at");
		MethodCallbackAsFuture<PostListResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).posts(startId, count);
		return callback.getFuture();
	}

	public CompletableFuture<PostListResponse> postsFor(String username, int count) {
		GWT.log("-> most recent posts for " + username);
		MethodCallbackAsFuture<PostListResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).postsFor(username, count);
		return callback.getFuture();
	}

	public CompletableFuture<PostListResponse> postsFor(String username, String startId, int count) {
		GWT.log("-> most recent posts starting at for " + username);
		MethodCallbackAsFuture<PostListResponse> callback = new MethodCallbackAsFuture<>();
		call(callback).postsFor(username, startId, count);
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
					GWT.log(" [api] onSuccess");
					future.complete(response);
				}
			};
		}
	}
}
