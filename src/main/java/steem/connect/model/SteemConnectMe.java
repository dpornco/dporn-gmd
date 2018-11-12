package steem.connect.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;

@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class SteemConnectMe {
	protected static interface Mapper extends ObjectMapper<SteemConnectMe> {}
	public static SteemConnectMe deserialize(String json) {
		try {
			return ((Mapper)GWT.create(Mapper.class)).read(json);
		} catch (JsonDeserializationException e) {
			GWT.log(e.getMessage(), e);
			return null;
		}
	}
	private String user;
	@JsonProperty("_id")
	private String id;
	private String name;
	private List<String> scope;
	private Map<String, Object> userMetadata;
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public SteemAccount getAccount() {
		return account;
	}
	public void setAccount(SteemAccount account) {
		this.account = account;
	}
	public List<String> getScope() {
		return scope;
	}
	public void setScope(List<String> scope) {
		this.scope = scope;
	}
	public Map<String, Object> getUserMetadata() {
		return userMetadata;
	}
	public void setUserMetadata(Map<String, Object> userMetadata) {
		this.userMetadata = userMetadata;
	}
	private SteemAccount account;
}
