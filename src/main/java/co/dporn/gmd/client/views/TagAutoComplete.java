package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import co.dporn.gmd.client.utils.DpornChipProvider;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete;
import gwt.material.design.addins.client.autocomplete.constants.AutocompleteType;

public class TagAutoComplete extends MaterialAutoComplete {
	private int maxTags = 14;

	public int getMaxTags() {
		return maxTags;
	}

	public void setMaxTags(int maxTags) {
		this.maxTags = maxTags;
	}
	
	@Override
	public void reset() {
		super.reset();
		MaterialChipProvider chipProvider = getChipProvider();
		if (chipProvider!=null && chipProvider instanceof DpornChipProvider) {
			Set<String> mandatoryTags = ((DpornChipProvider)chipProvider).getMandatoryTags();
			setItemValues(new ArrayList<>(mandatoryTags), true);
		}
	}

	protected void validateTags(ValueChangeEvent<List<? extends Suggestion>> event) {
		GWT.log("validate tags");
		clearErrorText();
		if (event.getValue().size() > maxTags) {
			GWT.log("MAX TAGS ERROR");
			setErrorText("MAX TAGS: " + maxTags);
		}
		Set<String> already = new HashSet<>();
		for (Suggestion tag : event.getValue()) {
			if (already.contains(tag.getReplacementString())) {
				GWT.log("DUPLICATE TAGS ERROR");
				setErrorText("DUPLICATE TAGS DETECTED");
				break;
			}
			already.add(tag.getReplacementString());
		}
	}
	
	public TagAutoComplete() {
		super(AutocompleteType.CHIP);
		setPlaceholder("TAGS");
		setAllowBlank(false);
		setAutoValidate(false);
		setLimit(maxTags);
		setAutoSuggestLimit(8);
		addValueChangeHandler((event)->validateTags(event));
		setChipProvider(new DpornChipProvider());
	}

	public void setMandatoryTags(Set<String> mandatoryTags) {
		MaterialChipProvider chipProvider = getChipProvider();
		if (chipProvider instanceof DpornChipProvider) {
			((DpornChipProvider)chipProvider).setMandatoryTags(mandatoryTags);
			List<String> itemValues = getItemValues();
			clear();
			Set<String> tags = new LinkedHashSet<>();
			tags.addAll(mandatoryTags);
			tags.addAll(itemValues);
			setItemValues(new ArrayList<>(tags), true);
		}
	}
}
