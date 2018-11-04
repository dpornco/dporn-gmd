package co.dporn.gmd.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.RootPanel;

import co.dporn.gmd.client.app.AppControllerModelImpl;
import co.dporn.gmd.client.presenters.AppPresenterImpl;
import co.dporn.gmd.client.views.AppLayoutUi;

public class DpornCoEp implements EntryPoint {
	@Override
	public void onModuleLoad() {
		AppControllerModelImpl model = new AppControllerModelImpl();
		RootPanel rootDisplay = RootPanel.get();
		AppLayoutUi appLayoutView = new AppLayoutUi();
		AppPresenterImpl mainPresenter = new AppPresenterImpl(model, rootDisplay, appLayoutView);
		Scheduler.get().scheduleDeferred(mainPresenter);
	}
}
