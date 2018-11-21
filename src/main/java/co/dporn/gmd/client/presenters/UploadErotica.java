package co.dporn.gmd.client.presenters;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import co.dporn.gmd.client.presenters.AppPresenter.IsChildPresenter;
import co.dporn.gmd.client.presenters.UploadErotica.UploadEroticaView;
import co.dporn.gmd.client.views.CommonUploadViewFeatures;
import co.dporn.gmd.client.views.IsView;
import elemental2.dom.Blob;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequest.OnprogressFn;

public interface UploadErotica extends IsChildPresenter<UploadEroticaView>, ScheduledCommand {
	interface UploadEroticaView extends IsView<UploadErotica>, CommonUploadViewFeatures {
		void onprogressFn(ProgressEvent p0);
		OnprogressFn getOnprogressFn(String filename);
	}
	void selectStockCoverImage(String coverImage);
	void showAvailableStockCoverImages();
	void submit();
	void cancel();
	void reset();
	void showPreviousTags();
	/**
	 * Posts the binary blob to IPFS and returns with the IPFS folder wrap hash and filename.
	 * @param blob
	 * @return
	 */
	CompletableFuture<String> postBlobToIpfsFile(String filename, Blob blob);
}
