package co.dporn.gmd.shared;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MongoId {
	private String oid;

	public MongoId() {
	}

	public MongoId(String oid) {
		this.oid = oid;
	}

	@JsonProperty("$oid")
	public String getOid() {
		return oid;
	}

	@JsonProperty("$oid")
	public void setOid(String $oid) {
		this.oid = $oid;
	}
}
