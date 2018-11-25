package co.dporn.gmd.shared;

import java.util.List;

public class BlogEntry {
	private MongoId _id;
	private String app;
	private String commentJsonMetadata;
	private List<String> communityTags;
	private String content;
	private MongoDate created;
	private String embed;
	private BlogEntryType entryType=BlogEntryType.VIDEO;
	private List<String> galleryImagePaths;
	private List<String> galleryImageThumbPaths;
	private boolean migrated;
	private MongoDate modified;
	private String permlink;
	private String posterImagePath;
	private List<String> postTags;
	private double score;
	private String title;
	private String username;
	private String videoPath;
	public MongoId get_id() {
		return _id;
	}
	public String getApp() {
		return app;
	}
	public String getCommentJsonMetadata() {
		return commentJsonMetadata;
	}
	public List<String> getCommunityTags() {
		return communityTags;
	}
	public String getContent() {
		return content;
	}
	public MongoDate getCreated() {
		return created;
	}
	public String getEmbed() {
		return embed;
	}
	public BlogEntryType getEntryType() {
		return entryType;
	}
	public List<String> getGalleryImagePaths() {
		return galleryImagePaths;
	}
	public List<String> getGalleryImageThumbPaths() {
		return galleryImageThumbPaths;
	}
	public MongoDate getModified() {
		return modified;
	}
	public String getPermlink() {
		return permlink;
	}
	public String getPosterImagePath() {
		return posterImagePath;
	}
	public List<String> getPostTags() {
		return postTags;
	}
	public double getScore() {
		return score;
	}
	public String getTitle() {
		return title;
	}
	public String getUsername() {
		return username;
	}
	public String getVideoPath() {
		return videoPath;
	}
	public boolean isMigrated() {
		return migrated;
	}
	public void set_id(MongoId _id) {
		this._id = _id;
	}
	public void setApp(String app) {
		this.app = app;
	}
	public void setCommentJsonMetadata(String commentJsonMetadata) {
		this.commentJsonMetadata = commentJsonMetadata;
	}
	public void setCommunityTags(List<String> communityTags) {
		this.communityTags = communityTags;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setCreated(MongoDate created) {
		this.created = created;
	}
	public void setEmbed(String embed) {
		this.embed = embed;
	}
	public void setEntryType(BlogEntryType entryType) {
		this.entryType = entryType;
	}
	public void setGalleryImagePaths(List<String> galleryImagePaths) {
		this.galleryImagePaths = galleryImagePaths;
	}
	public void setGalleryImageThumbPaths(List<String> galleryImageThumbPaths) {
		this.galleryImageThumbPaths = galleryImageThumbPaths;
	}
	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}
	public void setModified(MongoDate modified) {
		this.modified = modified;
	}
	public void setPermlink(String permlink) {
		this.permlink = permlink;
	}
	public void setPosterImagePath(String posterImagePath) {
		this.posterImagePath = posterImagePath;
	}
	public void setPostTags(List<String> postTags) {
		this.postTags = postTags;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}
}
