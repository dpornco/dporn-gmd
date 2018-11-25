package co.dporn.gmd.shared;

import java.util.List;
import java.util.Map;

public class PostListResponse {
	private List<BlogEntry> posts;
	private Map<String, AccountInfo> infoMap;

	public Map<String, AccountInfo> getInfoMap() {
		return infoMap;
	}

	public List<BlogEntry> getPosts() {
		return posts;
	}

	public void setPosts(List<BlogEntry> posts) {
		this.posts = posts;
	}

	public void setInfoMap(Map<String, AccountInfo> infoMap) {
		this.infoMap = infoMap;
	}
}
