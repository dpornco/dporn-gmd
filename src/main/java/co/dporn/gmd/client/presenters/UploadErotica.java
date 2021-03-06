package co.dporn.gmd.client.presenters;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import co.dporn.gmd.client.presenters.AppPresenter.IsChildPresenter;
import co.dporn.gmd.client.presenters.UploadErotica.UploadEroticaView;
import co.dporn.gmd.client.utils.IpfsApi;
import co.dporn.gmd.client.views.CommonUploadViewFeatures;
import co.dporn.gmd.client.views.IsView;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.TagSet;
import elemental2.dom.Blob;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;

public interface UploadErotica extends IsChildPresenter<UploadEroticaView>, ScheduledCommand, IpfsApi {
	interface UploadEroticaView extends IsView<UploadErotica>, CommonUploadViewFeatures {
		void onprogressFn(ProgressEvent p0);
		OnprogressFn getOnprogressFn(String filename);
		void showTagSets(List<TagSet> sets);
		void showPreview(String html);
		double getEditorWidth();
		void setErrorBadTitle();
		void setErrorBadContent();
		void setErrorBadTags();
		void reset();
	}
	void reset();
	/**
	 * Posts the binary blob to IPFS and returns with the IPFS folder wrap hash and filename.
	 * @param blob
	 * @return
	 */
	@Override
	CompletableFuture<String> postBlobToIpfs(String filename, Blob blob);
	Void viewRecentTagSets(String mustHaveTag);
	void showPostBodyPreview(Double editorWidth, String html);
	void createNewBlogEntry(BlogEntryType erotica, double width, String value, List<? extends Suggestion> tags,
			String value3);
}
