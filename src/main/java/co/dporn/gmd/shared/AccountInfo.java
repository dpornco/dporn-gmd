package co.dporn.gmd.shared;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class AccountInfo {
	private String about;

	private String coverImage;

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
	private Date created;

	private String displayName;

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
	private Date lastRootPost;

	private String location;

	private String profileImage;

	private String website;

	public String getAbout() {
		return about;
	}

	public String getCoverImage() {
		return coverImage;
	}
	public Date getCreated() {
		return created;
	}

	public String getDisplayName() {
		return displayName;
	}
	public Date getLastRootPost() {
		return lastRootPost;
	}

	public String getLocation() {
		return location;
	}
	public String getProfileImage() {
		return profileImage;
	}

	public String getWebsite() {
		return website;
	}
	public void setAbout(String about) {
		this.about=about;
	}

	public void setCoverImage(String coverImage) {
		this.coverImage=coverImage;
	}
	public void setCreated(Date created) {
		this.created = created;
	}

	public void setDisplayName(String name) {
		this.displayName=name;
	}
	public void setLastRootPost(Date lastRootPost) {
		this.lastRootPost = lastRootPost;
	}

	public void setLocation(String location) {
		this.location=location;
	}
	public void setProfileImage(String profileImage) {
		this.profileImage=profileImage;
	}

	public void setWebsite(String website) {
		this.website=website;
	}
	
}
