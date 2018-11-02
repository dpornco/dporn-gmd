package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class PhotoGalleryUi extends Composite {

	private static PhotoGalleryUiUiBinder uiBinder = GWT.create(PhotoGalleryUiUiBinder.class);

	interface PhotoGalleryUiUiBinder extends UiBinder<Widget, PhotoGalleryUi> {
	}

	public PhotoGalleryUi() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
