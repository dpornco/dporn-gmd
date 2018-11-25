package co.dporn.gmd.shared;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MongoDate {
	private Date date;

	public MongoDate() {
	}

	public MongoDate(Date date) {
		this.date = date;
	}

	@JsonProperty("$date")
	public Date getDate() {
		return date;
	}

	@JsonProperty("$date")
	public void setDate(Date $date) {
		this.date = $date;
	}
}
