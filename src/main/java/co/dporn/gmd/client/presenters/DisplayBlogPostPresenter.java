package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.views.IsView;

public interface DisplayBlogPostPresenter extends ContentPresenter, ScheduledCommand {
	interface DisplayBlogPostView extends ContentView {
		IsWidget getContainer();
		void showLoading(boolean loading);
		void setEmbedUrl(String embedUrl);
		void setTitle(String title);
		void setBodyMessage(String body);
		void setCommentRepliesView(CommentRepliesView view);
		void setPostDetails(PostDetails details);
		void showUserNotFound(String username);
		BlogHeader getBlogHeader();
	}
	interface CommentRepliesView extends IsView<DisplayBlogPostPresenter> {}
	interface PostDetails {}
	void upvote(int percent);
	void downvote(int percent);
	void report(String reason);
	void reply(String reply);
}
