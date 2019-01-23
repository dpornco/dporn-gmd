package co.dporn.gmd.client.views;

import java.math.BigDecimal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.CurrencyData;
import com.google.gwt.i18n.client.CurrencyList;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialRange;
import gwt.material.design.client.ui.MaterialRow;

public class VoteBarUI extends Composite implements HasVoting {

	@UiField
	protected MaterialRow voteBarDisplayAmounts;
	@UiField
	protected MaterialRow voteBarControls;
	
	@UiField
	protected MaterialButton btnThumbsUp;
	@UiField
	protected MaterialLabel lblVoteCountUp;
	@UiField
	protected MaterialLabel lblEarnings;
	
	@UiField
	protected MaterialRange voteWeight;
	@UiField
	protected MaterialButton btnConfirm;
	@UiField
	protected MaterialButton btnCancel;
	
	private HandlerRegistration voteUpRegistration;
	private final NumberFormat voteCountFormatter;
	private final NumberFormat sbdValueFormatter;

	private static VoteBarUI2UiBinder uiBinder = GWT.create(VoteBarUI2UiBinder.class);

	interface VoteBarUI2UiBinder extends UiBinder<Widget, VoteBarUI> {
	}

	private static class NF extends NumberFormat {
		protected NF(String pattern, CurrencyData cdata, boolean userSuppliedPattern) {
			super(pattern, cdata, userSuppliedPattern);
		}

		/**
		 * Provides a new standard decimal format for the default locale. NOT A CACHED SINGLE INSTANCE!
		 *
		 * @return a <code>NumberFormat</code> capable of producing and consuming
		 *         decimal format for the default locale
		 */
		public static NumberFormat getDecimalFormat() {
			return new NF(defaultNumberConstants.decimalPattern(), CurrencyList.get().getDefault(), false);
		}
	}

	public VoteBarUI() {
		initWidget(uiBinder.createAndBindUi(this));
		voteCountFormatter = NF.getDecimalFormat().overrideFractionDigits(0);
		sbdValueFormatter = NF.getDecimalFormat().overrideFractionDigits(3, 3);
		setEarnings(BigDecimal.ZERO);
		setNetVoteCount(0);
		setVotedValue(0);
		btnThumbsUp.setEnabled(false);
		voteBarDisplayAmounts.setVisible(true);
		voteBarControls.setVisible(false);
		btnThumbsUp.addClickHandler((e)->showVoting(true));
//		btnConfirm.addClickHandler(this::doUpvote);
		btnCancel.addClickHandler((e)->showVoting(false));
	}
	
	private void showVoting(boolean showVoteControls) {
		voteBarDisplayAmounts.setVisible(!showVoteControls);
		voteBarControls.setVisible(showVoteControls);
	}

	@Override
	public void setVotedValue(int amount) {
		if (amount<0) {
			amount=0;
		}
		if (amount>100) {
			amount=100;
		}
		voteWeight.setValue(amount, false, true);
	}

	@Override
	public void setEarnings(BigDecimal earnings) {
		lblEarnings.setText("$ " + sbdValueFormatter.format(earnings));
	}

	@Override
	public void setNetVoteCount(long netVotes) {
		lblVoteCountUp.setText(voteCountFormatter.format(netVotes));
	}

	@Override
	public HandlerRegistration setUpvoteHandler(ClickHandler handler) {
		if (voteUpRegistration != null) {
			voteUpRegistration.removeHandler();
		}
		if (handler==null) {
			btnThumbsUp.setEnabled(false);
			return null;
		}
		voteUpRegistration = btnThumbsUp.addClickHandler(handler);
		btnThumbsUp.setEnabled(true);
		return voteUpRegistration;
	}
}
