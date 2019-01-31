package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;

import co.dporn.gmd.client.utils.DpornChipProvider;
import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete;
import gwt.material.design.addins.client.autocomplete.constants.AutocompleteType;

public class TagAutoComplete extends MaterialAutoComplete {
	private int maxTags = 14;
	private HandlerRegistration blurRegistration;
	private HandlerRegistration itemBoxKeyDownHandler;

	public int getMaxTags() {
		return maxTags;
	}

	public void setMaxTags(int maxTags) {
		this.maxTags = maxTags;
	}

	@Override
	protected void setup(SuggestOracle suggestions) {
		registerCustomHandlers();
		super.setup(suggestions);
	}

	@Override
	public void reset() {
		super.reset();
		MaterialChipProvider chipProvider = getChipProvider();
		if (chipProvider != null && chipProvider instanceof DpornChipProvider) {
			Set<String> mandatoryTags = ((DpornChipProvider) chipProvider).getMandatoryTags();
			setItemValues(new ArrayList<>(mandatoryTags), true);
		}
	}

	protected void validateTags(ValueChangeEvent<List<? extends Suggestion>> event) {
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
			if (tag.getReplacementString().contains(" ")) {
				GWT.log("TAGS MAY NOT CONTAIN SPACES");
				setErrorText("TAGS MAY NOT CONTAIN SPACES");
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
		addValueChangeHandler((event) -> validateTags(event));
		setChipProvider(new DpornChipProvider());
	}

	private void doChip(KeyDownEvent e) {
		if (getType() == AutocompleteType.TEXT) {
			return;
		}
		if (!isDirectInputAllowed()) {
			return;
		}
		switch (e.getNativeKeyCode()) {
		case KeyCodes.KEY_ENTER:
		case KeyCodes.KEY_MAC_ENTER:
		case ';':
		case 188: // ','
		case ' ':
			//DomEvent.fireNativeEvent(Document.get().createKeyDownEvent(false, false, false, false, KeyCodes.KEY_ENTER), getItemBox());
			e.preventDefault();
			e.stopPropagation();
			doChip();
			break;
		}
	}

	private void doChip(BlurEvent e) {
		if (getType() == AutocompleteType.TEXT) {
			return;
		}
		if (!isDirectInputAllowed()) {
			return;
		}
		doChip();
	}

	private void doChip() {
		TextBox itemBox = getItemBox();
		String value = itemBox.getValue();
		itemBox.setValue("");
		if (value != null && !(value = value.trim()).isEmpty()) {
			String[] chipValues = value.split("[\\s,;]+");
			if (chipValues != null) {
				for (String chipValue : chipValues) {
					chipValue = chipValue.toLowerCase();
					gwt.material.design.client.base.Suggestion directInput = new gwt.material.design.client.base.Suggestion();
					directInput.setDisplay(chipValue);
					directInput.setSuggestion(chipValue);
					addItem(directInput);
				}
			}
		}
	}

	@Override
	protected void onLoad() {
		registerCustomHandlers();
		super.onLoad();
		getSuggestBox().setAutoSelectEnabled(false);
	}

	@Override
	protected void loadHandlers() {
		registerCustomHandlers();
		super.loadHandlers();
	}

	private void registerCustomHandlers() {
		if (itemBoxKeyDownHandler == null) {
			itemBoxKeyDownHandler = getItemBox().addKeyDownHandler(this::doChip);
		}
		if (blurRegistration == null) {
			blurRegistration = getItemBox().addBlurHandler(this::doChip);
		}
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		unregisterCustomHandlers();
	}

	private void unregisterCustomHandlers() {
		removeHandler(itemBoxKeyDownHandler);
		removeHandler(blurRegistration);
	}

	public void setMandatoryTags(Set<String> mandatoryTags) {
		MaterialChipProvider chipProvider = getChipProvider();
		if (chipProvider instanceof DpornChipProvider) {
			((DpornChipProvider) chipProvider).setMandatoryTags(mandatoryTags);
			List<String> itemValues = getItemValues();
			clear();
			Set<String> tags = new LinkedHashSet<>();
			tags.addAll(mandatoryTags);
			tags.addAll(itemValues);
			setItemValues(new ArrayList<>(tags), true);
		}
	}
}
