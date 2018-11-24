package co.dporn.gmd.shared;

import java.util.Date;

public class MongoDate {
	private Date $date;

	public MongoDate() {
	}

	public MongoDate(Date date) {
		this.$date = date;
	}

	public Date get$date() {
		return $date;
	}

	public void set$date(Date $date) {
		this.$date = $date;
	}
}
