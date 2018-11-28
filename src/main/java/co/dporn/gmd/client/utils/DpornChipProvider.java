package co.dporn.gmd.client.utils;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete.DefaultMaterialChipProvider;
import gwt.material.design.client.constants.ChipType;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.ui.MaterialChip;

public class DpornChipProvider extends DefaultMaterialChipProvider {
	private Set<String> mandatoryTags;

	public Set<String> getMandatoryTags() {
		return mandatoryTags;
	}

	public void setMandatoryTags(Set<String> mandatoryTags) {
		this.mandatoryTags = mandatoryTags;
	}

	public DpornChipProvider() {
		this.mandatoryTags = new HashSet<>();
	}

	public DpornChipProvider(Set<String> mandatoryTags) {
		this.mandatoryTags = mandatoryTags;
	}

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
		if (mandatoryTags == null) {
			return true;
		}
		if (mandatoryTags.contains(suggestion.getDisplayString())) {
			return false;
		}
		if (mandatoryTags.contains(suggestion.getReplacementString())) {
			return false;
		}
		return super.isChipRemovable(suggestion);
	}

}