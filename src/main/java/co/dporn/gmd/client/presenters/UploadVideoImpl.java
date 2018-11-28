package co.dporn.gmd.client.presenters;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.utils.HtmlReformatter;
import co.dporn.gmd.client.views.IsView;
import co.dporn.gmd.shared.BlogEntryType;
import elemental2.dom.Blob;
import elemental2.dom.XMLHttpRequest.OnprogressFn;

public class UploadVideoImpl implements UploadVideo {

	private AppControllerModel model;
	private UploadVideoView view;
	private int posX;
	private int posY;

	public UploadVideoImpl(AppControllerModel model, UploadVideoView childView) {
		setModel(model);
		setView(childView);
	}

	@Override
	public IsView<?> getContentView() {
		return view;
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model=model;
	}

	@Override
	public void setView(UploadVideoView view) {
		this.view=view;
		view.bindPresenter(this);
	}

	@Override
	public void saveScrollPosition() {
		posX = Window.getScrollLeft();
		posY = Window.getScrollTop();
	}

	@Override
	public void restoreScrollPosition() {
		Window.scrollTo(posX, posY);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		view.reset();
	}

	@Override
	public CompletableFuture<String> postBlobToIpfsFile(String filename, Blob blob) {
		OnprogressFn onprogressfn = view.getOnprogressFn(filename);
		return model.postBlobToIpfsFile(filename, blob, (e)->onprogressfn.onInvoke(e));
	}

	@Override
	public Void viewRecentTagSets(String mustHaveTag) {
		model.recentTagSets(mustHaveTag).thenAccept(sets->view.showTagSets(sets));
		return null;
	}

	@Override
	public void showPostBodyPreview(Double editorWidth, String html) {
		double imgScaleWidth = Math.min( 640d / editorWidth, 1.0d);
		GWT.log("SCALE: "+imgScaleWidth);
		HtmlReformatter reformatter = new HtmlReformatter(imgScaleWidth);
		String newHtml = reformatter.reformat(html);
		view.showPreview(newHtml);
		GWT.log(newHtml);
	}

	@Override
	public void createNewBlogEntry(BlogEntryType entryType, double width, String title,
			List<? extends Suggestion> tags,
			String content) {
		if (tags == null || tags.isEmpty()) {
			view.setErrorBadTags();
			return;
		}
		if (title == null || title.trim().isEmpty()) {
			view.setErrorBadTitle();
			return;
		}
		GWT.log("Content: "+content.length()+"\n"+content);
		if (content.length()<16) {
			view.setErrorBadContent();
			return;
		}
		Set<String> _tags = new LinkedHashSet<>();
		for (Suggestion tag: tags) {
			_tags.add(tag.getReplacementString());
		}
		model.sortTagsByNetVoteDesc(new ArrayList<>(_tags)).thenAccept(t->{
			model.newBlogEntry(entryType, width, title, t, content).thenAccept(permlink->{
				view.reset();
			});
		}).exceptionally(ex->{
			GWT.log(ex.getMessage(), ex);
			model.newBlogEntry(entryType, width, title, new ArrayList<>(_tags), content).thenAccept(permlink->{
				view.reset();
			});
			return null;
		});
	}

}
