package co.dporn.gmd.servlet;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
@ApplicationPath("/dpornco/1.0")
public class DpornCoServletApplication extends ResourceConfig {
	public DpornCoServletApplication() {
		register(DpornCoApiImpl.class);
	}
}
