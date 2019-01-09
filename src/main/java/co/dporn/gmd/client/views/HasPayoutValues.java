package co.dporn.gmd.client.views;

import java.math.BigDecimal;

public interface HasPayoutValues {

	void setEarnings(BigDecimal earnings);

	default void setEarnings(String earnings) {
		BigDecimal bigDec;
		try {
			bigDec = new BigDecimal(earnings);
			setEarnings(bigDec);
		} catch (NumberFormatException e) {
		}
	}

	void setVoteCount(long count);

}
