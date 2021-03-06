package co.dporn.gmd.client.views;

import java.math.BigDecimal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.ContentPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.VideoCardView;
import gwt.material.design.client.ui.MaterialCard;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialVideo;
import gwt.material.design.client.ui.animate.MaterialAnimation;
import gwt.material.design.client.ui.animate.Transition;

public class VideoCardUi extends Composite implements VideoCardView, CanBeDeleted {

	@UiField
	VoteBarUI voteBarUi;
	@UiField
	protected MaterialVideo videoEmbedUrl;
	@UiField
	protected MaterialLabel authorName;
	@UiField
	protected MaterialLabel authorBlurb;
	@UiField
	protected MaterialCard card;
	@UiField
	protected DpornLink viewLink;
	@UiField
	protected DpornLink viewChannel;

	private int showDelay;

	private static VideoCardUiUiBinder uiBinder = GWT.create(VideoCardUiUiBinder.class);

	interface VideoCardUiUiBinder extends UiBinder<Widget, VideoCardUi> {
	}

	public VideoCardUi() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		MaterialAnimation animation = new MaterialAnimation();
		animation.setTransition(Transition.ZOOMIN);
		animation.setDelay(0);
		animation.setDuration(250 + showDelay);
		animation.animate(card);
	}

	@Override
	public void setImageUrl(String url) {
		// do nothing
	}

	@Override
	public void setAvatarUrl(String url) {
		// avatarImage.setUrl(url);
	}

	@Override
	public void setDisplayName(String name) {
		authorName.setText(name);
	}

	@Override
	public void setTitle(String blurb) {
		authorBlurb.setText(blurb);
	}

	@Override
	public void setShowDelay(int showDelay) {
		this.showDelay = showDelay;
	}

	@Override
	public void bindPresenter(ContentPresenter presenter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unbindPresenter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVideoEmbedUrl(String url) {
		videoEmbedUrl.setUrl(url);
	}

	@Override
	public void setViewLink(String linkUrl) {
		viewLink.setHref(linkUrl);
	}

	@Override
	public void setChannelLink(String linkUrl) {
		viewChannel.setHref(linkUrl);
	}

	@Override
	public void setChannelLinkVisible(boolean visible) {
		viewChannel.setVisible(visible);
	}

	@Override
	public void setViewLinkVisible(boolean visible) {
		viewLink.setVisible(visible);
	}


	@Override
	public void setDeleted(boolean deleted) {
		if (deleted) {
			MaterialAnimation animation = new MaterialAnimation();
			animation.setTransition(Transition.ZOOMOUT);
			animation.setDelay(250);
			animation.setDuration(250 + showDelay);
			animation.animate(card);
			animation.animate(() -> removeFromParent());
		}
	}
	@Override
	public void setEarnings(BigDecimal earnings) {
		voteBarUi.setEarnings(earnings);
	}
	
	@Override
	public void setNetVoteCount(long netVoteCount) {
		voteBarUi.setNetVoteCount(netVoteCount);
	}

	@Override
	public void setVotedValue(int amount) {
		voteBarUi.setVotedValue(amount);
	}

	@Override
	public HandlerRegistration setUpvoteHandler(ClickHandler handler) {
		return voteBarUi.setUpvoteHandler(handler);
	}

	@Override
	public int getVotedValue() {
		return voteBarUi.getVotedValue();
	}

}
