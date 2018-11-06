package co.dporn.gmd.shared;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DpornCoApi {
	@Path("ping")
	@GET
	PingResponse ping();
	
	@Path("embed/@{authorname}/{permlink}")
	@GET
	Map<String, String> embed(@PathParam("authorname") String author, @PathParam("permlink") String permlink);
	
	@Path("posts/@{username}/{startId}/{count}")
	@GET
	PostListResponse postsFor(@PathParam("username")String username, @PathParam("startId")String startId, @PathParam("count")int count);
	
	@Path("posts/@{username}/{count}")
	@GET
	PostListResponse postsFor(@PathParam("username")String username, @PathParam("count")int count);
	
	@Path("posts/{startId}/{count}")
	@GET
	PostListResponse posts(@PathParam("startId")String startId, @PathParam("count")int count);
	
	@Path("posts/{count}")
	@GET
	PostListResponse posts(@PathParam("count")int count);
	
	@Path("blogs/recent")
	@GET
	ActiveBlogsResponse blogsRecent();
}
