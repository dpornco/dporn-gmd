package co.dporn.gmd.client.utils;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.dom.client.Element;

import elemental2.dom.Blob;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLImageElement;
import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;
import jsinterop.base.Js;

public class ImgUtils {

	private static final int DEFAULT_MAX_IMAGE_HEIGHT = 1080;
	private static final int DEFAULT_MAX_IMAGE_WIDTH = 1920;
	private static final double BLOB_QUALITY = 0.98;
	private static final double BASE64_QUALITY = 1.0;

	public static interface EventMessageHandler {
		void onEventMessage(String message);
	}

	private EventMessageHandler handler;

	public void setEventMessageHandler(EventMessageHandler handler) {
		this.handler = handler;
	}

	private void message(String message) {
		DomGlobal.console.log(this.getClass().getSimpleName(), message);
		if (handler == null) {
			return;
		}
		handler.onEventMessage(message);
	}

	public CompletableFuture<HTMLImageElement> resizeInplace(HTMLImageElement image, double maxWidth, double maxHeight,
			boolean maxpect) {
		CompletableFuture<HTMLImageElement> future = new CompletableFuture<>();
		if (image == null) {
			future.completeExceptionally(new NullPointerException("Image element must not be null"));
			return future;
		}
		if (!image.complete) {
			EventListener onload = new EventListener() {
				EventListener _self = this;

				@Override
				public void handleEvent(Event evt) {
					image.removeEventListener("onload", _self);
					resizeInplace(image, maxWidth, maxHeight, maxpect).thenAccept(value -> {
						future.complete(value);
					}).exceptionally(ex -> {
						future.completeExceptionally(ex);
						return null;
					});
				}
			};
			image.addEventListener("onload", onload);
			return future;
		}
		double scale = 1;
		double scaleWidth = 1;
		double scaleHeight = 1;
		final double w = image.naturalWidth;
		final double h = image.naturalHeight;
		/*
		 * scale image calculations
		 */
		scaleWidth = maxWidth / w;
		scaleHeight = maxHeight / h;
		if (scaleWidth < scaleHeight) {
			scale = scaleWidth;
		} else {
			scale = scaleHeight;
		}

		// don't canvas resize webp or gif - animations are lost
		boolean skip = false;
		switch (guessExtension(image.src)) {
		case "webp":
		case "gif":
			scale = 1.0d;
			skip = true;
			break;
		default:
		}
		
		if (skip) {
			future.complete(image);
			return future;
		}

		if (scale < 1 || maxpect) {
			int newWidth = (int) (w * scale);
			int newHeight = (int) (h * scale);
			message("RESIZING: " + newWidth + " x " + newHeight);
			HTMLCanvasElement canvas = Js.cast(DomGlobal.document.createElement("canvas"));
			canvas.width = ((int) (w * scale));
			canvas.height = ((int) (h * scale));
			CanvasRenderingContext2D ctx = Js.cast(canvas.getContext("2d"));
			ctx.drawImage(image, 0, 0, newWidth, newHeight);
			String mime = guessMime(image.src);
			image.onload = (p0) -> future.complete(image);
			image.src = canvas.toDataURL(mime, BASE64_QUALITY);
		} else {
			future.complete(image);
		}
		return future;
	}

	public CompletableFuture<HTMLImageElement> resizeInplace(Element image, int maxWidth, int maxHeight) {
		try {
			HTMLImageElement cast = Js.cast(image);
			return resizeInplace(cast, maxWidth, maxHeight, false);
		} catch (Exception e) {
			CompletableFuture<HTMLImageElement> future = new CompletableFuture<>();
			future.completeExceptionally(e);
			return future;
		}
	}

	public CompletableFuture<HTMLImageElement> resizeInplace(Element image) {
		return resizeInplace(image, DEFAULT_MAX_IMAGE_WIDTH, DEFAULT_MAX_IMAGE_HEIGHT);
	}

	public CompletableFuture<HTMLImageElement> resizeInplace(HTMLImageElement image) {
		return resizeInplace(image, DEFAULT_MAX_IMAGE_WIDTH, DEFAULT_MAX_IMAGE_HEIGHT, false);
	}

	public String guessMime(String dataUrl) {
		if (dataUrl.startsWith("data:")) {
			dataUrl = dataUrl.substring(5);
		}
		if (dataUrl.contains(";base64")) {
			String xmime = StringUtils.substringBefore(dataUrl, ";base64");
			// user browser supplied mime type when possible
			if (xmime.contains("/")) {
				return xmime;
			}
		}
		if (dataUrl.contains("base64,")) {
			dataUrl = StringUtils.substringAfter(dataUrl, "base64,");
		}
		dataUrl = StringUtils.left(dataUrl, 16);
		String mime = dataUrl.startsWith("iVBOR") ? "image/png" : "image/jpeg";
		mime = dataUrl.startsWith("R0lGO") ? "image/gif" : mime;
		mime = dataUrl.startsWith("UklGR") ? "image/webp" : mime;
		return mime;
	}

	public String guessExtension(String dataUrl) {
		switch (guessMime(dataUrl)) {
		case "image/png":
			return "png";
		case "image/jpeg":
			return "jpg";
		case "image/gif":
			return "gif";
		default:
			return "webp";
		}
	}

	public CompletableFuture<Blob> toBlob(HTMLImageElement image) {
		CompletableFuture<Blob> future = new CompletableFuture<>();
		try {
			HTMLCanvasElement canvas = Js.cast(DomGlobal.document.createElement("canvas"));
			canvas.width = image.naturalWidth;
			canvas.height = image.naturalHeight;
			CanvasRenderingContext2D ctx = Js.cast(canvas.getContext("2d"));
			ctx.drawImage(image, 0, 0, canvas.width, canvas.height);
			String mime = guessMime(image.src);
			canvas.toBlob((blob) -> future.complete(blob), mime, BLOB_QUALITY);
		} catch (Exception ex) {
			future.completeExceptionally(ex);
		}
		return future;
	}

	public static void automaticImageRestyler(Element e) {
		JQueryElement j = JQuery.$(e);
		String styles = e.getAttribute("style");
		if (styles.contains("float: left") || styles.contains("float: right")) {
			j.css("max-width", "50%");
			j.css("height", "");
		} else {
			j.css("max-width", "100%");
			j.css("height", "");
		}
		String origSrc = e.getAttribute("src");
		String src = origSrc;
		if (src.startsWith("https://steemitimages.com/")) {
			if (src.matches("^https://steemitimages.com/\\d+x\\d+/.*")) {
				src = "https://steemitimages.com/" + j.width() + "x0/"
						+ src.replaceFirst("^https://steemitimages.com/\\d+x\\d+/", "");
			}
		} else {
			if (src.startsWith("http://") || src.startsWith("https://")) {
				src = "https://steemitimages.com/" + j.width() + "x0/" + src;
			}
		}
		String srcset = e.getAttribute("srcset");
		if (srcset != null && srcset.contains("://steemitimages.com/")) {
			String newsrcset = srcset.replaceAll("https://steemitimages.com/\\d+x\\d+/",
					"https://steemitimages.com/" + j.width() + "x0/");
			if (!newsrcset.equals(srcset)) {
				e.setAttribute("srcset", newsrcset);
			}
		}
		if (!src.equals(origSrc)) {
			e.setAttribute("src", src);
		}
	}
}
