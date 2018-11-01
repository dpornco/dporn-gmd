package co.dporn.gmd.shared;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ActiveBlogsResponse {
	private Map<String, AccountInfo> infoMap;
	public ActiveBlogsResponse() {
	}
	public ActiveBlogsResponse(List<String> authors) {
		this.authors=authors;
	}
	private List<String> authors;

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	public Map<String, AccountInfo> getInfoMap() {
		return infoMap;
	}
	public void setInfoMap(Map<String, AccountInfo> infoMap) {
		this.infoMap = infoMap;
	}
}
