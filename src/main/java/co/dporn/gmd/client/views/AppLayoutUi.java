package co.dporn.gmd.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.wallissoftware.pushstate.client.PushStateHistorian;

import co.dporn.gmd.client.presenters.AppPresenter;
import co.dporn.gmd.client.presenters.AppPresenter.AppLayoutView;
import co.dporn.gmd.client.presenters.AppPresenter.IsChildPresenter;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialSideNavPush;
import gwt.material.design.client.ui.MaterialToast;

public class AppLayoutUi extends Composite implements AppLayoutView {

	@UiField
	protected MaterialLink lnkGetVerified;
	@UiField
	protected DpornLink linkMyChannel;
	
	@UiField
	protected DpornLink linkHome;
	
	@UiField
	protected DpornLink linkSearch;
	
	@UiField
	protected DpornLink linkVerified;

	@UiField
	protected MaterialSideNavPush sidenav;

	@UiField
	protected MaterialPanel panel;
	
	@UiField
	protected MaterialButton account;
	@UiField
	protected MaterialLabel displayName;
	@UiField
	protected MaterialImage avatar;
	
	@UiField
	protected DpornLink linkPostErotica;
	@UiField
	protected DpornLink linkUploadPhotos;
	@UiField
	protected DpornLink linkUploadVideo;
	@UiField
	protected DpornLink linkSettings;

	private IsView<?> container;

	private HandlerRegistration handler;

	private AppPresenter presenter;

	private static AppLayoutUiUiBinder uiBinder = GWT.create(AppLayoutUiUiBinder.class);

	interface AppLayoutUiUiBinder extends UiBinder<Widget, AppLayoutUi> {
	}

	public AppLayoutUi() {
		initWidget(uiBinder.createAndBindUi(this));
		linkHome.addClickHandler(e -> {
			e.preventDefault();
			new PushStateHistorian().newItem("/", true);
		});
		linkMyChannel.setVisible(false);
		linkMyChannel.setEnabled(false);
		enableContentCreatorRoles(false);
	}

	@Override
	public void bindPresenter(AppPresenter presenter) {
		this.presenter=presenter;
	}

	@Override
	public void unbindPresenter() {
		this.presenter=null;
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		handler = account.addClickHandler((e)->{
			presenter.account();
		});
	}
	@Override
	protected void onUnload() {
		super.onUnload();
		account.removeHandler(handler);
	}

	@Override
	public void setUsername(String username) {
		if (username==null||username.trim().isEmpty()) {
			this.account.setText("Login");
			linkMyChannel.setVisible(false);
			linkMyChannel.setEnabled(false);
			return;
		}
		this.account.setText("Logout");
		linkMyChannel.setHref("/@"+username);
		linkMyChannel.setVisible(true);
		linkMyChannel.setEnabled(true);
	}

	@Override
	public void setDisplayname(String displayname) {
		this.displayName.setText(displayname);
	}

	@Override
	public void setAvatar(String avatarUrl) {
		this.avatar.setUrl(avatarUrl);
	}

	@Override
	public void enableContentCreatorRoles(boolean enabled) {
		linkPostErotica.setVisible(enabled);
		linkUploadPhotos.setVisible(enabled);
		linkUploadVideo.setVisible(enabled);
		linkSettings.setVisible(enabled);
		
		linkPostErotica.setEnabled(enabled);
		linkUploadPhotos.setEnabled(enabled);
		linkUploadVideo.setEnabled(enabled);
		linkSettings.setEnabled(enabled);	
	}

	@Override
	public void setChildPresenter(IsChildPresenter<? extends IsView<?>> childPresenter) {
		IsView<?> view = childPresenter.getContentView();
		if (view==null) {
			throw new IllegalStateException("View must implement a non-null getContentView()");
		}
		if (container != null) {
			panel.remove(container.asWidget());
		}
		int sidenavix = panel.getWidgetIndex(sidenav);
		panel.insert(view.asWidget(), sidenavix + 1);
		container = view;
		
	}

	@Override
	public void toast(String message) {
		MaterialToast.fireToast(message);
	}

	@Override
	public void enableUnimplementedFeatures(boolean enabled) {
		linkSearch.setEnabled(enabled);
		linkVerified.setEnabled(enabled);
		linkUploadPhotos.setEnabled(enabled);
		linkUploadVideo.setEnabled(enabled);
		linkSettings.setEnabled(enabled);
	}

	@Override
	public void setVerified(Boolean verified) {
		if (verified) {
			lnkGetVerified.setText("Verified Chat");
		} else {
			lnkGetVerified.setText("Get Verified");
		}
	}
}
