package co.dporn.gmd.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PingResponse {
	private boolean pong;

	public PingResponse() {
	}

	public PingResponse(boolean pong) {
		this.pong = pong;
	}

	public boolean isPong() {
		return pong;
	}

	public void setPong(boolean pong) {
		this.pong = pong;
	}
}
