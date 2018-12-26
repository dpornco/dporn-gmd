package co.dporn.gmd.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NotificationsResponse {
	private List<String> notifications;
	
	public NotificationsResponse() {
		notifications=new ArrayList<>();
	}

	public NotificationsResponse(Collection<String> notifications) {
		this.notifications=new ArrayList<>(notifications);
	}

	public List<String> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<String> notifications) {
		this.notifications = notifications;
	}
}
