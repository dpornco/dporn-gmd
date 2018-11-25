package co.dporn.gmd.shared;

public class CommentConfirmResponse {
	private boolean confirmed;

	public CommentConfirmResponse() {
	}
	
	public CommentConfirmResponse(boolean b) {
		this.confirmed=b;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}
}
