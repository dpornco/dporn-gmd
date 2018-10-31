package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.ContentPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.ContentView;
import gwt.material.design.client.ui.MaterialContainer;

public class ContentUi extends Composite implements ContentView {
	
	@UiField
    protected MaterialContainer container;

	@Override
	public IsWidget getContainer() {
		return container;
	}

	private static ContainerUiUiBinder uiBinder = GWT.create(ContainerUiUiBinder.class);

	interface ContainerUiUiBinder extends UiBinder<Widget, ContentUi> {
	}

	public ContentUi() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void bindPresenter(ContentPresenter presenter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unbindPresenter() {
		// TODO Auto-generated method stub
		
	}

}
