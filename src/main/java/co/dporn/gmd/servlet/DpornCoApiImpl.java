package co.dporn.gmd.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import co.dporn.gmd.servlet.mongodb.MongoDpornCo;
import co.dporn.gmd.servlet.utils.HtmlSanitizer;
import co.dporn.gmd.servlet.utils.Mapper;
import co.dporn.gmd.servlet.utils.ResponseWithHeaders;
import co.dporn.gmd.servlet.utils.ServerSteemConnect;
import co.dporn.gmd.servlet.utils.ServerUtils;
import co.dporn.gmd.servlet.utils.steemj.DpornMetadata;
import co.dporn.gmd.servlet.utils.steemj.SJCommentMetadata;
import co.dporn.gmd.servlet.utils.steemj.SteemJInstance;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.BlogEntry;
import co.dporn.gmd.shared.BlogEntryListResponse;
import co.dporn.gmd.shared.BlogEntryResponse;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.CommentConfirmResponse;
import co.dporn.gmd.shared.DpornCoApi;
import co.dporn.gmd.shared.HtmlSanitizedResponse;
import co.dporn.gmd.shared.IpfsHashResponse;
import co.dporn.gmd.shared.MongoDate;
import co.dporn.gmd.shared.PingResponse;
import co.dporn.gmd.shared.SuggestTagsResponse;
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("1.0")
public class DpornCoApiImpl implements DpornCoApi {

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;
	@Context
	protected HttpHeaders headers;

	@Override
	public PingResponse ping() {
		return new PingResponse(true);
	}

	@Override
	public BlogEntryListResponse blogEntries(BlogEntryType entryType, String startId, int count) {
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		List<BlogEntry> entries = MongoDpornCo.listBlogEntries(entryType, startId, count);
		Set<String> accountNameList = new HashSet<>();
		Set<String> blacklist = new HashSet<>(SteemJInstance.get().getBlacklist());
		entries.forEach(p -> {
			if (blacklist.contains(p.getUsername())) {
				p.setPosterImagePath(null);
				p.setPosterImagePath(null);
				p.setPermlink(null);
				p.setScore(-1000);
				p.setTitle(null);
				p.setVideoPath(null);
			}
		});
		entries.forEach(p -> accountNameList.add(p.getUsername()));
		Map<String, AccountInfo> infoMap = SteemJInstance.get().getBlogDetails(accountNameList);
		BlogEntryListResponse response = new BlogEntryListResponse();
		response.setBlogEntries(entries);
		response.setInfoMap(infoMap);
		return response;
	}

	@Override
	public BlogEntryListResponse blogEntries(BlogEntryType entryType, int count) {
		return blogEntries(entryType, "", count);
	}

	@Override
	public ActiveBlogsResponse blogsRecent() {
		List<String> active = SteemJInstance.get().getActiveNsfwVerifiedList();
		List<String> sublist = active.subList(0, Math.min(active.size(), 16));
		ActiveBlogsResponse activeBlogsResponse = new ActiveBlogsResponse(sublist);
		activeBlogsResponse.setInfoMap(SteemJInstance.get().getBlogDetails(sublist));
		return activeBlogsResponse;
	}

	@Override
	public BlogEntryListResponse blogEntriesFor(String username, String startId, int count) {
		BlogEntryListResponse response = new BlogEntryListResponse();
		Set<String> blacklist = new HashSet<>(SteemJInstance.get().getBlacklist());
		if (blacklist.contains(username)) {
			response.setInfoMap(new HashMap<>());
			response.setBlogEntries(new ArrayList<>());
			return response;
		}
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		List<BlogEntry> entries = MongoDpornCo.listBlogEntriesFor(username, startId, count);
		Set<String> accountNameList = new HashSet<>();
		entries.forEach(p -> accountNameList.add(p.getUsername()));
		Map<String, AccountInfo> infoMap = SteemJInstance.get().getBlogDetails(accountNameList);
		response.setBlogEntries(entries);
		response.setInfoMap(infoMap);
		return response;
	}

	@Override
	public BlogEntryListResponse blogEntriesFor(String username, int count) {
		return blogEntriesFor(username, "", count);
	}

	@Override
	public Map<String, String> embed(String author, String permlink) {
		Map<String, String> embed = new HashMap<>();
		embed.put("embed", DpornCoEmbed.getEmbedHtml(author, permlink));
		return embed;
	}

	@Override
	public ActiveBlogsResponse blogInfo(String username) {
		ActiveBlogsResponse response = new ActiveBlogsResponse();
		Set<String> blacklist = new HashSet<>(SteemJInstance.get().getBlacklist());
		if (blacklist.contains(username)) {
			response.setAuthors(new ArrayList<>());
			response.setInfoMap(new HashMap<>());
			return response;
		}
		try {
			response.setInfoMap(SteemJInstance.get().getBlogDetails(Arrays.asList(username)));
		} catch (Exception e) {
			return response;
		}
		response.setAuthors(new ArrayList<>(response.getInfoMap().keySet()));
		return response;
	}

	@Override
	public SuggestTagsResponse suggest(String tag) {
		if (tag == null) {
			tag = "";
		}
		tag = tag.trim().toLowerCase();
		SuggestTagsResponse response = new SuggestTagsResponse();
		response.setTags(MongoDpornCo.getMatchingTags(tag));
		return response;
	}

	@Override
	public SuggestTagsResponse suggest() {
		return suggest("");
	}

	private static long _counter = System.currentTimeMillis();

	private static synchronized long nextCounter() {
		return (_counter = Math.max(_counter + 1, System.currentTimeMillis()));
	}

	protected String safeFilename(String filename) {
		filename = filename.trim();
		filename = filename.toLowerCase();
		filename = filename.replaceAll("[^a-z0-9\\.-_]", "-");
		filename = filename.replaceAll("-+", "-");
		filename = StringUtils.strip(filename, "-");
		if (filename.isEmpty()) {
			filename = String.valueOf(nextCounter());
		}
		if (filename.startsWith(".")) {
			filename = String.valueOf(nextCounter()) + filename;
		}
		if (filename.length() < 5) {
			filename = String.valueOf(nextCounter()) + "-" + filename;
		}
		return filename;
	}

	private boolean isAuthorized(String username, String authorization) {
		if (username == null) {
			System.err.println("isAuthorized: username is null");
			return false;
		}
		String meUsername = ServerSteemConnect.username(authorization);
		System.err.println("isAuthorized: meUsername = " + meUsername);
		boolean authorized = username.equalsIgnoreCase(meUsername);
		return authorized;
	}

	private void setResponseAsUnauthorized() {
		response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
		response.setContentType(MediaType.TEXT_PLAIN);
		try {
			response.getWriter().println("NOT AUTHORIZED");
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
		}
	}

	private static final String IPFS_EMPTY_DIR = "QmUNLLsPACCz1vLxQVkXqqLX5R1X345qqfHbsf67hvA3Nn";
	private static final String IPFS_GATEWAY = "http://localhost:8008/ipfs/";

	@Override
	public IpfsHashResponse ipfsPut(InputStream is, String username, String authorization, String filename) {
		if (!isAuthorized(username, authorization)) {
			setResponseAsUnauthorized();
			return null;
		}
		filename = safeFilename(filename);
		ResponseWithHeaders putResponse = ServerUtils.putStream(IPFS_GATEWAY + IPFS_EMPTY_DIR + "/" + filename, is,
				null);
		List<String> ipfsHashes = putResponse.getHeaders().get("ipfs-hash");
		List<String> locations = putResponse.getHeaders().get("location");
		IpfsHashResponse response = new IpfsHashResponse();
		response.setFilename(filename);
		if (!ipfsHashes.isEmpty()) {
			response.setIpfsHash(ipfsHashes.get(ipfsHashes.size() - 1));
			System.out.println("ipfsPut => ipfs-hash: " + ipfsHashes.toString());
		}
		if (!locations.isEmpty()) {
			response.setLocation(locations.get(locations.size() - 1));
			System.out.println("ipfsPut => location: " + locations.toString());
		}
		return response;
	}

	/**
	 * ffmpeg -hide_banner -y -i beach.mkv \ <br>
	 * -vf scale=w=640:h=360:force_original_aspect_ratio=decrease -c:a aac -ar 48000
	 * -c:v h264 -profile:v main -crf 20 -sc_threshold 0 -g 48 -keyint_min 48
	 * -hls_time 4 -hls_playlist_type vod -b:v 800k -maxrate 856k -bufsize 1200k
	 * -b:a 96k -hls_segment_filename beach/360p_%03d.ts beach/360p.m3u8 \ <br>
	 * -vf scale=w=842:h=480:force_original_aspect_ratio=decrease -c:a aac -ar 48000
	 * -c:v h264 -profile:v main -crf 20 -sc_threshold 0 -g 48 -keyint_min 48
	 * -hls_time 4 -hls_playlist_type vod -b:v 1400k -maxrate 1498k -bufsize 2100k
	 * -b:a 128k -hls_segment_filename beach/480p_%03d.ts beach/480p.m3u8 \ <br>
	 * -vf scale=w=1280:h=720:force_original_aspect_ratio=decrease -c:a aac -ar
	 * 48000 -c:v h264 -profile:v main -crf 20 -sc_threshold 0 -g 48 -keyint_min 48
	 * -hls_time 4 -hls_playlist_type vod -b:v 2800k -maxrate 2996k -bufsize 4200k
	 * -b:a 128k -hls_segment_filename beach/720p_%03d.ts beach/720p.m3u8 \ <br>
	 * -vf scale=w=1920:h=1080:force_original_aspect_ratio=decrease -c:a aac -ar
	 * 48000 -c:v h264 -profile:v main -crf 20 -sc_threshold 0 -g 48 -keyint_min 48
	 * -hls_time 4 -hls_playlist_type vod -b:v 5000k -maxrate 5350k -bufsize 7500k
	 * -b:a 192k -hls_segment_filename beach/1080p_%03d.ts beach/1080p.m3u8 <br>
	 * 
	 */

	/**
	 * 
	 */
	@Override
	public IpfsHashResponse ipfsPutVideo(InputStream is, String username, String authorization, String filename) {
		if (!isAuthorized(username, authorization)) {
			setResponseAsUnauthorized();
			return null;
		}
		IpfsHashResponse response = new IpfsHashResponse();
		File tmpDir = null;
		Process ffmpeg = null;
		try {
			String frameRate = "29.97";
			tmpDir = Files.createTempDirectory("hls-").toFile();
			System.out.println(" --- VID TEMP: " + tmpDir.getAbsoluteFile());
			List<String> cmd = new ArrayList<>();

			cmd.add("/usr/bin/nice");

			cmd.add("/usr/bin/ffmpeg");
			cmd.add("-hide_banner");
			cmd.add("-y");

			cmd.add("-blocksize");
			cmd.add("1k");
			cmd.add("-i");
			cmd.add("pipe:0");

			StringBuilder m3u8 = new StringBuilder();
			m3u8.append("#EXTM3U\n");
			m3u8.append("#EXT-X-VERSION:3\n");

			// 240p
			new File(tmpDir, "240p").mkdir();
			ffmpegOptionsFor(frameRate, 240, cmd);
			m3u8.append("#EXT-X-STREAM-INF:BANDWIDTH=500000,RESOLUTION=426x240\n");
			m3u8.append("240p/240p.m3u8\n");
			// 480p
			new File(tmpDir, "480p").mkdir();
			ffmpegOptionsFor(frameRate, 480, cmd);
			m3u8.append("#EXT-X-STREAM-INF:BANDWIDTH=1000000,RESOLUTION=854x480\n");
			m3u8.append("480p/480p.m3u8\n");
			// 720p
			new File(tmpDir, "720p").mkdir();
			ffmpegOptionsFor(frameRate, 720, cmd);
			m3u8.append("#EXT-X-STREAM-INF:BANDWIDTH=1500000,RESOLUTION=1280x720\n");
			m3u8.append("720p/720p.m3u8\n");
			// 1080p
			new File(tmpDir, "1080p").mkdir();
			ffmpegOptionsFor(frameRate, 1080, cmd);
			m3u8.append("#EXT-X-STREAM-INF:BANDWIDTH=3000000,RESOLUTION=1920x1080\n");
			m3u8.append("1080p/1080p.m3u8\n");

			File video_m3u8 = new File(tmpDir, "video.m3u8");
			FileUtils.write(video_m3u8, m3u8.toString(), StandardCharsets.UTF_8);

			FileUtils.write(new File(tmpDir, "ffmpeg.txt"), StringUtils.join(cmd, " ") + "\n", StandardCharsets.UTF_8);

			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(tmpDir);

			pb.redirectError(new File(tmpDir, "log.err"));
			pb.redirectOutput(new File(tmpDir, "log.out"));

			ffmpeg = pb.start();
			ServerUtils.copyStream(is, ffmpeg.getOutputStream());
			System.out.print("- TRANSCODE FINISHED");
			IOUtils.closeQuietly(ffmpeg.getOutputStream());
			ffmpeg.waitFor();
			
			/*
			 * put in a basic player for direct IPFS playback
			 */
			String player = DpornCoEmbed.htmlTemplateVideo();
			player = player.replace("__TITLE__", StringEscapeUtils.escapeHtml4(filename));
			player = player.replaceAll("poster=\"[^\"]*?\"", "");
			player = player.replaceAll("<source src=\"[^\"]*?__VIDEOPATH__\"\\s+type=\"video/mp4\" />", "<source src=\"video.m3u8\"/>");
			FileUtils.write(new File(tmpDir, "video.html"), player.toString(), StandardCharsets.UTF_8);

			List<File> files = new ArrayList<>(FileUtils.listFiles(tmpDir, null, true));
			/*
			 * sort the data by name but force the file video.m3u8 to be last in the list - THIS IS IMPORTANT TO DO!
			 */
			Collections.sort(files, (a,b) ->{
				if (a.equals(video_m3u8)) {
					return 1;
				}
				if (b.equals(video_m3u8)) {
					return -1;
				}
				return a.getAbsolutePath().compareTo(b.getAbsolutePath());
			});
			String IPFS_HASH = IPFS_EMPTY_DIR;
			for (File file : files) {
				String destFilename = StringUtils.substringAfter(file.getAbsolutePath(), tmpDir.getAbsolutePath());
				System.out.println("   DEST FILE: " + destFilename);
				ResponseWithHeaders putResponse = ServerUtils.putFile(IPFS_GATEWAY + IPFS_HASH + destFilename, file,
						null);
				List<String> ipfsHashes = putResponse.getHeaders().get("ipfs-hash");
				List<String> locations = putResponse.getHeaders().get("location");
				if (!ipfsHashes.isEmpty()) {
					IPFS_HASH = ipfsHashes.get(ipfsHashes.size() - 1);
					response.setIpfsHash(IPFS_HASH);
				}
				if (!locations.isEmpty()) {
					response.setLocation(locations.get(locations.size() - 1));
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (ffmpeg != null) {
				ffmpeg.destroyForcibly();
			}
			if (tmpDir != null) {
				 FileUtils.deleteQuietly(tmpDir);
			}
		}
		return response;
	}

	private void ffmpegOptionsFor(String frameRate, int size, List<String> cmd) {
		cmd.add("-c:a");
		cmd.add("aac");
		cmd.add("-ar");
		cmd.add("48000");
		cmd.add("-b:a");

		if (size < 360) {
			cmd.add("64k");
		} else if (size < 480) {
			cmd.add("96k");
		} else if (size < 1080) {
			cmd.add("128k");
		} else {
			cmd.add("192k");
		}

		cmd.add("-r");
		cmd.add(frameRate);

		cmd.add("-g");
		cmd.add(frameRate);

		cmd.add("-preset");
		cmd.add("ultrafast");

		cmd.add("-tune");
		cmd.add("film");

		cmd.add("-movflags");
		cmd.add("+faststart");

		cmd.add("-crf");
		cmd.add("28");

		cmd.add("-keyint_min");
		cmd.add(frameRate);
		cmd.add("-sc_threshold");
		cmd.add("0");

		cmd.add("-c:v");
		cmd.add("h264");
		cmd.add("-profile:v");
		cmd.add("main");

		cmd.add("-maxrate");
		if (size < 360) {
			cmd.add("500k");
		} else if (size < 480) {
			cmd.add("750k");
		} else if (size < 720) {
			cmd.add("1000k");
		} else if (size < 1080) {
			cmd.add("1500k");
		} else {
			cmd.add("3000k");
		}

		cmd.add("-bufsize");
		if (size < 360) {
			cmd.add("1000k");
		} else if (size < 480) {
			cmd.add("1500k");
		} else if (size < 720) {
			cmd.add("2000k");
		} else if (size < 1080) {
			cmd.add("3000k");
		} else {
			cmd.add("6000k");
		}

		cmd.add("-vf");
		if (size < 360) {
			cmd.add("scale=w=426x240:force_original_aspect_ratio=decrease,pad=w='iw+mod(iw,2)':h='ih+mod(ih,2)'");
		} else if (size < 480) {
			cmd.add("scale=w=640x360:force_original_aspect_ratio=decrease,pad=w='iw+mod(iw,2)':h='ih+mod(ih,2)'");
		} else if (size < 720) {
			cmd.add("scale=w=854x480:force_original_aspect_ratio=decrease,pad=w='iw+mod(iw,2)':h='ih+mod(ih,2)'");
		} else if (size < 1080) {
			cmd.add("scale=w=1280x720:force_original_aspect_ratio=decrease,pad=w='iw+mod(iw,2)':h='ih+mod(ih,2)'");
		} else {
			cmd.add("scale=w=1920x1080:force_original_aspect_ratio=decrease,pad=w='iw+mod(iw,2)':h='ih+mod(ih,2)'");
		}

		cmd.add("-hls_time");
		cmd.add("1");
		cmd.add("-hls_playlist_type");
		cmd.add("vod");
		cmd.add("-hls_segment_filename");

		String template;
		if (size < 360) {
			template = "240p";
		} else if (size < 480) {
			template = "360p";
		} else if (size < 720) {
			template = "480p";
		} else if (size < 1080) {
			template = "720p";
		} else {
			template = "1080p";
		}

		cmd.add(template + "/" + template + "_%09d.ts");
		cmd.add(template + "/" + template + ".m3u8");
	}

	@Override
	public CommentConfirmResponse commentConfirm(String username, String authorization, String permlink) {
		if (!isAuthorized(username, authorization)) {
			setResponseAsUnauthorized();
			return null;
		}
		if (username.equals(MongoDpornCo.getEntry(username, permlink).getUsername())) {
			return new CommentConfirmResponse(true);
		}
		;
		Discussion content = SteemJInstance.get().getContent(username, permlink);
		SJCommentMetadata metadata;
		try {
			metadata = Mapper.get().readValue(content.getJsonMetadata(), SJCommentMetadata.class);
		} catch (IOException e) {
			e.printStackTrace();
			return new CommentConfirmResponse(false);
		}
		if (metadata == null) {
			return new CommentConfirmResponse(false);
		}
		DpornMetadata dpornMetadata = metadata.getDpornMetadata();
		if (dpornMetadata == null) {
			return new CommentConfirmResponse(false);
		}

		BlogEntry entry = new BlogEntry();
		Set<String> extractedTags = new LinkedHashSet<>(Arrays.asList(metadata.getTags()));
		extractedTags.add("@" + username);

		entry.setId(null);
		entry.setTitle(content.getTitle());
		entry.setPermlink(permlink);
		entry.setContent(content.getBody());
		entry.setPostTags(new ArrayList<>(extractedTags));
		entry.setCommunityTags(new ArrayList<>(extractedTags));
		entry.setGalleryImagePaths(dpornMetadata.getPhotoGalleryImagePaths());
		entry.setVideoPath(dpornMetadata.getVideoPath());
		entry.setPosterImagePath(dpornMetadata.getPosterImagePath());
		entry.setUsername(username);
		entry.setCommentJsonMetadata(content.getJsonMetadata());
		entry.setCreated(new MongoDate(content.getCreated().getDateTimeAsDate()));
		entry.setModified(entry.getCreated());
		entry.setEntryType(dpornMetadata.getEntryType());
		entry.setGalleryImageThumbPaths(dpornMetadata.getPhotoGalleryImagePaths());
		entry.setMigrated(false);
		entry.setApp(dpornMetadata.getApp());
		entry.setEmbed(dpornMetadata.getEmbed());

		return new CommentConfirmResponse(MongoDpornCo.insertEntry(entry));
	}

	@Override
	public void check(String username, String permlink) {
		if (username == null || permlink == null) {
			return;
		}
		if (username.trim().isEmpty() || permlink.trim().isEmpty()) {
			return;
		}
		username = username.toLowerCase().trim();
		permlink = permlink.trim();
		BlogEntry entry = MongoDpornCo.getEntry(username, permlink);
		if (entry == null || !username.equals(entry.getUsername())) {
			return;
		}
		synchronized (DpornCoApiImpl.class) {
			Discussion content;
			try {
				content = SteemJInstance.get().getContent(username, permlink);
				if (content == null || content.getAuthor() == null || !username.equals(content.getAuthor().getName())) {
					System.err.println("BAD ENTRY: " + username + " | " + permlink);
					MongoDpornCo.deleteEntry(username, permlink);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public BlogEntryResponse getBlogEntry(String username, String permlink) {
		BlogEntryResponse response = new BlogEntryResponse();
		response.setBlogEntry(MongoDpornCo.getEntry(username, permlink));
		return response;
	}

	@Override
	public HtmlSanitizedResponse getHtmlSanitized(String username, String authorization, String html) {
		if (!isAuthorized(username, authorization)) {
			setResponseAsUnauthorized();
			return null;
		}
		if (html == null) {
			html = "";
		}
		HtmlSanitizedResponse response = new HtmlSanitizedResponse();
		response.setSanitizedHtml(HtmlSanitizer.get().sanitize(html));
		return response;
	}

}
