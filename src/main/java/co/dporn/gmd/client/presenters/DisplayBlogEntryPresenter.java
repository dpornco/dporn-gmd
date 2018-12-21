package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.views.IsView;

public interface DisplayBlogEntryPresenter extends ContentPresenter, ScheduledCommand {
	interface DisplayBlogEntryView extends ContentView {
		@Override
		IsWidget getContainer();
		@Override
		void showLoading(boolean loading);
		void setEmbedUrl(String embedUrl);
		void setTitle(String title);
		void setBodyMessage(String body);
		void setCommentRepliesView(CommentRepliesView view);
		void setPostDetails(PostDetails details);
		void showUserNotFound(String username);
		BlogHeader getBlogHeader();
		HasWidgets getPostView();
	}
	interface CommentRepliesView extends IsView<DisplayBlogEntryPresenter> {}
	interface PostDetails {}
	void upvote(int percent);
	void downvote(int percent);
	void report(String reason);
	void reply(String reply);
}
