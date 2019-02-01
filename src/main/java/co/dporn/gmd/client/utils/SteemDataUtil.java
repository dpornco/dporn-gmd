package co.dporn.gmd.client.utils;

import java.math.BigDecimal;
import com.google.gwt.core.client.GWT;

import co.dporn.gmd.client.ViewEvents;
import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.CanBeDeleted;
import co.dporn.gmd.client.views.HasVoting;
import co.dporn.gmd.shared.CommentNotFoundException;
import steem.model.Vote;

public class SteemDataUtil {

	public static <T extends HasVoting & CanBeDeleted> void enableAndUpdateCardVoting(AppControllerModel model,
			String author, String permlink, T card) {
		String thisUser = model.getUsername()==null?"":model.getUsername();

		/*
		 * If logged in, enable real voting, else have vote trigger "login".
		 */
		if (model.isLoggedIn()) {
			card.setUpvoteHandler(event -> {
				model.doVote(author, permlink, card.getVotedValue()).thenAccept((t)->{
					model.fireEvent(new ViewEvents.DoNotifyMessage("Voted!"));
					enableAndUpdateCardVoting(model, author, permlink, card);
				}).exceptionally((ex)->{
					model.fireEvent(new ViewEvents.DoNotifyMessage(ex.getMessage()));
					enableAndUpdateCardVoting(model, author, permlink, card);
					return null;
				});
			});
		} else {

		}

		/*
		 * Update voted values for "this user" and report existing vote counts and SBD amounts.
		 */
		model.getDiscussionComment(author, permlink).thenAccept((comment) -> {
			if (comment.getNetVotes() != null) {
				card.setNetVoteCount(comment.getNetVotes().longValue());
			}
			if (comment.getActiveVotes()!=null) {
				for (Vote vote: comment.getActiveVotes()) {
					if (vote.getVoter().equalsIgnoreCase(thisUser)) {
						if (vote.getPercent()!=null) {
							card.setVotedValue(vote.getPercent().intValue()/100);
						}
					}
				}
			}
			BigDecimal ppv;
			try {
				ppv = new BigDecimal(comment.getPendingPayoutValue().replaceAll(" .*", ""));
			} catch (Exception e) {
				GWT.log(e.getMessage(), e);
				ppv = BigDecimal.ZERO;
			}
			BigDecimal tpv;
			try {
				tpv = new BigDecimal(comment.getTotalPayoutValue().replaceAll(" .*", ""));
			} catch (Exception e) {
				GWT.log(e.getMessage(), e);
				tpv = BigDecimal.ZERO;
			}
			if (ppv.compareTo(tpv) > 0) {
				card.setEarnings(ppv);
			} else {
				card.setEarnings(tpv);
			}
		}).exceptionally((ex) -> {
			card.setNetVoteCount(0);
			if (ex instanceof CommentNotFoundException) {
				card.setDeleted(((CommentNotFoundException) ex).isDeleted());
			}
			return null;
		});
	}
}
