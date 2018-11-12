package steem.connect.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.shared.GWT;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonNaming(SnakeCaseStrategy.class)
public class SteemAccountMetadata {
	protected static interface Mapper extends ObjectMapper<SteemAccountMetadata> {}
	public static SteemAccountMetadata deserialize(String json) {
		try {
			return ((Mapper)GWT.create(Mapper.class)).read(json);
		} catch (JsonDeserializationException e) {
			GWT.log(e.getMessage(), e);
			return null;
		}
	}
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonNaming(SnakeCaseStrategy.class)
	public static class AccountProfile {
		private String name;
		private String about;
		private String location;
		private String profileImage;
		private String coverImage;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getAbout() {
			return about;
		}
		public void setAbout(String about) {
			this.about = about;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public String getProfileImage() {
			return profileImage;
		}
		public void setProfileImage(String profileImage) {
			this.profileImage = profileImage;
		}
		public String getCoverImage() {
			return coverImage;
		}
		public void setCoverImage(String coverImage) {
			this.coverImage = coverImage;
		}
		public String getWebsite() {
			return website;
		}
		public void setWebsite(String website) {
			this.website = website;
		}
		private String website;
	}
	private AccountProfile profile;
	public AccountProfile getProfile() {
		return profile;
	}
	public void setProfile(AccountProfile profile) {
		this.profile = profile;
	}
}
