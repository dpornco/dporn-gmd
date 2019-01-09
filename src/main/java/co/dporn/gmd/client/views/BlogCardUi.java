package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.ContentPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.BlogCardView;
import gwt.material.design.client.ui.MaterialCard;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.animate.MaterialAnimation;
import gwt.material.design.client.ui.animate.Transition;

public class BlogCardUi extends Composite implements BlogCardView {
	@UiField
	protected VoteBarUI voteBarUi;
	@UiField
	protected MaterialImage postImage;
	@UiField
	protected MaterialImage avatarImage;
	@UiField
	protected MaterialLabel displayName;
	@UiField
	protected MaterialLabel authorBlurb;
	@UiField
	protected MaterialCard card;
	@UiField
	protected DpornLink viewLink;
	@UiField
	protected DpornLink viewChannel;

	private int showDelay;

	private static BlogCardUiUiBinder uiBinder = GWT.create(BlogCardUiUiBinder.class);

	interface BlogCardUiUiBinder extends UiBinder<Widget, BlogCardUi> {
	}

	public BlogCardUi() {
		initWidget(uiBinder.createAndBindUi(this));
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
		postImage.setUrl(url);
	}

	@Override
	public void setAvatarUrl(String url) {
		avatarImage.setUrl(url);
	}

	@Override
	public void setDisplayName(String name) {
		displayName.setText(name);
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
	public void setViewLink(String linkUrl) {
		this.viewLink.setHref(linkUrl);
	}

	@Override
	public void setChannelLink(String linkUrl) {
		this.viewChannel.setHref(linkUrl);
	}

	@Override
	public void setChannelLinkVisible(boolean visible) {
		this.viewChannel.setVisible(visible);
	}
	
	@Override
	public void setViewLinkVisible(boolean visible) {
		this.viewLink.setVisible(visible);
	}

	public void setVoteBarVisible(boolean visible) {
		voteBarUi.setVisible(visible);
	}
}
