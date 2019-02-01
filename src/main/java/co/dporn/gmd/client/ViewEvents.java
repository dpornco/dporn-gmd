package co.dporn.gmd.client;

import com.google.web.bindery.event.shared.binder.GenericEvent;

public interface ViewEvents {

	public class DoNotifyMessage extends GenericEvent {

		private final String html;

		public DoNotifyMessage(String html) {
			this.html = html;
		}

		public String getHtml() {
			return html;
		}

	}

}
