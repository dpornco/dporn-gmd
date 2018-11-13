package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.UploadErotica;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete.DefaultMaterialChipProvider;
import gwt.material.design.client.constants.ChipType;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialChip;
import gwt.material.design.client.ui.MaterialContainer;

public class UploadEroticaUi extends Composite implements UploadErotica.UploadEroticaView {

	@UiField
	protected MaterialContainer mainContent;

	@UiField
	protected MaterialAutoComplete ac;
	
	@UiField
	protected MaterialButton btnTagSets;

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
	}

	@Override
	protected void onLoad() {
		//MaterialRow
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
	}
	
	private class ChipProvider extends DefaultMaterialChipProvider {
		@Override
		public boolean isChipRemovable(Suggestion suggestion) {
			GWT.log("isChipRemovable: "+suggestion.getDisplayString()+"|"+suggestion.getReplacementString());
			if (mandatorySuggestions==null) {
				return true;
			}
			if (mandatorySuggestions.contains(suggestion.getDisplayString())) {
				GWT.log("isChipRemovable: false");
				return false;
			}
			if (mandatorySuggestions.contains(suggestion.getReplacementString())) {
				GWT.log("isChipRemovable: false");
				return false;
			}
			GWT.log("isChipRemovable: true");
			return super.isChipRemovable(suggestion);
		}
		
		@Override
		public MaterialChip getChip(Suggestion suggestion) {
			if (suggestion.getDisplayString()==null||suggestion.getDisplayString().trim().isEmpty()) {
				return new MaterialChip() {
					@Override
					protected void onLoad() {
						super.onLoad();
						this.removeFromParent();
					}
				};
			}
			GWT.log("NEW CHIP: "+suggestion.getDisplayString());
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
