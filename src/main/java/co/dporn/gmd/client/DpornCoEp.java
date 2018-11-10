package co.dporn.gmd.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.RootPanel;
import com.wallissoftware.pushstate.client.PushStateHistorian;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.app.AppControllerModelImpl;
import co.dporn.gmd.client.presenters.AppPresenterImpl;
import co.dporn.gmd.client.views.AppLayoutUi;
import steem.JSON;
import steem.connect.SteemConnectV2;

public class DpornCoEp implements EntryPoint {
	@Override
	public void onModuleLoad() {
		PushStateHistorian.setRelativePath("/");
		PushStateHistorian historian = GWT.create(PushStateHistorian.class);
		AppControllerModel model = new AppControllerModelImpl();
		RootPanel rootDisplay = RootPanel.get();
		AppLayoutUi appLayoutView = new AppLayoutUi();
		AppPresenterImpl mainPresenter = new AppPresenterImpl(historian, model, rootDisplay, appLayoutView);
		Scheduler.get().scheduleDeferred(mainPresenter);
		GWT.log(JSON.stringify(SteemConnectV2.initialize("", "", "access_token", "vote", "comment")));
	}
}
