package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.presenters.PostPresenter.DisplayPostView;
import co.dporn.gmd.client.views.IsView;

public interface PostPresenter extends IsPresenter<DisplayPostView>, ScheduledCommand {
	interface DisplayPostView extends IsView<PostPresenter> {
		IsWidget getContainer();
		void showLoading(boolean loading);
		void setEmbedUrl(String embedUrl);
		void setPageTitle(String title);
		void setBodyMessage(String body);
		void setCommentRepliesView(CommentRepliesView view);
		void setPostDetails(PostDetails details);
	}
	interface CommentRepliesView extends IsView<PostPresenter> {}
	interface PostDetails {}
	void upvote(int percent);
	void downvote(int percent);
	void report(String reason);
	void reply(String reply);
}
