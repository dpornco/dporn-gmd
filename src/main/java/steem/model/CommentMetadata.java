package steem.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;

@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value=Include.NON_NULL)
public class CommentMetadata {

	protected static interface MapperCommentMetadata extends ObjectMapper<CommentMetadata> {
		static MapperCommentMetadata mapper = GWT.create(MapperCommentMetadata.class);
	}

	public static CommentMetadata fromJson(String json) {
		try {
			return MapperCommentMetadata.mapper.read(json);
		} catch (JsonDeserializationException e) {
			return new CommentMetadata();
		}
	}

	public static CommentMetadata newInstance() {
		return new CommentMetadata();
	}

	private String app;
	private String canonical;
	private String community;
	private String embedLink;
	private String format;
	private List<String> image;
	private String ipfsHlsVideoStream;
	private String ipfsPosterImage;
	private String ipfsVideoStream;
	private List<String> links;
	private List<String> tags;
	private List<String> users;

	protected CommentMetadata() {
	}

	public String getApp() {
		return app;
	}

	public String getCanonical() {
		return canonical;
	}

	public String getCommunity() {
		return community;
	}

	public String getEmbedLink() {
		return embedLink;
	}

	public String getFormat() {
		return format;
	}

	public List<String> getImage() {
		return image;
	}

	public String getIpfsHlsVideoStream() {
		return ipfsHlsVideoStream;
	}

	public String getIpfsPosterImage() {
		return ipfsPosterImage;
	}

	public String getIpfsVideoStream() {
		return ipfsVideoStream;
	}

	public List<String> getLinks() {
		return links;
	}

	public List<String> getTags() {
		return tags;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public void setCanonical(String canonical) {
		this.canonical = canonical;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public void setEmbedLink(String embedLink) {
		this.embedLink = embedLink;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setImage(List<String> image) {
		this.image = image;
	}

	public void setIpfsHlsVideoStream(String ipfsHlsVideoStream) {
		this.ipfsHlsVideoStream = ipfsHlsVideoStream;
	}

	public void setIpfsPosterImage(String ipfsPosterImage) {
		this.ipfsPosterImage = ipfsPosterImage;
	}

	public void setIpfsVideoStream(String ipfsVideoStream) {
		this.ipfsVideoStream = ipfsVideoStream;
	}

	public void setLinks(List<String> links) {
		this.links = links;
	};

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}
}
