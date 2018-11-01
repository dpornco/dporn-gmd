package co.dporn.gmd.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.RootPanel;

import co.dporn.gmd.client.presenters.AppPresenterImpl;
import co.dporn.gmd.client.views.AppLayoutUi;

public class DpornCoEp implements EntryPoint {
	@Override
	public void onModuleLoad() {
		Scheduler.get().scheduleDeferred(new AppPresenterImpl(RootPanel.get(), new AppLayoutUi()));
		RestClient client = new RestClient();
		client.ping().thenAccept((p)->GWT.log("PONG: "+p.isPong())).exceptionally((e)->{
			GWT.log(e.getMessage(),e);
			return null;
		});
	}
}
