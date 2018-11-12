package steem.connect.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * We only deserialize the portions of the account information that are of
 * interest to the UI and Application Model
 * 
 * @author muksihs
 *
 */
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteemAccount {
	private String name;
	@JsonProperty("json_metadata")
	private String jsonMetadata;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
	private Date created;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJsonMetadata() {
		return jsonMetadata;
	}

	public void setJsonMetadata(String jsonMetadata) {
		this.jsonMetadata = jsonMetadata;
	}

	/**
	 * Returns a COPY of the account's metadata as an object.
	 * 
	 * @return
	 */
	@JsonIgnore
	public SteemAccountMetadata getMetadata() {
		return SteemAccountMetadata.deserialize(getJsonMetadata());
	}
}
