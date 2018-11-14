package co.dporn.gmd.client.img;

import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLImageElement;
import jsinterop.base.Js;

public class ImgUtils {
	private static final int DEFAULT_MAX_IMAGE_HEIGHT = 1080;
	private static final int DEFAULT_MAX_IMAGE_WIDTH = 1920;
	private static final double QUALITY = 0.98;

	public static CompletableFuture<HTMLImageElement> resizeInplace(HTMLImageElement image, double maxWidth,
			double maxHeight) {
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
					resizeInplace(image, maxWidth, maxHeight).thenAccept(value -> {
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
		 * scale image down
		 */
		if (w > maxWidth) {
			scaleWidth = maxWidth / w;
		}
		if (h > maxHeight) {
			scaleHeight = maxHeight / h;
		}
		if (scaleWidth < scaleHeight) {
			scale = scaleWidth;
		} else {
			scale = scaleHeight;
		}
		if (scale < 1) {
			int newWidth = (int) (w * scale);
			int newHeight = (int) (h * scale);
			GWT.log("ImgUtils.resizeInplace: " + newWidth + " x " + newHeight);
			HTMLCanvasElement canvas = Js.cast(DomGlobal.document.createElement("canvas"));
			canvas.width = ((int) (w * scale));
			canvas.height = ((int) (h * scale));
			CanvasRenderingContext2D ctx = Js.cast(canvas.getContext("2d"));
			ctx.drawImage(image, 0, 0, newWidth, newHeight);
			String mime = image.src.contains(";base64,iVBOR") ? "image/png" : "image/jpeg";
			mime = image.src.contains(";base64,R0lGODl") ? "image/gif" : mime;
			image.onload = (p0) -> future.complete(image);
			image.src = canvas.toDataURL(mime, QUALITY);
		} else {
			future.complete(image);
		}
		return future;
	}

	public static CompletableFuture<HTMLImageElement> resizeInplace(Element image, int maxWidth, int maxHeight) {
		try {
			GWT.log("ImgUtils.resizeInplace:cast => HTMLImageElement cast;");
			HTMLImageElement cast = Js.cast(image);
			return resizeInplace(cast, maxWidth, maxHeight);
		} catch (Exception e) {
			CompletableFuture<HTMLImageElement> future = new CompletableFuture<>();
			future.completeExceptionally(e);
			return future;
		}
	}

	public static CompletableFuture<HTMLImageElement> resizeInplace(Element image) {
		return resizeInplace(image, DEFAULT_MAX_IMAGE_WIDTH, DEFAULT_MAX_IMAGE_HEIGHT);
	}

	public static CompletableFuture<HTMLImageElement> resizeInplace(HTMLImageElement image) {
		return resizeInplace(image, DEFAULT_MAX_IMAGE_WIDTH, DEFAULT_MAX_IMAGE_HEIGHT);
	}

	/*
	 * function b64toBlob(b64, onsuccess, onerror) { var img = new Image();
	 * 
	 * img.onerror = onerror;
	 * 
	 * img.onload = function onload() { var canvas =
	 * document.createElement('canvas'); canvas.width = img.width; canvas.height =
	 * img.height;
	 * 
	 * var ctx = canvas.getContext('2d'); ctx.drawImage(img, 0, 0, canvas.width,
	 * canvas.height);
	 * 
	 * canvas.toBlob(onsuccess); };
	 * 
	 * img.src = b64; }
	 * 
	 * var base64Data = 'data:image/jpg;base64,/9j/4AAQSkZJRgABAQA...';
	 * b64toBlob(base64Data, function(blob) { var url =
	 * window.URL.createObjectURL(blob); // do something with url }, function(error)
	 * { // handle error });
	 */
}
