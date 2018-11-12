package co.dporn.gmd.client.presenters;

public interface RoutePresenter {
	public static class ActiveUserInfo {
		private final String username;
		private final String displayname;
		public ActiveUserInfo(String username, String displayname) {
			this.username=username;
			this.displayname=displayname;
		}
		public String getUsername() {
			return username;
		}
		public String getDisplayname() {
			return displayname;
		}
	}
	void loadRoutePresenter(String route);
	void setUserInfo(ActiveUserInfo info);
}
