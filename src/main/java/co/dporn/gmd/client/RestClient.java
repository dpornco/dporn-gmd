package co.dporn.gmd.client;

import java.util.concurrent.CompletableFuture;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.google.gwt.core.client.GWT;

import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.DpornCoApi;
import co.dporn.gmd.shared.PingResponse;
import co.dporn.gmd.shared.PostListResponse;

public class RestClient {
	
	private static RestClient _instance;
	public static RestClient get() {
		return _instance==null?_instance=new RestClient():_instance;
	}

	public RestClient() {
		String serviceRoot = "/dpornco_application/api/1.0";
		Defaults.setServiceRoot(serviceRoot);
		rest = GWT.create(DpornCoRestApi.class);
	}

	public static interface DpornCoRestApi extends DpornCoApi, DirectRestService {
	}

	private final DpornCoRestApi rest;
	
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
