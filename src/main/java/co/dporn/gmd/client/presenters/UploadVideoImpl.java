package co.dporn.gmd.client.presenters;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.wallissoftware.pushstate.client.PushStateHistorian;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.IsView;
import co.dporn.gmd.shared.BlogEntryType;
import elemental2.dom.Blob;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;

public class UploadVideoImpl implements UploadVideo {

	private AppControllerModel model;
	private UploadVideoView view;
	private int posX;
	private int posY;

	public UploadVideoImpl(AppControllerModel model, UploadVideoView childView) {
		setModel(model);
		setView(childView);
	}

	@Override
	public IsView<?> getContentView() {
		return view;
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model = model;
	}

	@Override
	public void setView(UploadVideoView view) {
		this.view = view;
		view.bindPresenter(this);
	}

	@Override
	public void saveScrollPosition() {
		posX = Window.getScrollLeft();
		posY = Window.getScrollTop();
	}

	@Override
	public void restoreScrollPosition() {
		Window.scrollTo(posX, posY);
	}

	@Override
	public void execute() {
		CompletableFuture<Boolean> future = model.isVerified();
		future.thenAccept(verified -> {
			view.setVerified(verified);
		});
	}

	@Override
	public void reset() {
		view.reset();
	}

	@Override
	public CompletableFuture<String> postBlobToIpfs(String filename, Blob blob) {
		OnprogressFn onprogressfn = view.getOnprogressFn(filename);
		return model.postBlobToIpfsFile(filename, blob, (e) -> onprogressfn.onInvoke(e));
	}

	@Override
	public CompletableFuture<String> postBlobToIpfsFile(String filename, Blob blob, OnprogressFn onprogressFn) {
		return model.postBlobToIpfsFile(filename, blob, (e) -> onprogressFn.onInvoke(e));
	}

	@Override
	public Void viewRecentTagSets(String mustHaveTag) {
		model.recentTagSets(mustHaveTag).thenAccept(sets -> view.showTagSets(sets));
		return null;
	}

	@Override
	public void showPostBodyPreview(Double editorWidth, String html, String posterImage, String videoLink) {
		String permlink = "";

		String newHtml = generatePostHtml(editorWidth, posterImage, videoLink, html, "", permlink);
		/*
		 * wrap
		 */
		newHtml = "<div class='Markdown show-border'>" + newHtml + "<div class='clear-both'></div></div>";
		view.showPreview(newHtml);
		GWT.log(newHtml);
	}

	private String generatePostHtml(Double editorWidth, String posterImage, String videoLink, String html,
			String username, String permlink) {
		if (username == null) {
			username = "";
		}
		if (!username.startsWith("@")) {
			username = "@" + username;
		}
		String lcPosterImage = posterImage.toLowerCase();
		if (lcPosterImage.startsWith("http:") || lcPosterImage.startsWith("https:")) {
			if (!lcPosterImage.matches("^https://steemitimages.com/\\d+x\\d+/.*")) {
				posterImage = "https://steemitimages.com/1280x720/" + posterImage;
			}
		} else {
			posterImage = "https://steemitimages.com/1280x720/https://ipfs.dporn.co" + posterImage;
		}
		double imgScaleWidth = Math.min(640d / editorWidth, 1.0d);
		GWT.log("SCALE: " + imgScaleWidth);
		/*
		 * prepend
		 */
		boolean havePermlink = permlink != null && !permlink.trim().isEmpty();
		html = "<div><center>" //
				+ (havePermlink
						? "<a href=\"https://dporn.co/dporn/" + username + "/" + permlink + "\" target=\"_blank\">"
						: "") //
				+ "<img style='max-width: 100%; width: auto; height: auto; max-height: none;' src=\"" + posterImage
				+ "\">" //
				+ "<h3>View video on DPorn</h3>" //
				+ (havePermlink ? "</a>" : "") //
				+ "</center>" //
				+ "</div><p><br/></p>" + html;

		/*
		 * append
		 */
		if (!html.trim().isEmpty()) {
			html += "<p><br/></p>";
		}
		html += "<div>";
		html += "<div class='pull-left' style='max-width: 50%; float: left; padding-right: 1rem;'>";
		html += "<h5>My DPorn channel: <a href=\"https://dporn.co/@" + model.getUsername() + "\" target=\"_blank\">@"
				+ model.getUsername() + "</a></h5>";
		html += "</div>";
		html += "<div class='pull-right' style='max-width: 50%; float: right; padding-left: 1rem;'>";
		html += "<div class='text-right' style='text-align: right;'>";
		html += "<h5>View using <a href=\"https://ipfs.io" + videoLink.replaceAll(".m3u8$", ".html")
				+ "\" target=\"_blank\">IPFS.IO</a></h5>";
		html += "</div>";
		html += "</div>";
		html += "<p><br/></p>";
		html += "</div>";

		return html;
	}

	@Override
	public void createNewBlogEntry(BlogEntryType entryType, double editorWidth, String title,
			List<? extends Suggestion> tags, String content, String posterImageLocation, String videoLocation) {
		if (posterImageLocation == null || posterImageLocation.trim().isEmpty()) {
			view.setErrorPosterImage();
			return;
		}
		if (videoLocation == null || videoLocation.trim().isEmpty()) {
			view.setErrorVideoFile();
			return;
		}
		if (tags == null || tags.isEmpty()) {
			view.setErrorBadTags();
			return;
		}
		if (title == null || title.trim().isEmpty()) {
			view.setErrorBadTitle();
			return;
		}
		String permlink = model.getTimestampedPermlink(title);
		String newHtml = generatePostHtml(editorWidth, posterImageLocation, videoLocation, content, //
				model.getUsername(), permlink);
		Set<String> _tags = new LinkedHashSet<>();
		for (Suggestion tag : tags) {
			_tags.add(tag.getReplacementString());
		}
		model.newBlogEntry(entryType, editorWidth, title, new ArrayList<>(_tags), newHtml, posterImageLocation,
				videoLocation, new ArrayList<>(), permlink).thenAccept(p -> {
					view.reset();
					new PushStateHistorian().newItem("@" + model.getUsername() + "/" + p, true);
				});
	}

	@Override
	public CompletableFuture<String> postBlobToIpfsHlsVideo(String filename, Blob blob, int videoWidth, int videoHeight,
			OnprogressFn onprogress) {
		return model.postBlobToIpfsHlsVideo(filename, blob, videoWidth, videoHeight, onprogress);
	}

	@Override
	public CompletableFuture<String> postBlobToIpfsHlsVideo(String filename, Blob blob, OnprogressFn onprogress) {
		return null;
	}

	@Override
	public void scrollToTop() {
		Window.scrollTo(0, 0);
	}
}
