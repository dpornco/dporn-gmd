package co.dporn.gmd.client.views;

import java.util.List;

public interface CommonUploadViewFeatures {
	String getTitle();
	void setTitle(String title);
	String getBody();
	void setAlwaysTags(List<String> alwaysTags);
}
