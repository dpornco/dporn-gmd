package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.URL;
import co.dporn.gmd.client.presenters.UploadVideo;
import co.dporn.gmd.client.utils.DpornChipProvider;
import co.dporn.gmd.client.utils.ImgUtils;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.TagSet;
import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FileReader;
import elemental2.dom.FileReader.OnloadendFn;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLVideoElement;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;
import gwt.material.design.client.constants.ProgressType;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialChip;
import gwt.material.design.client.ui.MaterialContainer;
import gwt.material.design.client.ui.MaterialDialog;
import gwt.material.design.client.ui.MaterialDialogContent;
import gwt.material.design.client.ui.MaterialDialogFooter;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialProgress;
import gwt.material.design.client.ui.MaterialRow;
import gwt.material.design.client.ui.MaterialTextBox;
import gwt.material.design.client.ui.MaterialToast;
import gwt.material.design.client.ui.MaterialVideo;
import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;
import jsinterop.base.Js;

public class UploadVideoUi extends Composite implements UploadVideo.UploadVideoView {
	private static final String MAX_NOT_VERIFIED = "Max video length: 15 minutes. If you would like to upload longer higher quality videos, \"Get Verified\".";
	private static final String MAX_VERIFIED = "Max video length: 60 minutes.";

	interface ThisUiBinder extends UiBinder<Widget, UploadVideoUi> {
	}

	private static ThisUiBinder uiBinder = GWT.create(ThisUiBinder.class);

	@UiField
	protected MaterialButton btnCancelUploadVideo;
	@UiField
	protected MaterialLabel maxLengthNotice;
	@UiField
	protected MaterialButton btnTakeSnap;
	@UiField
	protected MaterialProgress videoUploadProgress;
	@UiField
	protected MaterialProgress posterUploadProgress;
	@UiField
	protected MaterialSteemitImage posterImage;
	@UiField
	protected MaterialVideo video;
	@UiField
	protected MaterialButton btnUploadVideo;
	@UiField
	protected MaterialButton btnUploadImage;
	@UiField
	protected HiddenFileUpload fileUploadImage;
	@UiField
	protected HiddenFileUpload fileUploadVideo;
	@UiField
	protected MaterialLink lnkCoverImage;
	@UiField
	protected MaterialButton btnPreviewVideoFile;
	@UiField
	protected TagAutoComplete ac;
	@UiField
	protected MaterialButton btnTagSets;
	@UiField
	protected MaterialButton btnClear;
	@UiField
	protected MaterialButton btnPreview;
	@UiField
	protected MaterialButton btnSubmit;
	@UiField
	protected DpornRichEditor editor;
	@UiField
	protected MaterialTextBox title;
	@UiField
	protected MaterialContainer mainContent;

	private UploadVideo presenter;

	public UploadVideoUi(SuggestOracle suggestOracle, Set<String> mandatorySuggestions) {
		initWidget(uiBinder.createAndBindUi(this));
		btnTagSets.addClickHandler(e -> presenter.viewRecentTagSets("dporncovideo"));
		btnClear.addClickHandler((e) -> reset());
		btnPreview.addClickHandler(e -> {
			presenter.showPostBodyPreview((double) editor.getEditor().width(), editor.getValue(), posterLocation,
					videoLocation);
			GWT.log("Video location: " + videoLocation);
			GWT.log("Poster location: " + posterLocation);
		});
		btnSubmit.addClickHandler(e -> {
			title.clearErrorText();
			editor.clearErrorText();
			ac.clearErrorText();
			presenter.createNewBlogEntry(BlogEntryType.VIDEO, //
					editor.getEditor().width(), //
					title.getValue(), //
					ac.getValue(), //
					editor.getValue(), //
					posterLocation, //
					videoLocation);
		});
		ac.setSuggestions(suggestOracle);
		ac.setMandatoryTags(mandatorySuggestions);
		btnUploadImage.addClickHandler((e) -> fileUploadImage.click());
		btnUploadVideo.addClickHandler((e) -> fileUploadVideo.click());
		fileUploadImage.setAccept(".jpg,.jpeg,.png,.gif");
		fileUploadVideo.setAccept("video/*");
		fileUploadImage.addChangeHandler(this::loadImageAndResize);
		fileUploadVideo.addChangeHandler(this::uploadVideo);
		btnTakeSnap.addClickHandler((e) -> {
			takeVideoSnap();
		});
		btnTakeSnap.setEnabled(false);
		btnUploadImage.setEnabled(false);
		videoLocation = "";
		btnPreviewVideoFile.setEnabled(false);
		btnPreviewVideoFile.addClickHandler(this::showVideoPreviewDialog);
		btnPreview.setEnabled(false);
		btnSubmit.setEnabled(false);
	}

	private Object showVideoPreviewDialog(ClickEvent e) {
		MaterialDialog dialog = new MaterialDialog();
		dialog.addCloseHandler((d) -> dialog.removeFromParent());
		dialog.setFullscreen(true);
		MaterialDialogContent content = new MaterialDialogContent();
		MaterialVideo video = new MaterialVideo("https://ipfs.dporn.co" + videoLocation.replaceAll(".m3u8$", ".html"));
		video.setFullscreen(true);
		content.add(video);
		dialog.add(content);
		MaterialDialogFooter footer = new MaterialDialogFooter();
		MaterialButton btnDismiss = new MaterialButton("DISMISS");
		btnDismiss.addClickHandler((b) -> dialog.close());
		footer.add(btnDismiss);
		dialog.add(footer);
		RootPanel.get().add(dialog);
		dialog.open();
		return null;
	}

	protected void log(Object object) {
		DomGlobal.console.log(object);
	}

	protected void loadImageAndResize(ChangeEvent event) {
		event.preventDefault();
		event.stopPropagation();
		posterImage.setUrl("");
		lnkCoverImage.setHref("");
		lnkCoverImage.setText("");
		HTMLInputElement input = Js.cast(fileUploadImage.getElement());
		if (input.files == null || input.files.length == 0) {
			GWT.log("loadImageAndResize: NO FILES");
			return;
		}
		ImgUtils imgUtils = new ImgUtils();
		btnUploadVideo.setEnabled(false);
		btnUploadImage.setEnabled(false);
		btnTakeSnap.setEnabled(false);
		File file = input.files.getAt(0);
		FileReader reader = new FileReader();
		reader.onabort = (e) -> loadImageError();
		reader.onerror = (e) -> loadImageError();
		OnloadendFn onloadendFn = (e) -> {
			String asString = reader.result.asString();
			HTMLImageElement tmpImage = Js.cast(DomGlobal.document.createElement("img"));
			tmpImage.onabort = (f) -> loadImageError();
			tmpImage.onerror = (f) -> loadImageError();
			tmpImage.onload = (f) -> {
				imgUtils.resizeInplace(tmpImage, 1280, 720, true).thenAccept(resized -> {
					imgUtils.toBlob(resized).thenAccept(blob -> {
						presenter.postBlobToIpfsFile(file.name, blob, this::imageOnprogressFn).thenAccept(location -> {
							posterUploadProgress.setType(ProgressType.DETERMINATE);
							posterUploadProgress.setPercent(100d);
							posterImage.setMaxHeight("240px");
							posterImage.setUrl("https://ipfs.dporn.co" + location);
							lnkCoverImage.setText("IPFS COVER IMAGE");
							lnkCoverImage.setHref("https://ipfs.io" + location);
							lnkCoverImage.setTarget("_blank");
							btnUploadVideo.setEnabled(true);
							btnUploadImage.setEnabled(true);
							btnTakeSnap.setEnabled(true);
						}).exceptionally(ex -> {
							btnUploadVideo.setEnabled(true);
							btnUploadImage.setEnabled(true);
							btnTakeSnap.setEnabled(true);
							MaterialToast.fireToast(ex.getMessage());
							return null;
						});
					}).exceptionally(ex -> {
						btnUploadVideo.setEnabled(true);
						btnUploadImage.setEnabled(true);
						btnTakeSnap.setEnabled(true);
						MaterialToast.fireToast(ex.getMessage());
						return null;
					});
				}).exceptionally(ex -> {
					btnUploadVideo.setEnabled(true);
					btnUploadImage.setEnabled(true);
					btnTakeSnap.setEnabled(true);
					MaterialToast.fireToast(ex.getMessage());
					return null;
				});
				return null;
			};
			tmpImage.src = asString;
			posterImage.setUrl(asString);
			return e;
		};
		reader.onloadend = onloadendFn;
		reader.readAsDataURL(file.slice());
	}

	protected Object loadImageError() {
		btnUploadImage.setEnabled(true);
		MaterialToast.fireToast("Failed to read file from disk.");
		return null;
	}

	protected Object loadVideoError() {
		btnUploadVideo.setEnabled(true);
		MaterialToast.fireToast("Failed to read file from disk.");
		return null;
	}

	protected void imageOnprogressFn(ProgressEvent p0) {
		if (!p0.lengthComputable) {
			log("Not Computable");
			posterUploadProgress.setType(ProgressType.INDETERMINATE);
			return;
		}
		if (p0.loaded == p0.total) {
			posterUploadProgress.setType(ProgressType.INDETERMINATE);
			return;
		}
		double percent = Math.ceil(100d * p0.loaded / p0.total);
		posterUploadProgress.setType(ProgressType.DETERMINATE);
		posterUploadProgress.setPercent(percent);
	};

	protected void uploadVideo(ChangeEvent event) {
		HTMLInputElement input = Js.cast(fileUploadVideo.getElement());
		if (input.files == null || input.files.length == 0) {
			GWT.log("uploadVideo: NO FILES");
			return;
		}
		btnUploadImage.setEnabled(false);
		btnTakeSnap.setEnabled(false);
		btnUploadVideo.setEnabled(false);
		btnPreviewVideoFile.setEnabled(false);
		btnPreview.setEnabled(false);
		btnSubmit.setEnabled(false);
		videoLocation = "";
		OnprogressFn videoOnprogressFn = new OnprogressFn() {
			private long start = 0;

			@Override
			public void onInvoke(ProgressEvent p0) {
				if (!p0.lengthComputable) {
					log("Not Computable");
					videoUploadProgress.setType(ProgressType.INDETERMINATE);
					return;
				}
				if (p0.loaded == p0.total) {
					videoUploadProgress.setType(ProgressType.INDETERMINATE);
					start = System.currentTimeMillis();
					return;
				}
				double percent = Math.ceil(100d * p0.loaded / p0.total);
				videoUploadProgress.setType(ProgressType.DETERMINATE);
				videoUploadProgress.setPercent(percent);
				if (System.currentTimeMillis() - start > 15000) {
					start = System.currentTimeMillis();
					MaterialToast.fireToast("Video upload: " + percent + "%");
				}
			}
		};
		File file = input.files.getAt(0);
		if (title.getValue().trim().isEmpty()) {
			String tmp = file.name;
			tmp = tmp.replaceAll("(.*)\\.[^.]{1,6}$", "$1");
			tmp = tmp.replace("_", " ");
			title.setValue(tmp);
		}
		log("Video file: " + file.name + " [" + file.type + "]");
		video.$this().find("iframe").css("display", "none");
		video.$this().find("video").each((o, e) -> {
			try {
				HTMLVideoElement vid = Js.cast(e);
				vid.onseeked = null;
				URL.revokeObjectURL(vid.src);
			} catch (Exception e1) {
				log(e1);
			}
			e.removeFromParent();
		});
		HTMLVideoElement vid = Js.cast(DomGlobal.document.createElement("video"));
		vid.setAttribute("playsinline", "playsinline");
		vid.autobuffer = true;
		// only true because of ios
		vid.autoplay = true;
		vid.controls = true;
		vid.loop = false;
		vid.volume = 0.1;
		vid.muted = true;
		JQueryElement jvid = JQuery.$(vid);
		video.$this().find("iframe").before(jvid);
		jvid.css("position", "absolute");
		jvid.css("width", "100%");
		jvid.css("height", "100%");
		jvid.css("x", "0");
		jvid.css("y", "0");
		vid.onabort = e -> {
			MaterialToast.fireToast("VIDEO UPLOAD ABORTED", 6000);
			btnUploadVideo.setEnabled(true);
			videoUploadProgress.setType(ProgressType.DETERMINATE);
			videoUploadProgress.setPercent(0);
			return e;
		};
		vid.onerror = e -> {
			MaterialToast.fireToast("NOT A COMPATIBLE VIDEO FILE", 15000);
			btnUploadVideo.setEnabled(true);
			videoUploadProgress.setType(ProgressType.DETERMINATE);
			videoUploadProgress.setPercent(0);
			return e;
		};
		vid.onloadedmetadata = e -> {
			log("vid.onloadedmetadata");
			btnUploadImage.setEnabled(true);
			btnTakeSnap.setEnabled(true);
			setMetadata(vid);
			setMaxVideoLengthNotice(vid);
			presenter.postBlobToIpfsHlsVideo(file.name, file, vid.videoWidth, vid.videoHeight, videoOnprogressFn)
					.thenAccept((location) -> {
						GWT.log("UPLOADED VIDEO: " + location);
						btnUploadVideo.setEnabled(true);
						videoUploadProgress.setType(ProgressType.DETERMINATE);
						videoUploadProgress.setPercent(100);
						btnPreviewVideoFile.setEnabled(true);
						btnPreview.setEnabled(true);
						btnSubmit.setEnabled(true);
						videoLocation = location;
					}).exceptionally(ex -> {
						MaterialToast.fireToast("Waiting for upload slot", 4000);
						new Timer() {
							@Override
							public void run() {
								uploadVideo(event);
							}
						}.schedule(5000 + new Random().nextInt(5000));
						GWT.log("UPLOADED VIDEO FAIL: " + ex.getMessage());
						videoUploadProgress.setType(ProgressType.INDETERMINATE);
						return null;
					});
			vid.onseeked = e1 -> {
				// ios hack
				if (vid.currentTime < 4.0 && vid.currentTime < vid.duration) {
					return e1;
				}
				vid.pause();
				log("vid.onseeked: " + (int) vid.currentTime);
				// only take snap if there isn't one already
				if (posterImage.getUrl() == null || posterImage.getUrl().trim().isEmpty()) {
					log("AUTO SNAP!");
					takeVideoSnap();
				}
				vid.onseeked = null;
				return e1;
			};
			/*
			 * iOS hack
			 */
			try {
				vid.play();
				vid.currentTime = 4.0;
				vid.pause();
			} catch (Exception e1) {
				vid.oncanplaythrough = e3 -> {
					// ios hack
					vid.onprogress = (e2) -> {
						vid.currentTime = 4.0;
						return e2;
					};
					return e3;
				};
			}
			return e;
		};
		// only submit file for recoding and posting to IPFS if the browser accepts it
		// as a video file
		vid.onloadeddata = e -> {
			return e;
		};
		vid.src = URL.createObjectURL(file);
		vid.play();
		vid.pause();
	}

	private void setMaxVideoLengthNotice(HTMLVideoElement vid) {
		StringBuilder sb = new StringBuilder();

		if (verified) {
			sb.append(MAX_VERIFIED);
		} else {
			sb.append(MAX_NOT_VERIFIED);
		}

		int totalSeconds = (int) vid.duration;
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = (totalSeconds / (60 * 60));

		sb.append("<br/><strong>Video length: ");
		if (hours < 10) {
			sb.append("0");
		}
		sb.append(hours);
		sb.append(":");
		if (minutes < 10) {
			sb.append("0");
		}
		sb.append(minutes);
		sb.append(":");
		if (seconds < 10) {
			sb.append("0");
		}
		sb.append(seconds);
		sb.append("<br/>");

		sb.append("Video size: ");
		sb.append(vid.videoWidth);
		sb.append("x");
		sb.append(vid.videoHeight);

		sb.append("<br/>");
		sb.append("Max stream quality: " + getQualityIndicator(vid.videoHeight));

		sb.append("</strong>");

		sb.append("<br/>");

		maxLengthNotice.getElement().setInnerHTML(sb.toString());
	}

	/**
	 * Logic must match the ipfs put video api call for quality selection for
	 * encoding!
	 * 
	 * @param height
	 * @return
	 */
	private String getQualityIndicator(int height) {
		boolean do1080p = verified;
		boolean do720p = verified;
		boolean do480p = true;
		boolean do360p = verified;
		// 1080p
		if (do1080p && height >= (1080 + 720) / 2) {
			return "1080p";
		}

		// 720p
		if (do720p && height >= (720 + 480) / 2) {
			return "720p";
		}

		// 480p
		if (do480p && height >= (480 + 360) / 2) {
			return "480p";
		}

		// 360p
		if (do360p && height >= (360 + 240) / 2) {
			return "360p";
		}

		return "240p";
	}

	private String videoLocation = "";
	private Boolean verified;

	private void setMetadata(HTMLVideoElement vid) {
		log("VIDEO SIZE: " + vid.videoWidth + "x" + vid.videoHeight);
	}

	private String posterLocation = "";

	private void takeVideoSnap() {
		JQueryElement jvid = video.$this().find("video").first();
		if (jvid.length() == 0) {
			return;
		}
		// posterImage.setUrl("");
		lnkCoverImage.setHref("");
		lnkCoverImage.setText("");
		HTMLVideoElement vid = Js.cast(jvid.asElement());
		vid.pause();
		vid.controls = false;
		ImgUtils imgUtils = new ImgUtils();
		imgUtils.resizeInplace(vid, 1280, 720, true).thenAccept(resized -> {
			btnUploadImage.setEnabled(false);
			btnTakeSnap.setEnabled(false);
			posterImage.setUrl(resized.src);
			imgUtils.toBlob(resized).thenAccept(blob -> {
				presenter.postBlobToIpfsFile("snap.jpg", blob, this::imageOnprogressFn).thenAccept(location -> {
					posterLocation = location;
					posterUploadProgress.setType(ProgressType.DETERMINATE);
					posterUploadProgress.setPercent(100d);
					posterImage.setMaxHeight("240px");
					posterImage.setUrl("https://ipfs.dporn.co" + location);
					lnkCoverImage.setText("IPFS COVER IMAGE");
					lnkCoverImage.setHref("https://ipfs.dporn.co" + location);
					lnkCoverImage.setTarget("_blank");
					btnUploadImage.setEnabled(true);
					btnTakeSnap.setEnabled(true);
					vid.controls = true;
				}).exceptionally(ex -> {
					btnUploadImage.setEnabled(true);
					btnTakeSnap.setEnabled(true);
					posterUploadProgress.setType(ProgressType.DETERMINATE);
					posterUploadProgress.setPercent(0d);
					vid.controls = true;
					MaterialToast.fireToast(ex.getMessage());
					return null;
				});
			}).exceptionally(ex -> {
				btnUploadImage.setEnabled(true);
				btnTakeSnap.setEnabled(true);
				posterUploadProgress.setType(ProgressType.DETERMINATE);
				posterUploadProgress.setPercent(0d);
				vid.controls = true;
				MaterialToast.fireToast(ex.getMessage());
				return null;
			});
		}).exceptionally(ex -> {
			btnUploadImage.setEnabled(true);
			btnTakeSnap.setEnabled(true);
			posterUploadProgress.setType(ProgressType.DETERMINATE);
			posterUploadProgress.setPercent(0d);
			vid.controls = true;
			MaterialToast.fireToast(ex.getMessage());
			return null;
		});
	}

	@Override
	public void bindPresenter(UploadVideo presenter) {
		this.presenter = presenter;
		editor.bindIpfsApi(presenter);
	}

	@Override
	public String getBody() {
		return editor.getValue();
	};

	@Override
	public OnprogressFn getOnprogressFn(String filename) {
		return new OnprogressFn() {
			long start = 0;

			@Override
			public void onInvoke(ProgressEvent p0) {
				if (!p0.lengthComputable) {
					return;
				}
				if (System.currentTimeMillis() - start > 10000l) {
					start = System.currentTimeMillis();
					int percent = (int) Math.ceil(p0.loaded * 100d / p0.total);
					MaterialToast.fireToast("Posting to IPFS: " + filename + " " + percent + "%");
				}
			}
		};
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		btnUploadVideo.setFocus(true);
	}

	@Override
	public void onprogressFn(ProgressEvent p0) {
	}

	@Override
	protected void onUnload() {
		super.onUnload();
	}

	@Override
	public void showTagSets(List<TagSet> sets) {
		if (sets.isEmpty()) {
			MaterialToast.fireToast("No recent tag sets available.");
			return;
		}
		MaterialDialog dialog = new MaterialDialog();
		// dialog.setType(DialogType.FIXED_FOOTER);
		MaterialDialogContent content = new MaterialDialogContent();
		dialog.add(content);
		MaterialDialogFooter footer = new MaterialDialogFooter();
		MaterialButton btnClose = new MaterialButton("CLOSE");
		btnClose.addClickHandler((e) -> dialog.close());
		footer.add(btnClose);
		dialog.add(footer);
		DpornChipProvider chipProvider = new DpornChipProvider(new HashSet<>());
		for (TagSet set : sets) {
			MaterialRow row = new MaterialRow();
			MaterialButton btnAdd = new MaterialButton("ADD TAG SET");
			btnAdd.setMarginRight(18);
			row.add(btnAdd);
			btnAdd.addClickHandler((e) -> {
				Set<String> activeTags = new LinkedHashSet<>(ac.getItemValues());
				activeTags.addAll(set.getTags());
				ac.setItemValues(new ArrayList<>(activeTags), true);
				dialog.close();
			});
			chipProvider.setMandatoryTags(set.getTags());
			for (String tag : set.getTags()) {
				MaterialChip chip = chipProvider.getChip(tag);
				chip.setMarginRight(8);
				row.add(chip);
			}
			content.add(row);
		}
		dialog.addCloseHandler((e) -> dialog.removeFromParent());
		RootPanel.get().add(dialog);
		dialog.open();
	}

	@Override
	public void unbindPresenter() {
		this.presenter = null;
		editor.unbindIpfsApi();
	}

	@Override
	public void showPreview(String html) {
		MaterialDialog dialog = new MaterialDialog();
		dialog.addCloseHandler(e -> dialog.removeFromParent());
		MaterialDialogContent content = new MaterialDialogContent();
		dialog.add(content);
		MaterialDialogFooter footer = new MaterialDialogFooter();
		MaterialButton btnClose = new MaterialButton("CLOSE");
		btnClose.addClickHandler((e) -> dialog.close());
		footer.add(btnClose);
		dialog.add(footer);

		MaterialRow row = new MaterialRow();
		row.setWidth("640px");
		row.getElement().setInnerHTML(html);
		content.add(row);

		RootPanel.get().add(dialog);
		dialog.open();
	}

	@Override
	public double getEditorWidth() {
		return editor.getEditor().width();
	}

	@Override
	public void setErrorBadTitle() {
		title.setErrorText("Title must not be blank");
		title.setFocus(true);
	}

	@Override
	public void setErrorBadContent() {
		MaterialToast.fireToast("Not enough content for blog body");
		MaterialToast.fireToast("Add content");
		GWT.log("Not enough content");
		editor.setErrorText("Not enough content");
		editor.setFocus(true);
	}

	@Override
	public void setErrorBadTags() {
		ac.setErrorText("Must specify at least one tag");
		ac.setFocus(true);
	}

	@Override
	public void reset() {
		ac.reset();
		title.reset();
		editor.reset();
		posterImage.setUrl("");
		posterLocation = "";
		videoLocation = "";
		video.setUrl("");
		video.$this().find("iframe").css("display", "");
		video.$this().find("video").each((o, e) -> {
			try {
				HTMLVideoElement vid = Js.cast(e);
				vid.onseeked = null;
				URL.revokeObjectURL(vid.src);
			} catch (Exception e1) {
				log(e1);
			}
			e.removeFromParent();
		});
		videoUploadProgress.setType(ProgressType.DETERMINATE);
		posterUploadProgress.setType(ProgressType.DETERMINATE);
		videoUploadProgress.setPercent(0);
		posterUploadProgress.setPercent(0);
		btnTakeSnap.setEnabled(false);
		btnPreviewVideoFile.setEnabled(false);
		btnUploadImage.setEnabled(true);
		btnUploadVideo.setEnabled(true);
		btnPreview.setEnabled(false);
		btnSubmit.setEnabled(false);
		btnUploadVideo.setFocus(true);
		Window.scrollTo(0, 0);
	}

	@Override
	public void setVerified(Boolean verified) {
		this.verified = verified;
		if (verified) {
			maxLengthNotice.setText(MAX_VERIFIED);
		} else {
			maxLengthNotice.setText(MAX_NOT_VERIFIED);
		}
	}

	@Override
	public void setErrorPosterImage() {
		MaterialToast.fireToast("You must supply a poster image.", 5000);
	}

	@Override
	public void setErrorVideoFile() {
		MaterialToast.fireToast("You must upload a video.", 5000);
	}
}
