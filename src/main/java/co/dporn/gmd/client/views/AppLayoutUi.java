package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.AppPresenter;
import gwt.material.design.client.ui.MaterialSplashScreen;
import gwt.material.design.client.ui.MaterialTab;

public class AppLayoutUi extends Composite implements AppLayoutView {
	
	@UiField
    protected MaterialSplashScreen splash;

    @UiField
    protected MaterialTab tab;

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

}
