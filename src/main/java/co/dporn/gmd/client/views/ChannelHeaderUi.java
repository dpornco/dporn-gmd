package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.ContentPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.BlogCardView;
import co.dporn.gmd.client.presenters.ContentPresenter.BlogHeader;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.ui.MaterialHeader;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialPanel;

public class ChannelHeaderUi extends Composite implements BlogCardView, BlogHeader {
	@UiField
	protected MaterialHeader header;
	@UiField
	protected MaterialPanel avatarImage;
	@UiField
	protected MaterialLabel displayName;
	@UiField
	protected MaterialLabel authorBlurb;

	private static _UiBinder uiBinder = GWT.create(_UiBinder.class);

	interface _UiBinder extends UiBinder<Widget, ChannelHeaderUi> {
	}

	public ChannelHeaderUi() {
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
	}

	@Override
	public void setImageUrl(String coverImage) {
		if (coverImage == null || coverImage.trim().isEmpty()) {
			header.setBackgroundColor(Color.GREY_DARKEN_3);
			return;
		}
		coverImage = coverImage.trim();

		if (coverImage.matches("https?://steemitimages.com/\\d+x\\d+/.*")) {
			coverImage = coverImage.replaceFirst("^https?://steemitimages.com/\\d+x\\d+/", "");
		}
		coverImage = "https://steemitimages.com/2048x512/" + coverImage;

		if (!coverImage.toLowerCase().startsWith("url")) {
			coverImage = "url(\"" + coverImage + "\")";
		}
		header.getElement().getStyle().setBackgroundImage(coverImage);
	}

	@Override
	public void setAvatarUrl(String url) {
		url = url.trim();
		if (!url.toLowerCase().startsWith("url")) {
			url = "url(\"" + url + "\")";
		}
		avatarImage.getElement().getStyle().setBackgroundImage(url);
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
	}

	@Override
	public void setViewLink(String linkUrl) {
	}

	@Override
	public void setAbout(String about) {
		authorBlurb.setText(about);
	}
}
