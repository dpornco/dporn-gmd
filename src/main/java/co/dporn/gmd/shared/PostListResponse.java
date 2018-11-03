package co.dporn.gmd.shared;

import java.util.List;
import java.util.Map;

public class PostListResponse {
	private List<Post> posts;
	private Map<String, AccountInfo> infoMap;

	public Map<String, AccountInfo> getInfoMap() {
		return infoMap;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public void setInfoMap(Map<String, AccountInfo> infoMap) {
		this.infoMap = infoMap;
	}
}
