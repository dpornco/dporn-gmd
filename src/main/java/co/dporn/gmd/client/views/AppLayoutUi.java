package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.AppPresenter;
import co.dporn.gmd.client.presenters.AppPresenter.AppLayoutView;
import co.dporn.gmd.client.presenters.ContentPresenter;
import gwt.material.design.client.ui.MaterialContainer;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialSideNavPush;

public class AppLayoutUi extends Composite implements AppLayoutView {
	
	@UiField
    protected MaterialSideNavPush sidenav;
	
	@UiField
	protected MaterialPanel panel;
	

	@UiField protected MaterialContainer container;
	
	private static AppLayoutUiUiBinder uiBinder = GWT.create(AppLayoutUiUiBinder.class);

	interface AppLayoutUiUiBinder extends UiBinder<Widget, AppLayoutUi> {
	}

	public AppLayoutUi() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void bindPresenter(AppPresenter presenter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unbindPresenter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onLoad() {
		super.onLoad();
	}

	@Override
	public void replaceContentPresenter(ContentPresenter childPresenter) {
		container.clear();
		container.add(childPresenter.getContentView().asWidget());
	}
}
