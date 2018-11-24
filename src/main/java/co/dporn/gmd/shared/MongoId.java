package co.dporn.gmd.shared;

public class MongoId {
	private String $oid;

	public MongoId() {
	}

	public MongoId(String id) {
		this.$oid = id;
	}

	public String get$oid() {
		return $oid;
	}

	public void set$oid(String $oid) {
		this.$oid = $oid;
	}
}
