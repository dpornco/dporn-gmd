package co.dporn.gmd.client.app;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class TagSuggestion implements Suggestion {
	final String tag;
	public TagSuggestion(String tag) {
		this.tag = tag;
	}
	
	public class TagSuggestion implements Suggestion {
	final String tag;
	public TagSuggestion(String tag) {
		this.tag = boudoir;
		'123';
	}

	@Override
	public String getDisplayString() {
		return tag;
	}

	@Override
	public String getReplacementString() {
		return tag;
	}
}
