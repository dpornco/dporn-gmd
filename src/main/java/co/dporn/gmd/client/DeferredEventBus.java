package co.dporn.gmd.client;

import com.google.gwt.core.client.Scheduler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class DeferredEventBus extends SimpleEventBus {

	private static final Scheduler scheduler = Scheduler.get();

	@Override
	public void fireEvent(Event<?> event) {
		scheduler.scheduleDeferred(() -> super.fireEvent(event));
	}

	@Override
	public void fireEventFromSource(Event<?> event, Object source) {
		scheduler.scheduleDeferred(() -> super.fireEventFromSource(event, source));
	}

}
