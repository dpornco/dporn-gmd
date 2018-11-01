package co.dporn.gmd.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.RootPanel;

import co.dporn.gmd.client.app.AppControllerModelImpl;
import co.dporn.gmd.client.presenters.AppPresenterImpl;
import co.dporn.gmd.client.views.AppLayoutUi;

public class DpornCoEp implements EntryPoint {
	@Override
	public void onModuleLoad() {
		AppPresenterImpl mainPresenter = new AppPresenterImpl(new AppControllerModelImpl(), RootPanel.get(), new AppLayoutUi());
		Scheduler.get().scheduleDeferred(mainPresenter);
		RestClient client = new RestClient();
		client.ping().thenAccept((p)->GWT.log("PONG: "+p.isPong())).exceptionally((e)->{
			GWT.log(e.getMessage(),e);
			return null;
		});
	}
}
