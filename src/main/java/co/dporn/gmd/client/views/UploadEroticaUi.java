package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.img.ImgUtils;
import co.dporn.gmd.client.presenters.UploadErotica;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.TagSet;
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
import gwt.material.design.client.ui.MaterialDialog;
import gwt.material.design.client.ui.MaterialDialogContent;
import gwt.material.design.client.ui.MaterialDialogFooter;
import gwt.material.design.client.ui.MaterialLoader;
import gwt.material.design.client.ui.MaterialRow;
import gwt.material.design.client.ui.MaterialTextBox;
import gwt.material.design.client.ui.MaterialToast;
import jsinterop.base.Js;

public class UploadEroticaUi extends Composite implements UploadErotica.UploadEroticaView {

	private static final int MAX_TAGS = 14;

	private class ChipProvider extends DefaultMaterialChipProvider {
		public MaterialChip getChip(String text) {
			Suggestion suggestion = new Suggestion() {
				@Override
				public String getDisplayString() {
					return text;
				}

				@Override
				public String getReplacementString() {
					return text;
				}
			};
			return getChip(suggestion);
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

	}

	interface ThisUiBinder extends UiBinder<Widget, UploadEroticaUi> {
	}

	private static long _counter = System.currentTimeMillis();

	private static ThisUiBinder uiBinder = GWT.create(ThisUiBinder.class);

	private static synchronized long nextCounter() {
		return (_counter = Math.max(_counter + 1, System.currentTimeMillis()));
	}

	@UiField
	protected MaterialAutoComplete ac;

	@UiField
	protected MaterialButton btnTagSets;

	@UiField
	protected MaterialButton btnClear;
	@UiField
	protected MaterialButton btnPreview;
	@UiField
	protected MaterialButton btnSubmit;
	@UiField
	protected MaterialRichEditor editor;
	@UiField
	protected MaterialTextBox title;

	private final ImgUtils imgUtils = new ImgUtils();

	@UiField
	protected MaterialContainer mainContent;

	private final Set<String> mandatorySuggestions;

	private UploadErotica presenter;

	public UploadEroticaUi(SuggestOracle suggestOracle, Set<String> mandatorySuggestions) {
		initWidget(uiBinder.createAndBindUi(this));
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
		editor.setMinHeight("550px");
		btnTagSets.addClickHandler(e -> presenter.viewRecentTagSets("erotica"));
		btnClear.addClickHandler((e) -> reset());
		btnPreview.addClickHandler(e -> {
			presenter.showPostBodyPreview((double) editor.getEditor().width(), editor.getValue());
		});
		btnSubmit.addClickHandler(e -> {
			title.clearErrorText();
			editor.clearErrorText();
			ac.clearErrorText();
			presenter.doPostBlogEntry( //
					BlogEntryType.EROTICA, //
					(double) editor.getEditor().width(), //
					title.getValue(), //
					ac.getValue(), //
					editor.getValue() //
			);
		});
		ac.setSuggestions(suggestOracle);
		Scheduler.get().scheduleDeferred(() -> {
			ac.setChipProvider(new ChipProvider());
			ac.setAllowBlank(false);
			ac.setAutoValidate(true);
			ac.setLimit(MAX_TAGS);
			ac.setAutoSuggestLimit(4);
			ac.setItemValues(new ArrayList<>(this.mandatorySuggestions), true);
			ac.addValueChangeHandler(list -> {
				ac.clearErrorText();
				if (list.getValue().size() > MAX_TAGS) {
					ac.setErrorText("MAX TAGS: " + MAX_TAGS);
				}
				Set<String> already = new HashSet<>();
				for (Suggestion tag : list.getValue()) {
					if (already.contains(tag.getDisplayString())) {
						ac.setErrorText("DUPLICATE TAGS DETECTED");
						break;
					}
					already.add(tag.getDisplayString());
				}
			});
		});
		editor.addValueChangeHandler(this::valueChangeHandler);
	}

	private void automaticImageResizeAndIpfsPut(Element e) {
		if (e.getAttribute("ImgUtilsResizedInplace").equals("true")) {
			return;
		}
		showLoading(true);
		imgUtils.resizeInplace(e)//
				.thenAccept((img) -> img.setAttribute("ImgUtilsResizedInplace", "true")) //
				.thenRun(() -> postImageToIpfs(Js.cast(e)))//
				.exceptionally(ex -> {
					MaterialToast.fireToast("ERROR: " + ex.getMessage());
					showLoading(false);
					return null;
				});
	}

	@Override
	public void bindPresenter(UploadErotica presenter) {
		this.presenter = presenter;
	}

	@Override
	public String getBody() {
		return editor.getValue();
	};

	@Override
	public String getCoverImage() {
		// TODO Auto-generated method stub
		return null;
	};

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

	@Override
	public List<String> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNoCoverWanted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		title.setFocus(true);
	}

	@Override
	public void onprogressFn(ProgressEvent p0) {
	}

	@Override
	protected void onUnload() {
		super.onUnload();
	}

	private void postImageToIpfs(HTMLImageElement image) {
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
				showLoading(false);
				StringBuilder srcset = new StringBuilder();
				for (String ipfsgw : new String[] { "https://ipfs.io",
						"https://steemitimages.com/0x0/https://ipfs.io" }) {
					srcset.append(ipfsgw);
					srcset.append(path);
					srcset.append(", ");
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

	private void showLoading(boolean loading) {
		MaterialLoader.loading(loading);
	}

	@Override
	public void setAlwaysTags(List<String> alwaysTags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBody(String body) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCoverImage(String coverImage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNoCoverWanted(boolean noCoverWanted) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTags(List<String> tags) {
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
	public void showTagSets(List<TagSet> sets) {
		GWT.log("Tag sets: " + sets.size());
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
		ChipProvider chips = new ChipProvider() {
			public boolean isChipRemovable(Suggestion suggestion) {
				return false;
			}
		};
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
			for (String tag : set.getTags()) {
				MaterialChip chip = chips.getChip(tag);
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
	}

	private void valueChangeHandler(ValueChangeEvent<String> event) {
		// keep all content visible
		editor.getEditor().find(".note-editable").css("height", "100%");

		// image fixups
		editor.getEditor().find(".note-editable").find("img[src^='data:']")
				.each((o, e) -> automaticImageResizeAndIpfsPut(e));
		editor.getEditor().find(".note-editable").find("img").each((o, e) -> ImgUtils.automaticImageRestyler(e));

		// TODO: add save/restore to/from local storage magic in case of hitting
		// "backspace"

		// TODO: try and magic the images sizes scaled to editor container vs the 640px
		// fixed width blog view at steemit.com/busy.org

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
		return (double) editor.getEditor().width();
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
		ac.setItemValues(new ArrayList<>(this.mandatorySuggestions), true);
		title.clear();
		editor.reset();
		title.setFocus(true);		
	}
}
