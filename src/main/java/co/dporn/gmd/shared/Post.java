package co.dporn.gmd.shared;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_NULL)
public class Post {
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
	private Date created;
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	private String author;
	private String permlink;
	private String title;
	private String coverImage;
	private String posterImagePath;
	private String videoPath;
	private String id;
	private List<String> tags;
	private double score;
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getPermlink() {
		return permlink;
	}
	public void setPermlink(String permlink) {
		this.permlink = permlink;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCoverImage() {
		return coverImage;
	}
	public void setCoverImage(String coverImage) {
		this.coverImage = coverImage;
	}
	public String getId() {
		return id;
	}
	public void setId(String string) {
		this.id = string;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getPosterImagePath() {
		return posterImagePath;
	}
	public void setPosterImagePath(String posterImagePath) {
		this.posterImagePath = posterImagePath;
	}
	public String getVideoPath() {
		return videoPath;
	}
	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
