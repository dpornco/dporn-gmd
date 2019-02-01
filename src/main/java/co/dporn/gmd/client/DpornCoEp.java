package co.dporn.gmd.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.wallissoftware.pushstate.client.PushStateHistorian;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.app.AppControllerModelImpl;
import co.dporn.gmd.client.presenters.AppPresenterImpl;
import co.dporn.gmd.client.views.AppLayoutUi;

public class DpornCoEp implements EntryPoint {

	@Override
	public void onModuleLoad() {
		EventBus eventBus = new DeferredEventBus();
		PushStateHistorian.setRelativePath("/");
		PushStateHistorian historian = new PushStateHistorian();
		AppControllerModel model = new AppControllerModelImpl(eventBus, historian);
		RootPanel rootDisplay = RootPanel.get();
		AppLayoutUi appLayoutView = new AppLayoutUi();
		AppPresenterImpl mainPresenter = new AppPresenterImpl(eventBus, model, rootDisplay, appLayoutView);
		ViewEventsHandler viewEventsHandler = new ViewEventsHandler(eventBus, mainPresenter);
		viewEventsHandler.bind();
		Scheduler.get().scheduleDeferred(mainPresenter);
	}
}
