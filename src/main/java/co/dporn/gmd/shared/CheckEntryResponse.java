package co.dporn.gmd.shared;

public class CheckEntryResponse {
	public CheckEntryResponse() {
		this(false);
	}
	public CheckEntryResponse(boolean deleted) {
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
