package co.dporn.gmd.client.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;

import co.dporn.gmd.client.presenters.UploadErotica;
import co.dporn.gmd.client.utils.DpornChipProvider;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.TagSet;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialChip;
import gwt.material.design.client.ui.MaterialContainer;
import gwt.material.design.client.ui.MaterialDialog;
import gwt.material.design.client.ui.MaterialDialogContent;
import gwt.material.design.client.ui.MaterialDialogFooter;
import gwt.material.design.client.ui.MaterialRow;
import gwt.material.design.client.ui.MaterialTextBox;
import gwt.material.design.client.ui.MaterialToast;

public class UploadEroticaUi extends Composite implements UploadErotica.UploadEroticaView {

	interface ThisUiBinder extends UiBinder<Widget, UploadEroticaUi> {
	}

	private static ThisUiBinder uiBinder = GWT.create(ThisUiBinder.class);

	@UiField
	protected TagAutoComplete ac;
	@UiField
	protected MaterialButton btnTagSets;
	@UiField
	protected MaterialButton btnClear;
	@UiField
	protected MaterialButton btnPreview;
	@UiField
	protected MaterialButton btnSubmit;
	@UiField
	protected DpornRichEditor editor;
	@UiField
	protected MaterialTextBox title;
	@UiField
	protected MaterialContainer mainContent;

	private UploadErotica presenter;

	public UploadEroticaUi(SuggestOracle suggestOracle, Set<String> mandatorySuggestions) {
		initWidget(uiBinder.createAndBindUi(this));
		btnTagSets.addClickHandler(e -> presenter.viewRecentTagSets("erotica"));
		btnClear.addClickHandler((e) -> reset());
		btnPreview.addClickHandler(e -> {
			presenter.showPostBodyPreview((double) editor.getEditor().width(), editor.getValue());
		});
		btnSubmit.addClickHandler(e -> {
			title.clearErrorText();
			editor.clearErrorText();
			ac.clearErrorText();
			presenter.createNewBlogEntry( //
					BlogEntryType.BLOG, //
					editor.getEditor().width(), //
					title.getValue(), //
					ac.getValue(), //
					editor.getValue() //
			);
		});
		ac.setSuggestions(suggestOracle);
		ac.setMandatoryTags(mandatorySuggestions);
	}

	@Override
	public void bindPresenter(UploadErotica presenter) {
		this.presenter = presenter;
		editor.bindIpfsApi(presenter);
	}

	@Override
	public String getBody() {
		return editor.getValue();
	};

	@Override
	public OnprogressFn getOnprogressFn(String filename) {
		return new OnprogressFn() {
			long start = 0;

			@Override
			public void onInvoke(ProgressEvent p0) {
				if (!p0.lengthComputable) {
					return;
				}
				if (System.currentTimeMillis() - start > 10000l) {
					start = System.currentTimeMillis();
					int percent = (int) Math.ceil(p0.loaded * 100d / p0.total);
					MaterialToast.fireToast("Posting to IPFS: " + filename + " " + percent + "%");
				}
			}
		};
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		title.setFocus(true);
	}

	@Override
	public void onprogressFn(ProgressEvent p0) {
	}

	@Override
	protected void onUnload() {
		super.onUnload();
	}

	@Override
	public void showTagSets(List<TagSet> sets) {
		GWT.log("Tag sets: " + sets.size());
		if (sets.isEmpty()) {
			MaterialToast.fireToast("No recent tag sets available.");
			return;
		}
		MaterialDialog dialog = new MaterialDialog();
		// dialog.setType(DialogType.FIXED_FOOTER);
		MaterialDialogContent content = new MaterialDialogContent();
		dialog.add(content);
		MaterialDialogFooter footer = new MaterialDialogFooter();
		MaterialButton btnClose = new MaterialButton("CLOSE");
		btnClose.addClickHandler((e) -> dialog.close());
		footer.add(btnClose);
		dialog.add(footer);
		DpornChipProvider chipProvider = new DpornChipProvider(new HashSet<>());
		for (TagSet set : sets) {
			MaterialRow row = new MaterialRow();
			MaterialButton btnAdd = new MaterialButton("ADD TAG SET");
			btnAdd.setMarginRight(18);
			row.add(btnAdd);
			btnAdd.addClickHandler((e) -> {
				Set<String> activeTags = new LinkedHashSet<>(ac.getItemValues());
				activeTags.addAll(set.getTags());
				ac.setItemValues(new ArrayList<>(activeTags), true);
				dialog.close();
			});
			chipProvider.setMandatoryTags(set.getTags());
			for (String tag : set.getTags()) {
				MaterialChip chip = chipProvider.getChip(tag);
				chip.setMarginRight(8);
				row.add(chip);
			}
			content.add(row);
		}
		dialog.addCloseHandler((e) -> dialog.removeFromParent());
		RootPanel.get().add(dialog);
		dialog.open();
	}

	@Override
	public void unbindPresenter() {
		this.presenter = null;
		editor.unbindIpfsApi();
	}

	@Override
	public void showPreview(String html) {
		MaterialDialog dialog = new MaterialDialog();
		dialog.addCloseHandler(e -> dialog.removeFromParent());
		MaterialDialogContent content = new MaterialDialogContent();
		dialog.add(content);
		MaterialDialogFooter footer = new MaterialDialogFooter();
		MaterialButton btnClose = new MaterialButton("CLOSE");
		btnClose.addClickHandler((e) -> dialog.close());
		footer.add(btnClose);
		dialog.add(footer);

		MaterialRow row = new MaterialRow();
		row.setWidth("640px");
		row.getElement().setInnerHTML(html);
		content.add(row);

		RootPanel.get().add(dialog);
		dialog.open();
	}

	@Override
	public double getEditorWidth() {
		return editor.getEditor().width();
	}

	@Override
	public void setErrorBadTitle() {
		title.setErrorText("Title must not be blank");
		title.setFocus(true);
	}

	@Override
	public void setErrorBadContent() {
		MaterialToast.fireToast("Not enough content for blog body");
		MaterialToast.fireToast("Add content");
		GWT.log("Not enough content");
		editor.setErrorText("Not enough content");
		editor.setFocus(true);
	}

	@Override
	public void setErrorBadTags() {
		ac.setErrorText("Must specify at least one tag");
		ac.setFocus(true);
	}

	@Override
	public void reset() {
		ac.reset();
		title.reset();
		editor.reset();
		title.setFocus(true);
	}
}
