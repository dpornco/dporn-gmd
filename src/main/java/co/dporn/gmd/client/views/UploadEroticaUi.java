package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.img.ImgUtils;
import co.dporn.gmd.client.presenters.UploadErotica;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete.DefaultMaterialChipProvider;
import gwt.material.design.addins.client.richeditor.MaterialRichEditor;
import gwt.material.design.addins.client.richeditor.base.constants.ToolbarButton;
import gwt.material.design.client.constants.ChipType;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialChip;
import gwt.material.design.client.ui.MaterialContainer;
import gwt.material.design.jquery.client.api.Event;
import gwt.material.design.jquery.client.api.Functions;
import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;

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

	public UploadEroticaUi(SuggestOracle suggestOracle, Set<String> mandatorySuggestions) {
		initWidget(uiBinder.createAndBindUi(this));
		this.suggestOracle = suggestOracle;
		this.mandatorySuggestions = mandatorySuggestions;
		
		ToolbarButton[] noOptions = new ToolbarButton[0];
		editor.setStyleOptions(ToolbarButton.STYLE, ToolbarButton.BOLD, ToolbarButton.ITALIC,
				ToolbarButton.STRIKETHROUGH, ToolbarButton.CLEAR, ToolbarButton.SUPERSCRIPT, ToolbarButton.SUBSCRIPT);
		editor.setFontOptions(noOptions);
		editor.setColorOptions(noOptions);
		editor.setCkMediaOptions(ToolbarButton.CK_IMAGE_UPLOAD);
		editor.setParaOptions(ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.LEFT, ToolbarButton.CENTER,
				ToolbarButton.RIGHT, ToolbarButton.JUSTIFY);
		editor.setUndoOptions(ToolbarButton.REDO, ToolbarButton.UNDO);
		editor.setMiscOptions(ToolbarButton.LINK, ToolbarButton.PICTURE, ToolbarButton.TABLE, ToolbarButton.HR, ToolbarButton.FULLSCREEN);
		editor.setHeightOptions(noOptions);
		editor.setAllowBlank(false);
		editor.setAutoValidate(true);
		editor.setDisableDragAndDrop(false);
		editor.setValue("<p><br></p>", true, true);
		
		editor.getEditor().on("materialnote", new Functions.EventFunc3<String, JQueryElement, JQueryElement>() {
			@Override
			public Object call(Event e, String param1, JQueryElement param2, JQueryElement param3) {
				GWT.log("event: "+e.type);
				GWT.log("param1: "+param1);
				GWT.log("param2: "+param2);
				GWT.log("param3: "+param3);
				return e;
			}
		});
		//String MATERIALNOTE_BLUR = "materialnote.blur";
//		JsRichEditor.$(editor.getEditor());
//		editor.getEditor().on("materialnote.onImageUpload", new Functions.EventFunc3<String, JQueryElement, JQueryElement>() {
//			@Override
//			public Object call(Event e, String param1, JQueryElement param2, JQueryElement param3) {
//				GWT.log("event: "+e.type);
//				GWT.log("param1: "+param1);
//				GWT.log("param2: "+param2);
//				GWT.log("param3: "+param3);
//				return e;
//			}
//		});
//		editor.getEditor().on("materialnote.onimageupload", new Functions.EventFunc3<String, JQueryElement, JQueryElement>() {
//			@Override
//			public Object call(Event e, String param1, JQueryElement param2, JQueryElement param3) {
//				GWT.log("event: "+e.type);
//				GWT.log("param1: "+param1);
//				GWT.log("param2: "+param2);
//				GWT.log("param3: "+param3);
//				return e;
//			}
//		});
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
		ValueChangeHandler<String> handler = new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				// always make sure there is "blank" below and above main content
				GWT.log(" - EDITOR CHANGE");

				// keep all content visible
				editor.getEditor().find(".note-editable").css("height", "100%").css("min-height", "512px");

				// image fixups
				editor.getEditor().find(".note-editable").find("img").each((o, e) -> {
					if (!e.getAttribute("ImgUtilsResizedInplace").equals("true")) {
						ImgUtils.resizeInplace(e).thenAccept((img)->img.setAttribute("ImgUtilsResizedInplace", "true"));
					}
					String styles = e.getAttribute("style");
					if (styles.contains("float: left") || styles.contains("float: right")) {
						JQuery.$(e).css("max-width", "50%");
						JQuery.$(e).css("height", "");
					} else {
						JQuery.$(e).css("max-width", "100%");
						JQuery.$(e).css("height", "");
					}
					styles = e.getAttribute("style");

					// TODO: add ipfs => steemitimages sized img src url magic

					// TODO: add save/restore to/from local storage magic in case of hitting
					// "backspace"

					// TODO: try and magic the images sizes scaled to editor container vs the 640px
					// fixed width blog view at steemit.com/busy.org

				});

			}
		};
		editor.addValueChangeHandler(handler);
	}

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

}
