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
	
	private final double imgScaleWidth;

	public HtmlReformatter() {
		this(1.0d);
	}
	
	public HtmlReformatter(double imgScaleWidth) {
		this.imgScaleWidth=imgScaleWidth;
	}
	
	private static final Set<String> TAG_WHITELIST = new HashSet<>(Arrays.asList( //
			"h1", "h2", "h3", //
			"h4", "h5", "h6", "a", "div", "span", //
			"p", "img", "ol", "ul", "li", "table", //
			"tr", "td", "thead", "center", "strong", //
			"b", "em", "i", "strike", "u", "cite", //
			"blockquote", "pre", "br", "hr"));
	
	private static final String ATTR_STYLE = "style";

	private static final String STEEMIT_PULL_LEFT = "pull-left";

	private static final String STEEMIT_PULL_RIGHT = "pull-right";

	/**
	 * Reformats HTML to be more steemit.com/busy.org display ready. Works from deepest elements first.
	 * @param html
	 * @return
	 */
	public String reformat(String html) {
		HTMLDivElement htmlWrapperElement = (HTMLDivElement) DomGlobal.document.createElement("div");
		htmlWrapperElement.innerHTML=html;
		try {
			convertStylesToTags(htmlWrapperElement);
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
		removeSpanTags(htmlWrapperElement);
		return htmlWrapperElement.innerHTML;
	}

	/**
	 * TODO: Not working right.
	 * Moves all DIVs to become siblings of any Ps they are inside of. (HTML Structure Compliancy!)
	 * @param element
	 */
	@SuppressWarnings("unused")
	private void moveDivsOutsidePs(Element element) {
		Element parentElement = (Element)element.parentNode;
		if (parentElement == null) {
			return;
		}
		if (element.nodeType != Node.ELEMENT_NODE) {
			return;
		}
		if (element.hasChildNodes()) {
			NodeList<Node> children = element.childNodes;
			for (int ix = children.getLength() - 1; ix >= 0; ix--) {
				Node item = children.getAt(ix);
				if (item.nodeType == Node.ELEMENT_NODE) {
					moveDivsOutsidePs((Element) item);
				}
			}
		}
		if (!element.tagName.toLowerCase().equals("div")) {
			return;
		}
		
		if (!parentElement.tagName.equals("p")) {
			return;
		}
		JQuery.$(element).unwrap();
	}

	/**
	 * Removes all span tags via unwrapping their contents. Works from deepest elements first.
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
		GWT.log("tag: "+tag);
		if (!TAG_WHITELIST.contains(tag)) {
			GWT.log("UNWRAPPED CHILDREN OF: " + tag);
			JQuery.$(element).contents().unwrap();
			return;
		}
		
		// walk list in reverse to simplify deletes
		String[] attributes = element.getAttributeNames();
		attrs: for (String aname: attributes) {
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
					GWT.log("STYLE: '"+next+"'");
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
							//"float: left; padding: 4px; max-width: 50%;";
							imgDiv.css("float", "left");
							imgDiv.css("padding", "4px");
							img.css("float", "");
							img.css("padding", "");
							continue style;
						}
						if (next.equals("float: right")) {
							JQuery.$(element).wrap("<div>");
							JQueryElement imgDiv = JQuery.$(element.parentNode);
							imgDiv.addClass(STEEMIT_PULL_RIGHT);
							//"float: right; padding: 4px; max-width: 50%;";
							imgDiv.css("float", "right");
							imgDiv.css("padding", "4px");
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
