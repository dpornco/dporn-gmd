package co.dporn.gmd.shared;

public class IpfsHashResponse {
	private String ipfsHash;
	private String filename;
	private String location;
	private String error;
	private boolean tryAgain;

	public String getIpfsHash() {
		return ipfsHash;
	}

	public void setIpfsHash(String ipfsHash) {
		this.ipfsHash = ipfsHash;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isTryAgain() {
		return tryAgain;
	}

	public void setTryAgain(boolean tryAgain) {
		this.tryAgain = tryAgain;
	}
}
