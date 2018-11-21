package co.dporn.gmd.client.presenters;

import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.utils.HtmlReformatter;
import co.dporn.gmd.client.views.IsView;
import elemental2.dom.Blob;
import elemental2.dom.XMLHttpRequest.OnprogressFn;

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
	public void selectStockCoverImage(String coverImage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showAvailableStockCoverImages() {
		// TODO Auto-generated method stub

	}

	@Override
	public void submit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		view.setBody(null);
		view.setCoverImage(null);
		view.setNoCoverWanted(false);
		view.setTags(null);
		view.setTitle(null);
	}

	@Override
	public void showPreviousTags() {
		// TODO Auto-generated method stub

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
		GWT.log(newHtml);
	}
}
