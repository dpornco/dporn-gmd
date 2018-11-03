package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.ContentPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.VideoCardView;
import gwt.material.design.client.ui.MaterialCard;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialVideo;
import gwt.material.design.client.ui.animate.MaterialAnimation;
import gwt.material.design.client.ui.animate.Transition;

public class VideoCardUi extends Composite implements VideoCardView {

	@UiField
	protected MaterialVideo videoEmbedUrl;
	@UiField
	protected MaterialImage avatarImage;
	@UiField
	protected MaterialLabel authorName;
	@UiField
	protected MaterialLabel authorBlurb;
	@UiField
	protected MaterialCard card;
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
		GWT.log("onLoad");
		MaterialAnimation animation = new MaterialAnimation();
		animation.setTransition(Transition.ZOOMIN);
		animation.setDelay(0);
		animation.setDuration(250+showDelay);
		animation.animate(card);
	}

	@Override
	public void setImageUrl(String url) {
		//do nothing
	}
	
	@Override
	public void setAvatarUrl(String url) {
		avatarImage.setUrl(url);
	}

	@Override
	public void setAuthorName(String name) {
		authorName.setText(name);
	}

	@Override
	public void setTitle(String blurb) {
		authorBlurb.setText(blurb);
	}

	@Override
	public void setShowDelay(int showDelay) {
		this.showDelay=showDelay;
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
		
	}

}
