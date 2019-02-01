package co.dporn.gmd.client.views;

import java.math.BigDecimal;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public interface HasVoting {

	HandlerRegistration setUpvoteHandler(ClickHandler handler);

	void setEarnings(BigDecimal earnings);

	default void setEarnings(String earnings) {
		if (earnings==null) {
			setEarnings(BigDecimal.ONE.negate());
			return;
		}
		earnings=earnings.trim();
		if (earnings.contains(" ")) {
			earnings = earnings.replaceAll(" .*", "");
		}
		if (earnings.isEmpty()) {
			setEarnings(BigDecimal.ONE.negate());
			return;
		}
		BigDecimal bigDec;
		try {
			bigDec = new BigDecimal(earnings);
			setEarnings(bigDec);
		} catch (NumberFormatException e) {
			setEarnings(BigDecimal.ONE.negate());
		}
	}

	void setNetVoteCount(long netVotes);

	void setVotedValue(int amount);
	int getVotedValue();
}
