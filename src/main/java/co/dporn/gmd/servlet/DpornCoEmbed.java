package co.dporn.gmd.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.TEXT_HTML)
@Path("/")
public class DpornCoEmbed {
	private static String _template;
	private static String template() {
		if (_template==null) {
			try {
				_template=IOUtils.toString(DpornCoEmbed.class.getResourceAsStream("/embed/embed-player.html"));
			} catch (IOException e) {
			}
		}
		return _template;
	}
	
	@Context
	private HttpServletResponse response;
	
	@Produces(MediaType.TEXT_HTML)
	@Path("@{authorname}/{permlink}")
	@GET
	public String player(@PathParam("authorname") String author, @PathParam("permlink") String permlink, @QueryParam("base-url") String baseUrl) {
		if (false) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return "NOT FOUND";
		}
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.TEXT_HTML);
		
//			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
//			return "SERVICE UNAVAILABLE";
		
		//https://steemitimages.com/0x0/https://steemitimages.com/0x0/http://gateway.ipfs.io/ipfs/Qmc4jbUWfHMn8B6jFbuJxCyMLkSiXud314x7eEJuG29VvF
		String embedHtml = template();
		embedHtml=embedHtml.replace("__TITLE__", "DPORNCO VIDEO PLAYER");
		embedHtml=embedHtml.replace("__POSTERHASH__", "Qmc4jbUWfHMn8B6jFbuJxCyMLkSiXud314x7eEJuG29VvF");
		embedHtml=embedHtml.replace("__VIDEOHASH__", "QmPRobJum5KF7Ge9f2XKskycduWbsTn71C2CFPbP2iHUKU");
		return embedHtml;
	}
	
}
