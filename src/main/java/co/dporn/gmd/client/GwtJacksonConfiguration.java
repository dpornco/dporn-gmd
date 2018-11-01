package co.dporn.gmd.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.nmorel.gwtjackson.client.AbstractConfiguration;

public class GwtJacksonConfiguration extends AbstractConfiguration {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MixInIgnoreUnknownProperties {
	}
	
	public GwtJacksonConfiguration() {
		super();
	}

	@Override
	protected void configure() {
		this.addMixInAnnotations(Object.class, MixInIgnoreUnknownProperties.class);
	}
}
