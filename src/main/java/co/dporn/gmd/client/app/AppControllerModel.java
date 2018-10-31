package co.dporn.gmd.client.app;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import co.dporn.gmd.shared.Post;
import co.dporn.gmd.shared.SortField;

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

	CompletableFuture<List<Post>> listPosts(int i, SortField byDate);

}
