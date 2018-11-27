package co.dporn.gmd.shared;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DpornCoApi {

	@GET
	@Path("blog/entry/{username}/{permlink}")
	BlogEntryResponse getBlogEntry(@PathParam("username") String username, @PathParam("permlink") String permlink);

	@Path("blog/check/{username}/{permlink}")
	@GET
	void check(@PathParam("username") String username, @PathParam("permlink") String permlink);

	@Path("blog/confirm/{permlink}")
	@POST
	CommentConfirmResponse commentConfirm(@HeaderParam("username") String username,
			@HeaderParam("Authorization") String authorization, @PathParam("permlink") String permlink);

	@Consumes(MediaType.WILDCARD)
	@Path("ipfs/put/{filename}")
	@PUT
	IpfsHashResponse ipfsPut(InputStream is, @HeaderParam("username") String username,
			@HeaderParam("Authorization") String authorization, @PathParam("filename") String filename);

	@Path("suggest/{tag}")
	@GET
	SuggestTagsResponse suggest(@PathParam("tag") String tag);

	@Path("suggest")
	@GET
	SuggestTagsResponse suggest();

	@Path("ping")
	@GET
	PingResponse ping();

	@Path("embed/@{authorname}/{permlink}")
	@GET
	Map<String, String> embed(@PathParam("authorname") String author, @PathParam("permlink") String permlink);

	@Path("blog/entries/@{username}/{startId}/{count}")
	@GET
	BlogEntryListResponse blogEntriesFor(@PathParam("username") String username, @PathParam("startId") String startId,
			@PathParam("count") int count);

	@Path("blog/entries/@{username}/{count}")
	@GET
	BlogEntryListResponse blogEntriesFor(@PathParam("username") String username, @PathParam("count") int count);

	@Path("blog/entries/{entryType}/{startId}/{count}")
	@GET
	BlogEntryListResponse blogEntries(@PathParam("entryType") BlogEntryType entryType, @PathParam("startId") String startId,
			@PathParam("count") int count);

	@Path("blog/entries/{entryType}/{count}")
	@GET
	BlogEntryListResponse blogEntries(@PathParam("entryType") BlogEntryType entryType, @PathParam("count") int count);

	@Path("blogs/recent")
	@GET
	ActiveBlogsResponse blogsRecent();

	@Path("blog/info/@{username}")
	@GET
	ActiveBlogsResponse blogInfo(@PathParam("username") String username);
}
