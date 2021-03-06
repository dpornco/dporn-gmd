package co.dporn.gmd.client.views;

import com.wallissoftware.pushstate.client.PushStateHistorian;

import gwt.material.design.client.ui.MaterialLink;

public class DpornLink extends MaterialLink {
	public DpornLink() {
		super();
		addClickHandler((e) -> {
			String href = getHref();
			if (href == null) {
				e.preventDefault();
				return;
			}
			if (href.toLowerCase().matches("^(https?|ftps?|mailto)://.*")) {
				return;
			}
			if (href.toLowerCase().matches("^//.*")) {
				return;
			}
			e.preventDefault();
			new PushStateHistorian().newItem(href, true);
		});
	}
}
