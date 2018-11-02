package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.ContentPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.ContentView;
import gwt.material.design.client.ui.MaterialContainer;
import gwt.material.design.client.ui.MaterialRow;

public class ContentUi extends Composite implements ContentView {

	@UiField
	protected MaterialRow featuredChannels;

	@UiField
	protected MaterialContainer mainContent;

	@Override
	public IsWidget getContainer() {
		return mainContent;
	}

	private static ContainerUiUiBinder uiBinder = GWT.create(ContainerUiUiBinder.class);

	interface ContainerUiUiBinder extends UiBinder<Widget, ContentUi> {
	}

	public ContentUi() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	protected void onLoad() {
		super.onLoad();
	}

	@Override
	public void bindPresenter(ContentPresenter presenter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unbindPresenter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public HasWidgets getFeatured() {
		return featuredChannels;
	}

	@Override
	public void animateFeatured() {
	}
	
	@Override
	public void hideFeatured() {
	}

	@Override
	public HasWidgets getPosts() {
		// TODO Auto-generated method stub
		return null;
	}

}
