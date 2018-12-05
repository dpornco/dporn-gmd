package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import elemental2.dom.Event;
import elemental2.dom.HTMLImageElement;
import gwt.material.design.client.ui.MaterialImage;
import jsinterop.base.Js;

public class MaterialSteemitImage extends MaterialImage {
	public MaterialSteemitImage() {
		super();
	}

	/**
	 * Sets the image SRC and also calculates and adds RESPONSIVE SIZED images via
	 * steemitimages in srcset.
	 */
	@Override
	public void setUrl(String url) {
		if (url==null) {
			url="";
		}
		HTMLImageElement img = Js.cast(getElement());
		img.src = url;
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			return;
		}
		img.srcset = "";
		List<String> srcset = new ArrayList<>();
		srcset.add("https://steemitimages.com/80x45/" + url + " 80w");
		srcset.add("https://steemitimages.com/160x90/" + url + " 160w");
		srcset.add("https://steemitimages.com/320x180/" + url + " 320w");
		srcset.add("https://steemitimages.com/640x360/" + url + " 640w");
		srcset.add("https://steemitimages.com/1280x720/" + url + " 1280w");
		srcset.add("https://steemitimages.com/1920x1080/" + url + " 1920w");
		srcset.add(url + " 1920w");
		Iterator<String> iter = srcset.iterator();
		StringBuilder sb = new StringBuilder();
		while (iter.hasNext()) {
			sb.append(iter.next());
			if (iter.hasNext()) {
				sb.append(",");
			}
		}
		img.onerror = new HTMLImageElement.OnerrorFn() {
			@Override
			public Object onInvoke(Event p0) {
				img.onerror = null;
				img.srcset = "";
				return p0;
			}
		};
		img.sizes = "(max-width: 1920px) 640px, (max-width: 1280px) 320px,"
				+ " (max-width: 640px) 320px, (max-width: 320px) 160px";
		img.srcset = sb.toString();
	}
}
