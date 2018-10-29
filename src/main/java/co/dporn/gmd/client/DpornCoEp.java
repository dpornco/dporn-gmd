package co.dporn.gmd.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLoader;

public class DpornCoEp implements EntryPoint {

	@Override
	public void onModuleLoad() {
		MaterialLoader.loading(true);
		RootPanel.get().add(new MaterialLabel("HELLO WORLD!"));
		new Timer() {
			@Override
			public void run() {
				MaterialLoader.loading(false);
			}
		}.schedule(5000);
	}

}
