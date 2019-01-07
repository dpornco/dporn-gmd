package co.dporn.gmd.client.presenters;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.wallissoftware.pushstate.client.PushStateHistorian;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.utils.HtmlReformatter;
import co.dporn.gmd.client.views.IsView;
import co.dporn.gmd.shared.BlogEntryType;
import elemental2.dom.Blob;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;

public class UploadEroticaImpl implements UploadErotica {

	private AppControllerModel model;
	private UploadEroticaView view;
	
	public UploadEroticaImpl(AppControllerModel model, UploadEroticaView view) {
		setModel(model);
		setView(view);
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model=model;
	}

	@Override
	public void setView(UploadEroticaView view) {
		this.view=view;
		view.bindPresenter(this);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		view.reset();
	}

	private int posX = 0;
	private int posY = 0;

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
	public IsView<?> getContentView() {
		return view;
	}

	@Override
	public CompletableFuture<String> postBlobToIpfs(String filename, Blob blob) {
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
		html += "<p>Posted using <a href=\"https://dporn.co/\" target=\"_blank\">DPorn</a></p>";
		double imgScaleWidth = Math.min( 640d / editorWidth, 1.0d);
		GWT.log("SCALE: "+imgScaleWidth);
		HtmlReformatter reformatter = new HtmlReformatter(imgScaleWidth);
		String newHtml = reformatter.reformat(html);
		view.showPreview(newHtml);
//		GWT.log(newHtml);
//		model.getHtmlSanitized(newHtml).thenAccept(response->{
//			String sanitizedHtml = response.getSanitizedHtml();
//			GWT.log(html);
//			GWT.log(sanitizedHtml);
//			view.showPreview(sanitizedHtml);
//		}).exceptionally((ex)->{
//			MaterialToast.fireToast(ex.getMessage());
//			return null;
//		});
	}

	@Override
	public void createNewBlogEntry(BlogEntryType entryType, double width, String title, List<? extends Suggestion> tags,
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
			model.newBlogEntry(entryType, width, title, t, content).thenAccept(p->{
				view.reset();
				new PushStateHistorian().newItem("@"+model.getUsername()+"/"+p, true);
			});
		}).exceptionally(ex->{
			GWT.log(ex.getMessage(), ex);
			model.newBlogEntry(entryType, width, title, new ArrayList<>(_tags), content).thenAccept(p->{
				view.reset();
				new PushStateHistorian().newItem("@"+model.getUsername()+"/"+p, true);
			});
			return null;
		});
	}

	@Override
	public CompletableFuture<String> postBlobToIpfsFile(String filename, Blob blob, OnprogressFn onprogressFn) {
		return model.postBlobToIpfsFile(filename, blob, (e)->onprogressFn.onInvoke(e));
	}

	@Override
	public CompletableFuture<String> postBlobToIpfsHlsVideo(String filename, Blob blob, OnprogressFn onprogress) {
		CompletableFuture<String> completableFuture = new CompletableFuture<>();
		//intentionally not implemented.
		completableFuture.completeExceptionally(new RuntimeException("Not Implemented."));
		return completableFuture;
	}
}
