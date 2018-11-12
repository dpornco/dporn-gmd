package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import co.dporn.gmd.client.presenters.AppPresenter.IsChildPresenter;
import co.dporn.gmd.client.presenters.UploadErotica.UploadEroticaView;
import co.dporn.gmd.client.views.CommonUploadViewFeatures;
import co.dporn.gmd.client.views.IsView;

public interface UploadErotica extends IsChildPresenter<UploadEroticaView>, ScheduledCommand {
	interface UploadEroticaView extends IsView<UploadErotica>, CommonUploadViewFeatures {
		
	}
	void selectStockCoverImage(String coverImage);
	void showAvailableStockCoverImages();
	void submit();
	void cancel();
	void reset();
	void showPreviousTags();
}
