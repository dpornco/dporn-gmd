package co.dporn.gmd.shared;

import java.util.List;
import java.util.Map;

public class BlogEntryListResponse {
	private List<BlogEntry> entries;
	private Map<String, AccountInfo> infoMap;

	public Map<String, AccountInfo> getInfoMap() {
		return infoMap;
	}

	public List<BlogEntry> getBlogEntries() {
		return entries;
	}

	public void setBlogEntries(List<BlogEntry> entries) {
		this.entries = entries;
	}

	public void setInfoMap(Map<String, AccountInfo> infoMap) {
		this.infoMap = infoMap;
	}
}
