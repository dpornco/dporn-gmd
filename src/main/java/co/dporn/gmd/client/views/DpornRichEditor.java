package co.dporn.gmd.client.views;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;

import co.dporn.gmd.client.utils.ImgUtils;
import co.dporn.gmd.client.utils.IpfsApi;
import elemental2.dom.HTMLImageElement;
import gwt.material.design.addins.client.richeditor.MaterialRichEditor;
import gwt.material.design.addins.client.richeditor.base.constants.ToolbarButton;
import gwt.material.design.client.ui.MaterialLoader;
import gwt.material.design.client.ui.MaterialToast;
import jsinterop.base.Js;

public class DpornRichEditor extends MaterialRichEditor {

	private static long _counter = System.currentTimeMillis();

	private static synchronized long nextCounter() {
		return (_counter = Math.max(_counter + 1, System.currentTimeMillis()));
	}

	public DpornRichEditor() {
		imgUtils = new ImgUtils();
		imgUtils.setEventMessageHandler((msg) -> MaterialToast.fireToast(msg));
		addValueChangeHandler(this::valueChangeHandler);
		deferredInit();
	}

	private void deferredInit() {
		Scheduler.get().scheduleDeferred(() -> {
			ToolbarButton[] noOptions = new ToolbarButton[0];
			setStyleOptions(ToolbarButton.STYLE, ToolbarButton.BOLD, ToolbarButton.ITALIC, ToolbarButton.STRIKETHROUGH,
					ToolbarButton.CLEAR, ToolbarButton.SUPERSCRIPT, ToolbarButton.SUBSCRIPT);
			setFontOptions(noOptions);
			setColorOptions(noOptions);
			setCkMediaOptions(ToolbarButton.CK_IMAGE_UPLOAD);
			setParaOptions(ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.LEFT, ToolbarButton.CENTER,
					ToolbarButton.RIGHT, ToolbarButton.JUSTIFY);
			setUndoOptions(ToolbarButton.REDO, ToolbarButton.UNDO);
			setMiscOptions(ToolbarButton.LINK, ToolbarButton.PICTURE, ToolbarButton.TABLE, ToolbarButton.HR,
					ToolbarButton.FULLSCREEN);
			setHeightOptions(noOptions);
			setAllowBlank(false);
			setAutoValidate(false);
			setDisableDragAndDrop(false);
			setValue("<p><br></p>", true, true);
			getEditor().find("[data-event='imageClass']").remove();
			getEditor().find("[data-event='imageShape']").remove();
		});
	}

	private void postImageToIpfs(HTMLImageElement image) {
		String guessExtension = imgUtils.guessExtension(image.src);
		String filename = image.getAttribute("data-filename");
		if (filename == null) {
			filename = "";
		}
		filename = filename.trim();
		filename = filename.replace(" ", "-");
		filename = filename.toLowerCase();
		filename = filename.replaceAll("[^a-z0-9\\.\\-_]", "");
		filename = filename.replaceAll("-+", "-");
		if (filename.isEmpty() || filename.length() <= guessExtension.length() + 1) {
			filename = nextCounter() + "." + guessExtension;
		}
		if (!filename.endsWith("." + guessExtension)) {
			filename += "." + guessExtension;
		}
		final String ipfsFilename = filename;
		new ImgUtils().toBlob(image).thenAccept((blob) -> {
			ipfsApi.postBlobToIpfs(ipfsFilename, blob).thenAccept((path) -> {
				MaterialToast.fireToast("Image posted: " + StringUtils.substringAfterLast(path, "/"));
				MaterialLoader.loading(false);
				StringBuilder srcset = new StringBuilder();
				for (String ipfsgw : new String[] { "https://ipfs.io",
						"https://steemitimages.com/0x0/https://ipfs.io" }) {
					srcset.append(ipfsgw);
					srcset.append(path);
					srcset.append(", ");
				}
				image.onerror = e -> {
					image.onerror = null;
					image.src = "https://ipfs.io" + path;
					return e;
				};
				image.srcset = srcset.toString();
				image.src = "https://steemitimages.com/0x0/https://ipfs.io" + path;
			});
		});
	}

	private final ImgUtils imgUtils;
	private IpfsApi ipfsApi;

	private void automaticImageResizeAndIpfsPut(Element e) {
		if (ipfsApi == null) {
			MaterialToast.fireToast("WARNING! IPFS NOT CONNECTED");
			return;
		}
		if (e.getAttribute("ImgUtilsResizedInplace").equals("true")) {
			return;
		}
		MaterialLoader.loading(true);
		imgUtils.resizeInplace(e)//
				.thenAccept((img) -> img.setAttribute("ImgUtilsResizedInplace", "true")) //
				.thenRun(() -> postImageToIpfs(Js.cast(e)))//
				.exceptionally(ex -> {
					MaterialToast.fireToast("ERROR: " + ex.getMessage());
					MaterialLoader.loading(false);
					return null;
				});
	}

	private void valueChangeHandler(ValueChangeEvent<String> event) {
		// keep all content visible
		getEditor().find(".note-editable").css("height", "100%").css("min-height", "550px");

		// image fixups
		getEditor().find(".note-editable").find("img[src^='data:']").each((o, e) -> automaticImageResizeAndIpfsPut(e));
		getEditor().find(".note-editable").find("img").each((o, e) -> ImgUtils.automaticImageRestyler(e));

		// TODO: add save/restore to/from local storage magic in case of hitting
		// "backspace"

		// TODO: try and magic the images sizes scaled to editor container vs the 640px
		// fixed width blog view at steemit.com/busy.org

	}

	public void unbindIpfsApi() {
		this.ipfsApi = null;
	}

	public void bindIpfsApi(IpfsApi ipfsApi) {
		this.ipfsApi = ipfsApi;
	}

}
