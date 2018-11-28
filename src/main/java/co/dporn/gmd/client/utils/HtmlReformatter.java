package co.dporn.gmd.client.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.Node;
import elemental2.dom.NodeList;
import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;

public class HtmlReformatter {

	/**
	 * Used to change steemitimages image sizing values to better match fixed 640px
	 * width display used by steemit/busy.
	 */
	@SuppressWarnings("unused")
	private final double imgScaleWidth;

	public HtmlReformatter() {
		this(1.0d);
	}

	public HtmlReformatter(double imgScaleWidth) {
		this.imgScaleWidth = imgScaleWidth;
	}

	private static final Set<String> TAG_WHITELIST = new HashSet<>(Arrays.asList( //
			"h1", "h2", "h3", //
			"h4", "h5", "h6", "a", "div", "span", //
			"p", "img", "ol", "ul", "li", "table", //
			"tr", "td", "thead", "center", "strong", //
			"b", "em", "i", "strike", "u", "cite", //
			"blockquote", "pre", "br", "hr"));
	
	private static final Set<String> TAG_ZAPLIST = new HashSet<>(Arrays.asList( //
			"script", "style"));

	private static final String ATTR_STYLE = "style";

	private static final String STEEMIT_PULL_LEFT = "pull-left";

	private static final String STEEMIT_PULL_RIGHT = "pull-right";

	/**
	 * Reformats HTML to be more steemit.com/busy.org display ready. Works from
	 * deepest elements first.
	 * 
	 * @param html
	 * @return
	 */
	public String reformat(String html) {
		HTMLDivElement htmlWrapperElement = (HTMLDivElement) DomGlobal.document.createElement("div");
		htmlWrapperElement.innerHTML = html;
		try {
			convertStylesToTags(htmlWrapperElement);
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
		removeSpanTags(htmlWrapperElement);
		moveDivsOutsidePs(htmlWrapperElement);
		moveImgsOutsidePs(htmlWrapperElement);
		return htmlWrapperElement.innerHTML;
	}
	
	/**
	 * Moves all IMGs to become after siblings of any Ps they are
	 * inside of. (Display fidelity for steemit/busy!)
	 * 
	 * @param element
	 */
	private void moveImgsOutsidePs(Element element) {
		while (true)  {
			JQueryElement divs = JQuery.$(element).find("p img");
			GWT.log("Have "+divs.length()+" imgs to relocate");
			if (divs.length()==0) {
				return;
			}
			for (int ix=0; ix<divs.length(); ix++) {
				JQueryElement img = JQuery.$(divs.get(ix));
				JQueryElement parent = img.parent();
				if (parent==null) {
					continue;
				}
				img.remove();
				parent.after(img);
			}
		}
	}

	/**
	 * Moves all DIVs to become before siblings of any Ps they are
	 * inside of. (HTML Structure Compliancy!)
	 * 
	 * @param element
	 */
	private void moveDivsOutsidePs(Element element) {
		while (true)  {
			JQueryElement divs = JQuery.$(element).find("p div");
			GWT.log("Have "+divs.length()+" divs to relocate");
			if (divs.length()==0) {
				return;
			}
			for (int ix=0; ix<divs.length(); ix++) {
				JQueryElement div = JQuery.$(divs.get(ix));
				JQueryElement parent = div.parent();
				if (parent==null) {
					continue;
				}
				div.remove();
				parent.before(div);
			}
		}
	}

	/**
	 * Removes all span tags via unwrapping their contents. Works from deepest
	 * elements first.
	 * 
	 * @param element
	 */
	private void removeSpanTags(Element element) {
		if (element.nodeType != Node.ELEMENT_NODE) {
			return;
		}
		if (element.hasChildNodes()) {
			NodeList<Node> children = element.childNodes;
			for (int ix = children.getLength() - 1; ix >= 0; ix--) {
				Node item = children.getAt(ix);
				if (item.nodeType == Node.ELEMENT_NODE) {
					removeSpanTags((Element) item);
				}
			}
		}
		if (element.tagName.toLowerCase().equals("span")) {
			JQuery.$(element).contents().unwrap();
		}
	}

	public void convertStylesToTags(Element element) {
		if (element.nodeType != Node.ELEMENT_NODE) {
			GWT.log("Skip non-element node");
			return;
		}
		if (element.hasChildNodes()) {
			NodeList<Node> children = element.childNodes;
			for (int ix = children.getLength() - 1; ix >= 0; ix--) {
				Node item = children.getAt(ix);
				if (item.nodeType == Node.ELEMENT_NODE) {
					convertStylesToTags((Element) item);
				}
			}
		}
		String tag = String.valueOf(element.tagName).toLowerCase();
		GWT.log("tag: " + tag);
		if (TAG_ZAPLIST.contains(tag)) {
			GWT.log("REMOVE: " + tag);
			JQuery.$(element).remove();
			return;
		}
		if (!TAG_WHITELIST.contains(tag)) {
			GWT.log("UNWRAPPED CHILDREN OF: " + tag);
			JQuery.$(element).contents().unwrap();
			return;
		}

		// walk list in depth first to simplify deletes and unwraps
		String[] attributes = element.getAttributeNames();
		attrs: for (String aname : attributes) {
			aname = aname.toLowerCase();
			if (aname.equals("src")) {
				continue;
			}
			if (aname.equals("srcset")) {
				continue;
			}
			if (aname.equals("target")) {
				continue;
			}
			if (aname.equals("href")) {
				continue;
			}
			if (aname.equals("class")) {
				continue;
			}
			if (aname.equals(ATTR_STYLE)) {
				String style = element.getAttribute(ATTR_STYLE).toLowerCase().replaceAll("\\s+", " ");
				element.removeAttribute(ATTR_STYLE);
				List<String> styles = new ArrayList<>(Arrays.asList(style.split("\\s*;\\s*")));
				Iterator<String> iStyles = styles.iterator();
				style: while (iStyles.hasNext()) {
					String next = iStyles.next();
					GWT.log("STYLE: '" + next + "'");
					if (next.equals("text-align: justify")) {
						JQuery.$(element).wrap("<div class='text-justify' style='text-align: justify;'>");
						continue style;
					}
					if (next.equals("text-align: right")) {
						JQuery.$(element).wrap("<div class='text-right' style='text-align: right;'>");
						continue style;
					}
					if (next.equals("text-align: left")) {
						JQuery.$(element).wrap("<div class='text-left' style='text-align: left;>");
						continue style;
					}
//					if (next.equals("text-align: center")) {
//						JQuery.$(element).wrap("<div class='text-center'>");
//						continue style;
//					}
					if (next.equals("text-align: center")) {
						JQuery.$(element).wrap("<center>");
						continue style;
					}
					if (next.equals("font-weight: bold")) {
						JQuery.$(element).wrap("<strong>");
						continue style;
					}
					if (next.equals("font-style: italic")) {
						JQuery.$(element).wrap("<em>");
						continue style;
					}
					if (next.equals("text-decoration: underline")) {
						JQuery.$(element).wrap("<u>");
						continue style;
					}
					if (next.equals("text-decoration: line-through")) {
						JQuery.$(element).wrap("<strike>");
						continue style;
					}
					if (tag.equals("img")) {
						JQueryElement img = JQuery.$(element);
						img.css("max-width", "100%");
						if (next.equals("float: left")) {
							JQuery.$(element).wrap("<div>");
							JQueryElement imgDiv = JQuery.$(element.parentNode);
							imgDiv.addClass(STEEMIT_PULL_LEFT);
							// "float: left; padding: 4px; max-width: 50%;";
							imgDiv.css("float", "left");
							imgDiv.css("padding", "4px");
							imgDiv.css("max-width", "50%");
							img.css("float", "");
							img.css("padding", "");
							continue style;
						}
						if (next.equals("float: right")) {
							JQuery.$(element).wrap("<div>");
							JQueryElement imgDiv = JQuery.$(element.parentNode);
							imgDiv.addClass(STEEMIT_PULL_RIGHT);
							// "float: right; padding: 4px; max-width: 50%;";
							imgDiv.css("float", "right");
							imgDiv.css("padding", "4px");
							imgDiv.css("max-width", "50%");
							img.css("float", "");
							img.css("padding", "");
							continue style;
						}
					}
				}
				if (tag.equals("img") || tag.equals("div")) {
					continue;
				}
				element.removeAttribute(ATTR_STYLE);
			}
			continue attrs;
		}
	}
}
