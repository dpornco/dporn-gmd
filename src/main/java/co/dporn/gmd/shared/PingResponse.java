package co.dporn.gmd.shared;

public class PingResponse {
	private boolean pong;

	public PingResponse() {
	}
	
	public PingResponse(boolean pong) {
		pong=pong;
	}

	public boolean isPong() {
		return pong;
	}

	public void setPong(boolean pong) {
		this.pong = pong;
	}
}
