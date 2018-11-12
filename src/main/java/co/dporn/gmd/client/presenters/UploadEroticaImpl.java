package co.dporn.gmd.client.presenters;

import com.google.gwt.user.client.Window;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.IsView;

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
}
