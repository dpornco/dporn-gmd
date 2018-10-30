package co.dporn.gmd.servlet;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class DpornCoServletApplication extends ResourceConfig {
	public DpornCoServletApplication() {
		register(DpornCoApiImpl.class);
	}
}
