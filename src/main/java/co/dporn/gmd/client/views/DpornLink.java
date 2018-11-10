package co.dporn.gmd.client.views;

import com.wallissoftware.pushstate.client.PushStateHistorian;

import gwt.material.design.client.ui.MaterialLink;

public class DpornLink extends MaterialLink {
	public DpornLink() {
		super();
		addClickHandler((e) -> {
			e.preventDefault();
			new PushStateHistorian().newItem(getHref(), true);
		});
	}
}
