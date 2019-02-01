package co.dporn.gmd.client;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import co.dporn.gmd.client.presenters.AppPresenter;
import gwt.material.design.client.ui.MaterialToast;

public class ViewEventsHandler {

	private final AppPresenter mainPresenter;

	public ViewEventsHandler(EventBus eventBus, AppPresenter mainPresenter) {
		this.eventBus = eventBus;
		this.mainPresenter = mainPresenter;
	}

	interface MyEventBinder extends EventBinder<ViewEventsHandler> {
	}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	private final EventBus eventBus;
	private HandlerRegistration registration;
	public void bind() {
		unbind();
		registration = eventBinder.bindEventHandlers(this, eventBus);
	}

	public void unbind() {
		if (registration != null) {
			registration.removeHandler();
			registration = null;
		}
	}

	@EventHandler
	protected void doShowErrorMessage(ViewEvents.DoNotifyMessage event) {
		MaterialToast.fireToast(event.getHtml(), 15000);
	}
}
