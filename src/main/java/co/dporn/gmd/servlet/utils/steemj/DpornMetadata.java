package co.dporn.gmd.servlet.utils.steemj;

import java.util.List;

import co.dporn.gmd.shared.BlogEntryType;

public class DpornMetadata {
	private String app;
	private BlogEntryType entryType;
	private String embed;
	private List<String> photoGalleryImagePaths;
	private String posterImagePath;
	private String videoPath;
	public String getApp() {
		return app;
	}
	public String getEmbed() {
		return embed;
	}
	public List<String> getPhotoGalleryImagePaths() {
		return photoGalleryImagePaths;
	}
	public String getPosterImagePath() {
		return posterImagePath;
	}
	public String getVideoPath() {
		return videoPath;
	}
	public void setApp(String app) {
		this.app = app;
	}
	public void setEmbed(String embed) {
		this.embed = embed;
	}
	public void setPhotoGalleryImagePaths(List<String> photoGalleryImagePaths) {
		this.photoGalleryImagePaths = photoGalleryImagePaths;
	}
	public void setPosterImagePath(String posterImagePath) {
		this.posterImagePath = posterImagePath;
	}
	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}
	public BlogEntryType getEntryType() {
		return entryType;
	}
	public void setEntryType(BlogEntryType entryType) {
		this.entryType = entryType;
	}
}
