package co.dporn.gmd.client.utils;

import java.math.BigDecimal;

import com.google.gwt.core.client.GWT;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.CanBeDeleted;
import co.dporn.gmd.client.views.HasVoting;
import co.dporn.gmd.shared.CommentNotFoundException;

public class SteemDataUtil {
	public static <T extends HasVoting & CanBeDeleted> void updateCardMetadata(AppControllerModel model,
			String username, String permlink, T card) {
		model.getDiscussionComment(username, permlink).thenAccept((comment) -> {
			if (comment.getNetVotes() != null) {
				card.setNetVoteCount(comment.getNetVotes().longValue());
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
