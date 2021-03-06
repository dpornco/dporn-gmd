package co.dporn.gmd.client.views;

import java.math.BigDecimal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.ContentPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.BlogHeader;
import co.dporn.gmd.client.presenters.DisplayBlogEntryPresenter.CommentRepliesView;
import co.dporn.gmd.client.presenters.DisplayBlogEntryPresenter.DisplayBlogEntryView;
import co.dporn.gmd.client.presenters.DisplayBlogEntryPresenter.PostDetails;
import gwt.material.design.client.constants.TextAlign;
import gwt.material.design.client.ui.MaterialContainer;
import gwt.material.design.client.ui.MaterialHeader;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialProgress;
import gwt.material.design.client.ui.MaterialRow;
import gwt.material.design.client.ui.MaterialVideo;

public class DisplayBlogEntryUi extends Composite implements DisplayBlogEntryView {
	
	@UiField
	protected MaterialProgress progress;

	@UiField
	protected MaterialLabel title;

	@UiField
	protected MaterialContainer mainContent;
	
	@UiField
	protected ChannelHeaderUi header;
	
	@UiField
	protected MaterialRow post;
	
	@UiField
	protected MaterialRow comments;
	
	@UiField
	protected VoteBarUI voteBarUi;

	@Override
	public IsWidget getContainer() {
		return mainContent;
	}

	private static ChannelUiBinder uiBinder = GWT.create(ChannelUiBinder.class);

	interface ChannelUiBinder extends UiBinder<Widget, DisplayBlogEntryUi> {
	}

	public DisplayBlogEntryUi() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	protected void onLoad() {
		super.onLoad();
	}

	@Override
	public void unbindPresenter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void showLoading(boolean loading) {
		progress.setVisible(loading);
	}

	@Override
	public BlogHeader getBlogHeader() {
		return header;
	}

	@Override
	public void showUserNotFound(String username) {
		mainContent.clear();
		MaterialLabel notFound = new MaterialLabel("USER NOT FOUND: @"+username);
		MaterialHeader materialHeader = new MaterialHeader();
		materialHeader.setFontWeight(FontWeight.BOLD);
		materialHeader.setFontSize(200, Unit.PCT);
		materialHeader.setTextAlign(TextAlign.CENTER);
		materialHeader.add(notFound);
		mainContent.add(materialHeader);
	}

	@Override
	public void setEmbedUrl(String embedUrl) {
		post.clear();
		MaterialVideo video = new MaterialVideo(embedUrl);
		video.setFullscreen(true);
		post.add(video);
	}

	@Override
	public void setTitle(String title) {
		this.title.setText(title);
	}

	@Override
	public void setBodyMessage(String body) {
		post.clear();
		MaterialRow row = new MaterialRow();
		row.setMargin(0);
		row.setPadding(16);
		row.getElement().setInnerHTML(body);
		post.add(row);
	}

	@Override
	public void setCommentRepliesView(CommentRepliesView view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPostDetails(PostDetails details) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HasWidgets getFeaturedChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HasWidgets getFeaturedPosts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HasWidgets getRecentPosts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bindPresenter(ContentPresenter presenter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HasWidgets getPostView() {
		return post;
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

	@Override
	public void setDeleted(boolean deleted) {
		setVisible(false);
		Scheduler.get().scheduleDeferred(()->removeFromParent());
	}
}
