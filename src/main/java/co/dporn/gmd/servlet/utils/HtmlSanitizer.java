package co.dporn.gmd.servlet.utils;

import org.owasp.html.AttributePolicy;
import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import com.google.common.collect.ImmutableSet;

public class HtmlSanitizer {

	private static final AttributePolicy INTEGER = new AttributePolicy() {
		public String apply(String elementName, String attributeName, String value) {
			int n = value.length();
			if (n == 0) {
				return null;
			}
			for (int i = 0; i < n; ++i) {
				char ch = value.charAt(i);
				if (ch == '.') {
					if (i == 0) {
						return null;
					}
					return value.substring(0, i); // truncate to integer.
				} else if (!('0' <= ch && ch <= '9')) {
					return null;
				}
			}
			return value;
		}
	};

	private static PolicyFactory _policy;
	public static PolicyFactory get() {
		if (_policy!=null) {
			return _policy;
		}
		return _policy=Sanitizers.BLOCKS//
				.and(FORMATTING)//
				.and(IMAGES)//
				.and(Sanitizers.LINKS)//
				.and(STYLES)//
				.and(Sanitizers.TABLES);
	}
	
	/**
	   * Allows common formatting elements including {@code <b>}, {@code <i>}, etc.
	   */
	  public static final PolicyFactory FORMATTING = new HtmlPolicyBuilder()
	      .allowCommonInlineFormattingElements().allowElements("center").toFactory();

	private static final ImmutableSet<String> WHITELIST = ImmutableSet.of("float", "clear", "-moz-border-radius",
			"-moz-border-radius-bottomleft", "-moz-border-radius-bottomright", "-moz-border-radius-topleft",
			"-moz-border-radius-topright", "-moz-box-shadow", "-moz-outline", "-moz-outline-color",
			"-moz-outline-style", "-moz-outline-width", "-o-text-overflow", "-webkit-border-bottom-left-radius",
			"-webkit-border-bottom-right-radius", "-webkit-border-radius", "-webkit-border-radius-bottom-left",
			"-webkit-border-radius-bottom-right", "-webkit-border-radius-top-left", "-webkit-border-radius-top-right",
			"-webkit-border-top-left-radius", "-webkit-border-top-right-radius", "-webkit-box-shadow", "azimuth",
			"background", "background-attachment", "background-color", "background-image", "background-position",
			"background-repeat", "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius",
			"border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-collapse",
			"border-color", "border-left", "border-left-color", "border-left-style", "border-left-width",
			"border-radius", "border-right", "border-right-color", "border-right-style", "border-right-width",
			"border-spacing", "border-style", "border-top", "border-top-color", "border-top-left-radius",
			"border-top-right-radius", "border-top-style", "border-top-width", "border-width", "box-shadow",
			"caption-side", "color", "cue", "cue-after", "cue-before", "direction", "elevation", "empty-cells", "font",
			"font-family", "font-size", "font-stretch", "font-style", "font-variant", "font-weight", "height",
			"image()", "letter-spacing", "line-height", "linear-gradient()", "list-style", "list-style-image",
			"list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left", "margin-right",
			"margin-top", "max-height", "max-width", "min-height", "min-width", "outline", "outline-color",
			"outline-style", "outline-width", "padding", "padding-bottom", "padding-left", "padding-right",
			"padding-top", "pause", "pause-after", "pause-before", "pitch", "pitch-range", "quotes",
			"radial-gradient()", "rect()", "repeating-linear-gradient()", "repeating-radial-gradient()", "rgb()",
			"rgba()", "richness", "speak", "speak-header", "speak-numeral", "speak-punctuation", "speech-rate",
			"stress", "table-layout", "text-align", "text-decoration", "text-indent", "text-overflow", "text-shadow",
			"text-transform", "text-wrap", "unicode-bidi", "vertical-align", "voice-family", "volume", "white-space",
			"width", "word-spacing", "word-wrap");

	private static CssSchema dpornAllowedStyles() {
		return CssSchema.withProperties(WHITELIST);
	}

	public static final PolicyFactory STYLES = new HtmlPolicyBuilder().allowStyling(dpornAllowedStyles()).toFactory();

	/**
	 * Allows {@code <img>} elements from HTTP, HTTPS, and relative sources.
	 */
	public static final PolicyFactory IMAGES = new HtmlPolicyBuilder().allowUrlProtocols("http", "https")
			.allowElements("img").allowAttributes("alt", "src").onElements("img") //
			.allowAttributes("border", "height", "width").matching(INTEGER).onElements("img") //
			// .allowAttributes("srcset").onElements("img")// sanitizer mangles srcset urls
			.toFactory();

}
