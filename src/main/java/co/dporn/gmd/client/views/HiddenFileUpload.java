package co.dporn.gmd.client.views;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.FileUpload;

import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;

public class HiddenFileUpload extends FileUpload {

	public HiddenFileUpload() {
		super();
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleDeferred(() -> {
			JQueryElement e = JQuery.$(this.getElement());
			e.css("position", "absolute !important");
			e.css("height", "1px");
			e.css("width", "1px");
			e.css("overflow", "hidden");
			e.css("clip", "rect(1px, 1px, 1px, 1px)");
		});
	}

	public void setAccept(String accepts) {

	}

	public void setMultiple(boolean multipleUploads) {

	}
}
