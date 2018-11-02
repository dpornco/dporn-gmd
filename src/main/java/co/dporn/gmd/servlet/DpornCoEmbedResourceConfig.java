package co.dporn.gmd.servlet;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("embed")
public class DpornCoEmbedResourceConfig extends ResourceConfig {
	public DpornCoEmbedResourceConfig() {
		register(DpornCoEmbed.class);
	}
}
