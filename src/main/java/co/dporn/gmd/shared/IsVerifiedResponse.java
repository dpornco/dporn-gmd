package co.dporn.gmd.shared;

public class IsVerifiedResponse {
	private String username;
	private boolean verified;
	public IsVerifiedResponse() {
	}
	public IsVerifiedResponse(String username, boolean verified) {
		this.username=username;
		this.verified=verified;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public boolean isVerified() {
		return verified;
	}
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
}
