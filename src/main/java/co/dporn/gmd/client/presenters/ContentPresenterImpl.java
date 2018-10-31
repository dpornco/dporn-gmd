package co.dporn.gmd.client.presenters;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.shared.Post;
import co.dporn.gmd.shared.SortField;

public class ContentPresenterImpl implements ContentPresenter {

	private ContentView view;
	private AppControllerModel model;

	public ContentPresenterImpl(AppControllerModel model, ContentView view) {
		this.view=view;
		this.model=model;
	}
	

	@Override
	public void setView(ContentView view) {
		this.view=view;
		loadInitialPosts();
	}

	private void loadInitialPosts() {
		CompletableFuture<List<Post>> futurePosts = model.listPosts(0, SortField.BY_DATE);
	}


	@Override
	public ContentView getContentView() {
		return view;
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model=model;
		loadInitialPosts();
	}
}
