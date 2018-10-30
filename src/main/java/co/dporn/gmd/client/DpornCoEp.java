package co.dporn.gmd.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.RootPanel;

import co.dporn.gmd.client.presenters.AppPresenterImpl;
import co.dporn.gmd.client.views.AppLayoutUi;
import gwt.material.design.client.ui.MaterialLoader;
import gwt.material.design.jquery.client.api.JQuery;

public class DpornCoEp implements EntryPoint {
	@Override
	public void onModuleLoad() {
		GWT.log(JQuery.window().getTitle());
		MaterialLoader.loading(true);
		Scheduler.get().scheduleDeferred(new AppPresenterImpl(RootPanel.get(), new AppLayoutUi()));
	}
}
