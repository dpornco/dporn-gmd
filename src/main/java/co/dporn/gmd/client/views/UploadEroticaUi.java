package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.img.ImgUtils;
import co.dporn.gmd.client.presenters.UploadErotica;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequest.OnprogressFn;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete.DefaultMaterialChipProvider;
import gwt.material.design.addins.client.richeditor.MaterialRichEditor;
import gwt.material.design.addins.client.richeditor.base.constants.ToolbarButton;
import gwt.material.design.client.constants.ChipType;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialChip;
import gwt.material.design.client.ui.MaterialContainer;
import gwt.material.design.client.ui.MaterialToast;
import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;
import jsinterop.base.Js;

public class UploadEroticaUi extends Composite implements UploadErotica.UploadEroticaView {

	@UiField
	protected MaterialContainer mainContent;

	@UiField
	protected MaterialAutoComplete ac;

	@UiField
	protected MaterialButton btnTagSets;

	@UiField
	protected MaterialRichEditor editor;

	private UploadErotica presenter;

	private final SuggestOracle suggestOracle;

	private final Set<String> mandatorySuggestions;

	private static ThisUiBinder uiBinder = GWT.create(ThisUiBinder.class);

	interface ThisUiBinder extends UiBinder<Widget, UploadEroticaUi> {
	}

	private final ImgUtils imgUtils = new ImgUtils();

	public UploadEroticaUi(SuggestOracle suggestOracle, Set<String> mandatorySuggestions) {
		initWidget(uiBinder.createAndBindUi(this));
		this.suggestOracle = suggestOracle;
		this.mandatorySuggestions = mandatorySuggestions;

		imgUtils.setEventMessageHandler((msg) -> MaterialToast.fireToast(msg));

		ToolbarButton[] noOptions = new ToolbarButton[0];
		editor.setStyleOptions(ToolbarButton.STYLE, ToolbarButton.BOLD, ToolbarButton.ITALIC,
				ToolbarButton.STRIKETHROUGH, ToolbarButton.CLEAR, ToolbarButton.SUPERSCRIPT, ToolbarButton.SUBSCRIPT);
		editor.setFontOptions(noOptions);
		editor.setColorOptions(noOptions);
		editor.setCkMediaOptions(ToolbarButton.CK_IMAGE_UPLOAD);
		editor.setParaOptions(ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.LEFT, ToolbarButton.CENTER,
				ToolbarButton.RIGHT, ToolbarButton.JUSTIFY);
		editor.setUndoOptions(ToolbarButton.REDO, ToolbarButton.UNDO);
		editor.setMiscOptions(ToolbarButton.LINK, ToolbarButton.PICTURE, ToolbarButton.TABLE, ToolbarButton.HR,
				ToolbarButton.FULLSCREEN);
		editor.setHeightOptions(noOptions);
		editor.setAllowBlank(false);
		editor.setAutoValidate(true);
		editor.setDisableDragAndDrop(false);
		editor.setValue("<p><br></p>", true, true);
		editor.getEditor().find("[data-event='imageClass']").remove();
		editor.getEditor().find("[data-event='imageShape']").remove();
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		ac.setSuggestions(suggestOracle);
		Scheduler.get().scheduleDeferred(() -> {
			ac.setChipProvider(new ChipProvider());
			ac.setAllowBlank(false);
			ac.setAutoValidate(true);
			ac.setLimit(8);
			ac.setAutoSuggestLimit(4);
			ac.setItemValues(new ArrayList<>(this.mandatorySuggestions), true);
		});
		editor.addValueChangeHandler(this::valueChangeHandler);
	}

	private static long _counter = System.currentTimeMillis();

	private static synchronized long nextCounter() {
		return (_counter = Math.max(_counter + 1, System.currentTimeMillis()));
	}

	private void postImageToIpfs(HTMLImageElement image) {
		GWT.log("image.src=" + StringUtils.left(image.src, 32));
		String guessExtension = imgUtils.guessExtension(image.src);
		String filename = image.getAttribute("data-filename");
		if (filename == null) {
			filename = "";
		}
		filename = filename.trim();
		filename = filename.replace(" ", "-");
		filename = filename.toLowerCase();
		filename = filename.replaceAll("[^a-z0-9\\.\\-_]", "");
		filename = filename.replaceAll("-+", "-");
		if (filename.isEmpty() || filename.length() <= guessExtension.length() + 1) {
			filename = nextCounter() + "." + guessExtension;
		}
		if (!filename.endsWith("." + guessExtension)) {
			filename += "." + guessExtension;
		}
		final String ipfsFilename = filename;
		new ImgUtils().toBlob(image).thenAccept((blob) -> {
			presenter.postBlobToIpfsFile(ipfsFilename, blob).thenAccept((path) -> {
				MaterialToast.fireToast("Image posted: " + StringUtils.substringAfterLast(path, "/"));
				StringBuilder srcset = new StringBuilder();
				for (String ipfsgw : new String[] { "https://ipfs.io",
						"https://steemitimages.com/0x0/https://ipfs.io" }) {
					srcset.append(ipfsgw);
					srcset.append(path);
					srcset.append(" ");
				}
				image.srcset = srcset.toString();
				image.src = "https://steemitimages.com/0x0/https://ipfs.io" + path;
				image.onerror = e -> {
					image.onerror = null;
					image.src = "https://ipfs.io" + path;
					return e;
				};
			});
		});
	}

	private void valueChangeHandler(ValueChangeEvent<String> event) {
		// always make sure there is "blank" below and above main content
		GWT.log(" - EDITOR CHANGE");

		// keep all content visible
		editor.getEditor().find(".note-editable").css("height", "100%").css("min-height", "256px");

		// image fixups
		editor.getEditor().find(".note-editable").find("img[src^='data:']")
				.each((o, e) -> automaticImageResizeAndIpfsPut(e));
		editor.getEditor().find(".note-editable").find("img").each((o, e) -> automaticImageRestyler(e));

		// TODO: add ipfs => steemitimages sized img src url magic

		// TODO: add save/restore to/from local storage magic in case of hitting
		// "backspace"

		// TODO: try and magic the images sizes scaled to editor container vs the 640px
		// fixed width blog view at steemit.com/busy.org

	}

	private void automaticImageResizeAndIpfsPut(Element e) {
		if (e.getAttribute("ImgUtilsResizedInplace").equals("true")) {
			return;
		}
		imgUtils.resizeInplace(e)//
				.thenAccept((img) -> img.setAttribute("ImgUtilsResizedInplace", "true")) //
				.thenRun(() -> postImageToIpfs(Js.cast(e)));
	};

	private void automaticImageRestyler(Element e) {
		JQueryElement j = JQuery.$(e);
		String styles = e.getAttribute("style");
		if (styles.contains("float: left") || styles.contains("float: right")) {
			j.css("max-width", "50%");
			j.css("height", "");
		} else {
			j.css("max-width", "100%");
			j.css("height", "");
		}
		String origSrc = e.getAttribute("src");
		String src = origSrc;
		if (src.startsWith("https://steemitimages.com/")) {
			if (src.matches("^https://steemitimages.com/\\d+x\\d+/.*")) {
				src = "https://steemitimages.com/" + j.width() + "x0/"
						+ src.replaceFirst("^https://steemitimages.com/\\d+x\\d+/", "");
			}
		} else {
			if (src.startsWith("http://") || src.startsWith("https://")) {
				src = "https://steemitimages.com/" + j.width() + "x0/" + src;
			}
		}
		if (!src.equals(origSrc)) {
			e.setAttribute("src", src);
		}
	};

	private class ChipProvider extends DefaultMaterialChipProvider {
		@Override
		public boolean isChipRemovable(Suggestion suggestion) {
			if (mandatorySuggestions == null) {
				return true;
			}
			if (mandatorySuggestions.contains(suggestion.getDisplayString())) {
				return false;
			}
			if (mandatorySuggestions.contains(suggestion.getReplacementString())) {
				return false;
			}
			return super.isChipRemovable(suggestion);
		}

		@Override
		public MaterialChip getChip(Suggestion suggestion) {
			if (suggestion.getDisplayString() == null || suggestion.getDisplayString().trim().isEmpty()) {
				return new MaterialChip() {
					@Override
					protected void onLoad() {
						super.onLoad();
						this.removeFromParent();
					}
				};
			}
			final MaterialChip chip = new MaterialChip();

			String imageChip = suggestion.getDisplayString();
			String textChip = imageChip;

			String s = "<img src=\"";
			if (imageChip.contains(s)) {
				int ix = imageChip.indexOf(s) + s.length();
				imageChip = imageChip.substring(ix, imageChip.indexOf("\"", ix + 1));
				chip.setUrl(imageChip);
				textChip = textChip.replaceAll("[<](/)?img[^>]*[>]", "");
			}
			chip.setText(textChip);
			if (isChipRemovable(suggestion)) {
				chip.setIconType(IconType.CLOSE);
			}
			chip.setType(ChipType.OUTLINED);
			chip.setMarginRight(8);
			chip.setMarginLeft(-8);
			return chip;
		}

	}

	@Override
	public void unbindPresenter() {
		this.presenter = null;
	}

	@Override
	public void bindPresenter(UploadErotica presenter) {
		this.presenter = presenter;
	}

	@Override
	public String getBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBody(String body) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTags(List<String> tags) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCoverImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCoverImage(String coverImage) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isNoCoverWanted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setNoCoverWanted(boolean noCoverWanted) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAlwaysTags(List<String> alwaysTags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showPreviousTags(List<String> tags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showStockCoverImages(List<String> imageUrls) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onprogressFn(ProgressEvent p0) {
	}

	@Override
	public OnprogressFn getOnprogressFn(String filename) {
		return new OnprogressFn() {
			int percent = -1;

			@Override
			public void onInvoke(ProgressEvent p0) {
				if (!p0.lengthComputable) {
					return;
				}
				int newPercent = 10 * (int) (Math.ceil(p0.loaded * 100 / p0.total) / 10);
				if (newPercent != percent) {
					percent = newPercent;
					MaterialToast.fireToast("Posting to IPFS: " + filename + " " + percent + "%");
				}
			}
		};
	}
}
