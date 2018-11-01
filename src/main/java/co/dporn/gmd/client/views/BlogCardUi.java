package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.ContentPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.BlogCardView;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialLabel;

public class BlogCardUi extends Composite implements BlogCardView {
	@UiField
	protected MaterialImage postImage;
	@UiField
	protected MaterialImage avatarImage;
	@UiField
	protected MaterialLabel authorName;
	@UiField
	protected MaterialLabel authorBlurb;

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
	public void setImageUrl(String url) {
		postImage.setUrl(url);
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
	public void setBlurb(String blurb) {
		authorBlurb.setText(blurb);
	}

}
