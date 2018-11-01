package co.dporn.gmd.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonNaming(SnakeCaseStrategy.class)
public class AccountMetadata {
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
