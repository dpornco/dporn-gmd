package co.dporn.gmd.servlet;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import co.dporn.gmd.shared.DpornCoApi;
import co.dporn.gmd.shared.PingResponse;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("dpornco/1.0")
public class DpornCoApiImpl implements DpornCoApi {
	@Override
	public PingResponse ping() {
		return new PingResponse(true);
	}
}
