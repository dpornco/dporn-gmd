package co.dporn.gmd.shared;

@SuppressWarnings("serial")
public class CommentNotFoundException extends RuntimeException {
	public CommentNotFoundException() {
		this(false);
	}
	public CommentNotFoundException(boolean deleted) {
		this.deleted=deleted;
	}
	private boolean deleted;

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
