package co.dporn.gmd.client.views;

import java.util.List;

public interface CommonUploadViewFeatures {
	String getTitle();
	void setTitle(String title);
	String getBody();
	void setBody(String body);
	List<String> getTags();
	void setTags(List<String> tags);
	String getCoverImage();
	void setCoverImage(String coverImage);
	boolean isNoCoverWanted();
	void setNoCoverWanted(boolean noCoverWanted);
	
	void setAlwaysTags(List<String> alwaysTags);
	void showPreviousTags(List<String> tags);
	void showStockCoverImages(List<String> imageUrls);
}
