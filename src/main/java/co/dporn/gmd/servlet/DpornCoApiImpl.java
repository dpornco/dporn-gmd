package co.dporn.gmd.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import co.dporn.gmd.servlet.mongodb.MongoDpornCo;
import co.dporn.gmd.servlet.utils.HtmlSanitizer;
import co.dporn.gmd.servlet.utils.Mapper;
import co.dporn.gmd.servlet.utils.Notifications;
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
import co.dporn.gmd.shared.IsVerifiedResponse;
import co.dporn.gmd.shared.MongoDate;
import co.dporn.gmd.shared.NotificationsResponse;
import co.dporn.gmd.shared.PingResponse;
import co.dporn.gmd.shared.SuggestTagsResponse;
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("1.0")
public class DpornCoApiImpl implements DpornCoApi {

	private static final boolean USE_FMP4 = false;

	private static final BigDecimal HLS_SPLIT_TIME_SECONDS = new BigDecimal("1");
	private static final BigDecimal FRAME_RATE = new BigDecimal("30");
	private static final int MAX_CONCURRENT_UPLOADS = 8;
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
		List<String> active = SteemJInstance.get().getActiveDpornVerifiedList();
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

	private static Map<String, String> cachedUsernameAuthorizations = new PassiveExpiringMap<>(60000);

	private boolean isAuthorized(String username, String authorization) {
		if (username == null) {
			return false;
		}
		String meUsername = cachedUsernameAuthorizations.get(authorization);
		if (meUsername == null) {
			meUsername = ServerSteemConnect.username(authorization);
			cachedUsernameAuthorizations.put(authorization, meUsername);
		}
		boolean authorized = username.equalsIgnoreCase(meUsername);
		if (SteemJInstance.get().getBlacklist().contains(username)) {
			return false;
		}
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

	private static Semaphore semaphore = new Semaphore(MAX_CONCURRENT_UPLOADS, true);

	private static class IpfsHash {
		String hash;

		@Override
		public String toString() {
			return hash;
		}
	}

	private static class TimeMark {
		long ms;
	}

	/**
	 * 
	 */
	@Override
	public IpfsHashResponse ipfsPutVideo(InputStream is, String username, String authorization, String filename,
			int width, int height) {
		if (!isAuthorized(username, authorization)) {
			setResponseAsUnauthorized();
			return null;
		}

		boolean isDpornVerified = SteemJInstance.get().getDpornVerifiedSet().contains(username);

		boolean do1080p = isDpornVerified;
		boolean do720p = isDpornVerified;
		boolean do480p = true;
		boolean do360p = isDpornVerified;
		boolean do240p = true;

		System.out.println(
				"ipfsPutVideo: " + username + ", " + filename + " [" + semaphore.availablePermits() + " slots]");
		String contentType = String.valueOf(request.getContentType() == null ? "" : request.getContentType())
				.toLowerCase();
		if (filename == null) {
			filename = "video.bin";
		}
		String guessedMimeType = request.getServletContext().getMimeType(filename).toLowerCase();
		System.out.println(" - contentType: " + contentType);
		System.out.println(" - guessed content type: " + guessedMimeType);
		boolean useTempFile = contentType.contains("quicktime") || guessedMimeType.contains("quicktime");

		if (width <= 0 || height <= 0) {
			System.out.println("ipfsPutVideo - bad dimensions: " + width + "x" + height);
			if (width <= 0) {
				width = 640;
			}
			if (height <= 0) {
				height = 360;
			}
		}
		IpfsHashResponse response = new IpfsHashResponse();
		boolean acquired = false;
		File tmpDir;
		try {
			tmpDir = Files.createTempDirectory("hls-").toFile();
		} catch (IOException e) {
			response.setError(e.getMessage());
			return response;
		}
		Process ffmpeg = null;
		final String player = DpornCoEmbed.htmlTemplateVideo().replace("video/mp4", "application/x-mpegurl");
		try {
			acquired = semaphore.tryAcquire(10, TimeUnit.SECONDS);
			if (!acquired) {
				response.setTryAgain(true);
				response.setError("UPLOAD QUEUE FULL - PLEASE TRY AGAIN LATER");
				return response;
			}
			System.out.println("ipfsPutVideo - semaphore acquired: " + username + ", " + filename);
			System.out.println(" - upload slots remaining: " + semaphore.availablePermits());
			System.out.println(" - using temporary file: " + useTempFile);

			System.out.println(" --- VID TEMP: " + tmpDir.getAbsoluteFile());

			List<String> cmd = new ArrayList<>();

			cmd.add("/usr/bin/nice");

			cmd.add("/usr/bin/ffmpeg");

			cmd.add("-hide_banner");
			cmd.add("-y");

			cmd.add("-blocksize");
			cmd.add("128k");

			if (useTempFile) {
				cmd.add("-i");
				cmd.add("tmp.mov");
			} else {
				cmd.add("-i");
				cmd.add("pipe:0");
			}

			if (isDpornVerified) {
				System.out.println(" - time limit: 60 minutes");
				cmd.add("-t");
				cmd.add("1:00:00");
			} else {
				System.out.println(" - time limit: 15 minutes");
				cmd.add("-t");
				cmd.add("15:00");
			}

			StringBuilder m3u8 = new StringBuilder();
			m3u8.append("#EXTM3U\n");
			m3u8.append("#EXT-X-VERSION:7\n");
			m3u8.append("#EXT-X-ALLOW-CACHE:YES\n");
			m3u8.append("#EXT-X-PLAYLIST-TYPE:VOD\n");

			// 1080p
			if (do1080p && height >= (1080 + 720) / 2) {
				ffmpegOptionsFor(isDpornVerified, 1080, cmd);
				m3u8.append("#EXT-X-STREAM-INF:HDCP-LEVEL=NONE,FRAME-RATE=" + FRAME_RATE.toPlainString()
						+ ",BANDWIDTH=4900000,RESOLUTION=1920x1080\n");
				m3u8.append("1080p.m3u8\n");
				String hlsPlayer = player;
				hlsPlayer = hlsPlayer.replace("__TITLE__", StringEscapeUtils.escapeHtml4(filename));
				hlsPlayer = hlsPlayer.replaceAll("poster=\"[^\"]*?\"", "");
				hlsPlayer = hlsPlayer.replaceAll("src\\s*:\\s*\".*?\"", "src: \"1080p.m3u8\"");
				FileUtils.write(new File(tmpDir, "1080p-video.html"), hlsPlayer.toString(), StandardCharsets.UTF_8);
			} else {
				do1080p = false;
			}

			// 720p
			if (do720p && height >= (720 + 480) / 2) {
				ffmpegOptionsFor(isDpornVerified, 720, cmd);
				m3u8.append("#EXT-X-STREAM-INF:HDCP-LEVEL=NONE,FRAME-RATE=" + FRAME_RATE.toPlainString()
						+ ",BANDWIDTH=2850000,RESOLUTION=1280x720\n");
				m3u8.append("720p.m3u8\n");
				String hlsPlayer = player;
				hlsPlayer = hlsPlayer.replace("__TITLE__", StringEscapeUtils.escapeHtml4(filename));
				hlsPlayer = hlsPlayer.replaceAll("poster=\"[^\"]*?\"", "");
				hlsPlayer = hlsPlayer.replaceAll("src\\s*:\\s*\".*?\"", "src: \"720p.m3u8\"");
				FileUtils.write(new File(tmpDir, "720p-video.html"), hlsPlayer.toString(), StandardCharsets.UTF_8);
			} else {
				do720p = false;
			}

			// 480p
			if (do480p && height >= (480 + 360) / 2) {
				ffmpegOptionsFor(isDpornVerified, 480, cmd);
				m3u8.append("#EXT-X-STREAM-INF:HDCP-LEVEL=NONE,FRAME-RATE=" + FRAME_RATE.toPlainString()
						+ ",BANDWIDTH=1425000,RESOLUTION=854x480\n");
				m3u8.append("480p.m3u8\n");
				String hlsPlayer = player;
				hlsPlayer = hlsPlayer.replace("__TITLE__", StringEscapeUtils.escapeHtml4(filename));
				hlsPlayer = hlsPlayer.replaceAll("poster=\"[^\"]*?\"", "");
				hlsPlayer = hlsPlayer.replaceAll("src\\s*:\\s*\".*?\"", "src: \"480p.m3u8\"");
				FileUtils.write(new File(tmpDir, "480p-video.html"), hlsPlayer.toString(), StandardCharsets.UTF_8);
			} else {
				do480p = false;
			}

			// 360p
			if (do360p && height >= (360 + 240) / 2) {
				ffmpegOptionsFor(isDpornVerified, 360, cmd);
				m3u8.append("#EXT-X-STREAM-INF:HDCP-LEVEL=NONE,FRAME-RATE=" + FRAME_RATE.toPlainString()
						+ ",BANDWIDTH=800000,RESOLUTION=640x360\n");
				m3u8.append("360p.m3u8\n");
				String hlsPlayer = player;
				hlsPlayer = hlsPlayer.replace("__TITLE__", StringEscapeUtils.escapeHtml4(filename));
				hlsPlayer = hlsPlayer.replaceAll("poster=\"[^\"]*?\"", "");
				hlsPlayer = hlsPlayer.replaceAll("src\\s*:\\s*\".*?\"", "src: \"360p.m3u8\"");
				FileUtils.write(new File(tmpDir, "360p-video.html"), hlsPlayer.toString(), StandardCharsets.UTF_8);
			} else {
				do360p = false;
			}

			// 240p
			if (do240p) {
				ffmpegOptionsFor(isDpornVerified, 240, cmd);
				m3u8.append("#EXT-X-STREAM-INF:HDCP-LEVEL=NONE,FRAME-RATE=" + FRAME_RATE.toPlainString()
						+ ",BANDWIDTH=500000,RESOLUTION=426x240\n");
				m3u8.append("240p.m3u8\n");
				String hlsPlayer = player;
				hlsPlayer = hlsPlayer.replace("__TITLE__", StringEscapeUtils.escapeHtml4(filename));
				hlsPlayer = hlsPlayer.replaceAll("poster=\"[^\"]*?\"", "");
				hlsPlayer = hlsPlayer.replaceAll("src\\s*:\\s*\".*?\"", "src: \"240p.m3u8\"");
				FileUtils.write(new File(tmpDir, "240p-video.html"), hlsPlayer.toString(), StandardCharsets.UTF_8);
			} else {
				do240p = false;
			}

			m3u8.append("#EXT-X-ENDLIST\n");

			File video_m3u8 = new File(tmpDir, "video.m3u8");
			FileUtils.write(video_m3u8, m3u8.toString(), StandardCharsets.UTF_8);

			FileUtils.write(new File(tmpDir, "ffmpeg.txt"), StringUtils.join(cmd, " ") + "\n", StandardCharsets.UTF_8);

			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(tmpDir);
			pb.redirectErrorStream(true);
			pb.redirectOutput(new File(tmpDir, "log.txt"));

			if (useTempFile) {
				Notifications.notify(authorization, "Copying upload to temporary file");
				FileOutputStream os = new FileOutputStream(new File(tmpDir, "tmp.mov"));
				ServerUtils.copyStream(is, os);
				IOUtils.closeQuietly(os);
			}

			IpfsHash ipfsHash = new IpfsHash();
			ipfsHash.hash = IPFS_EMPTY_DIR;
			Set<File> alreadyAdded = new HashSet<>();

			ffmpeg = pb.start();
			if (!useTempFile) {
				TimeMark nextNotify = new TimeMark();
				TimeMark nextAdd = new TimeMark();
				ServerUtils.copyStreamWithNotify(is, ffmpeg.getOutputStream(), () -> {
					if (nextNotify.ms < System.currentTimeMillis()) {
						Notifications.notify(authorization, "Converting to streaming format");
						nextNotify.ms = System.currentTimeMillis() + 14000l;
					}
					if (nextAdd.ms < System.currentTimeMillis()) {
						addSegmentsToIpfsAsTheyAppear(tmpDir, ipfsHash, alreadyAdded, response);
						nextAdd.ms = System.currentTimeMillis() + 500l;
					}
				});
				IOUtils.closeQuietly(ffmpeg.getOutputStream());
			}
			if (useTempFile) {
				TimeMark nextNotify = new TimeMark();
				TimeMark nextAdd = new TimeMark();
				while (ffmpeg.isAlive()) {
					if (nextNotify.ms < System.currentTimeMillis()) {
						Notifications.notify(authorization, "Converting to streaming format");
						nextNotify.ms = System.currentTimeMillis() + 14000l;
					}
					if (nextAdd.ms < System.currentTimeMillis()) {
						addSegmentsToIpfsAsTheyAppear(tmpDir, ipfsHash, alreadyAdded, response);
						nextAdd.ms = System.currentTimeMillis() + 500l;
					}
					Thread.sleep(500);
				}
			}
			ffmpeg.waitFor();

			/*
			 * Supply a basic player for direct IPFS playback
			 */
			String hlsPlayer = player;
			hlsPlayer = hlsPlayer.replace("__TITLE__", StringEscapeUtils.escapeHtml4(filename));
			hlsPlayer = hlsPlayer.replaceAll("poster=\"[^\"]*?\"", "");
			hlsPlayer = hlsPlayer.replaceAll("src\\s*:\\s*\".*?\"", "src: \"video.m3u8\"");
			FileUtils.write(new File(tmpDir, "video.html"), hlsPlayer.toString(), StandardCharsets.UTF_8);

			List<File> files = new ArrayList<>(FileUtils.listFiles(tmpDir, null, true));
			/*
			 * Sort the files by name ascending but force the file video.m3u8 to be last in
			 * the list - Having video.m3u8 be last is important.
			 */
			Collections.sort(files, (a, b) -> {
				if (a.equals(video_m3u8)) {
					return 1;
				}
				if (b.equals(video_m3u8)) {
					return -1;
				}
				return a.getAbsolutePath().compareTo(b.getAbsolutePath());
			});

			// go ahead and release the semaphore
			synchronized (semaphore) {
				semaphore.release();
				acquired = false;
				System.out.println(" - upload slots remaining: " + semaphore.availablePermits());
			}

			long nextNotify = 0l;
			int count = 0;
			int size = files.size();
			int percent = -1;
			nextNotify = 0l;
			for (File file : files) {
				count++;
				if (nextNotify < System.currentTimeMillis()) {
					nextNotify = System.currentTimeMillis() + 6000l;
					percent = 100 * count / size;
					Notifications.notify(authorization, "Adding HLS video to IPFS: " + percent + "%");
				}
				if (file.getName().equalsIgnoreCase("tmp.mov")) {
					continue;
				}
				if (alreadyAdded.contains(file)) {
					continue;
				}
				if (Thread.interrupted()) {
					System.out.println("IPFS POST INTERRUPTED");
					throw new InterruptedException("IPFS POST INTERRUPTED");
				}
				String destFilename = StringUtils.substringAfter(file.getAbsolutePath(), tmpDir.getAbsolutePath());
				System.out.println("   DEST FILE: " + destFilename);
				ResponseWithHeaders putResponse = ServerUtils.putFile(IPFS_GATEWAY + ipfsHash.hash + destFilename, file,
						null);
				List<String> ipfsHashes = putResponse.getHeaders().get("ipfs-hash");
				List<String> locations = putResponse.getHeaders().get("location");
				if (!ipfsHashes.isEmpty()) {
					ipfsHash.hash = ipfsHashes.get(ipfsHashes.size() - 1);
					response.setIpfsHash(ipfsHash.hash);
				}
				if (!locations.isEmpty()) {
					response.setLocation(locations.get(locations.size() - 1));
				}
			}
			Notifications.notify(authorization, "HLS video added to IPFS.");
			System.out.println(" VIDEO FOLDER: https://ipfs.dporn.co/ipfs/" + ipfsHash.hash);
			return response;
		} catch (Exception e) {
			response.setError(e.getMessage());
			return response;
		} finally {
			if (acquired) {
				semaphore.release();
				System.out.println(" - upload slots remaining: " + semaphore.availablePermits());
			}
			if (ffmpeg != null) {
				IOUtils.closeQuietly(ffmpeg.getOutputStream());
				if (ffmpeg.isAlive()) {
					ffmpeg.destroyForcibly();
				}
			}
			if (tmpDir != null) {
				FileUtils.deleteQuietly(tmpDir);
			}
		}
	}

	/**
	 * Set to only add *.mp4, *.m4s, and *.ts.
	 * 
	 * @param tmpDir
	 * @param ipfsHash
	 * @param alreadyAdded
	 * @param response
	 */
	private void addSegmentsToIpfsAsTheyAppear(File tmpDir, IpfsHash ipfsHash, Set<File> alreadyAdded,
			IpfsHashResponse response) {
		synchronized (alreadyAdded) {
			List<File> files = new ArrayList<>(FileUtils.listFiles(tmpDir, null, true));
			Collections.sort(files);
			Iterator<File> iFiles = files.iterator();
			while (iFiles.hasNext()) {
				File next = iFiles.next();
				if (alreadyAdded.contains(next)) {
					continue;
				}
				String lcName = next.getName().toLowerCase();
				if (!lcName.endsWith(".ts") && !lcName.endsWith(".mp4") && !lcName.endsWith(".m4s")) {
					continue;
				}
				String destFilename = StringUtils.substringAfter(next.getAbsolutePath(), tmpDir.getAbsolutePath());
				System.out.println("   DEST FILE [early IPFS add]: " + destFilename);
				ResponseWithHeaders putResponse;
				try {
					putResponse = ServerUtils.putFile(IPFS_GATEWAY + ipfsHash.hash + destFilename, next, null);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				List<String> ipfsHashes = putResponse.getHeaders().get("ipfs-hash");
				List<String> locations = putResponse.getHeaders().get("location");
				if (!ipfsHashes.isEmpty()) {
					ipfsHash.hash = ipfsHashes.get(ipfsHashes.size() - 1);
					response.setIpfsHash(ipfsHash.hash);
				}
				if (!locations.isEmpty()) {
					response.setLocation(locations.get(locations.size() - 1));
					alreadyAdded.add(next);
				}
			}
		}
	}

	private void ffmpegOptionsFor(boolean isDpornVerified, int size, List<String> cmd) {
		BigDecimal tsLength = FRAME_RATE.multiply(HLS_SPLIT_TIME_SECONDS);

		if (size >= (1080 + 720) / 2) {
			size = 1080;
		} else if (size >= (720 + 480) / 2) {
			size = 720;
		} else if (size >= (480 + 360) / 2) {
			size = 480;
		} else if (size >= (360 + 240) / 2) {
			size = 360;
		} else {
			size = 240;
		}

		cmd.add("-threads");
		cmd.add("1");

		cmd.add("-c:a");
		cmd.add("aac");
		cmd.add("-ar");
		cmd.add("48000");
		cmd.add("-b:a");

		switch (size) {
		case 1080:
			cmd.add("192k");
			break;
		case 720:
			cmd.add("128k");
			break;
		case 480:
			cmd.add("128k");
			break;
		case 360:
			cmd.add("96k");
			break;
		default:
			cmd.add("64k");
		}

		cmd.add("-r");
		cmd.add(FRAME_RATE.toPlainString());

		cmd.add("-g");
		cmd.add(tsLength.toPlainString());

		cmd.add("-preset");
		cmd.add("fast");

		cmd.add("-tune");
		cmd.add("film");

		cmd.add("-keyint_min");
		cmd.add(tsLength.toPlainString());
		cmd.add("-sc_threshold");
		cmd.add("0");

		cmd.add("-c:v");
		cmd.add("h264");
		/*
		 * https://trac.ffmpeg.org/wiki/Encode/H.264
		 * 
		 * If you want your videos to have highest compatibility with ancient devices
		 * (e.g., old Android phones): -profile:v baseline -level 3.0
		 */
		cmd.add("-profile:v");
		cmd.add("baseline");
		cmd.add("-level");
		cmd.add("3.0");

		if (isDpornVerified) {
			cmd.add("-crf");
			cmd.add("24");
		} else {
			cmd.add("-crf");
			cmd.add("28");
		}

		// Max target bitrates are set the middle between high motion and low motion
		// recommended settings
		cmd.add("-maxrate");
		switch (size) {
		case 1080:
			cmd.add("4900k");
			break;
		case 720:
			cmd.add("2850k");
			break;
		case 480:
			cmd.add("1425k");
			break;
		case 360:
			cmd.add("800k");
			break;
		default:
			cmd.add("500k");
		}

		// Must be set to same as max rate to prevent excessive bitrate spikes
		cmd.add("-bufsize");
		switch (size) {
		case 1080:
			cmd.add("4900k");
			break;
		case 720:
			cmd.add("2850k");
			break;
		case 480:
			cmd.add("1425k");
			break;
		case 360:
			cmd.add("800k");
			break;
		default:
			cmd.add("500k");
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

		/*
		 * https://trac.ffmpeg.org/wiki/Encode/H.264
		 * 
		 * QuickTime
		 * 
		 * Although other pixel formats may be supported, YUV planar color space with
		 * 4:2:0 chroma subsampling is a safe pixel format for H.264 video; use -vf
		 * format=yuv420p or -pix_fmt yuv420p.
		 */
		cmd.add("-pix_fmt");
		cmd.add("yuv420p");

		/*
		 * faststart for web video
		 * 
		 * 
		 * You can add -movflags +faststart as an output option if your videos are going
		 * to be viewed in a browser. This will move some information to the beginning
		 * of your file and allow the video to begin playing before it is completely
		 * downloaded by the viewer. It is not required if you are going to use a video
		 * service such as YouTube. YouTube ​recommends using faststart, so they can
		 * begin re-encoding before uploads complete.
		 */
		cmd.add("-movflags");
		cmd.add("+faststart");

		cmd.add("-hls_allow_cache");
		cmd.add("1");

		cmd.add("-hls_segment_type");
		if (USE_FMP4) {
			cmd.add("fmp4");
		} else {
			cmd.add("mpegts");
		}

		cmd.add("-hls_time");
		cmd.add(HLS_SPLIT_TIME_SECONDS.toPlainString());

		cmd.add("-hls_playlist_type");
		cmd.add("vod");

		String template;
		switch (size) {
		case 1080:
			template = "1080p";
			break;
		case 720:
			template = "720p";
			break;
		case 480:
			template = "480p";
			break;
		case 360:
			template = "360p";
			break;
		default:
			template = "240p";
		}

		if (USE_FMP4) {
			cmd.add("-hls_fmp4_init_filename");
			cmd.add(template + "-init.mp4");
		}

		cmd.add("-hls_segment_filename");
		if (USE_FMP4) {
			cmd.add(template + "-%012d.mp4");
		} else {
			cmd.add(template + "-%012d.ts");
		}

		cmd.add("-hls_flags");
		cmd.add("independent_segments");
		cmd.add("-hls_flags");
		cmd.add("program_date_time");
		cmd.add("-hls_flags");
		cmd.add("temp_file");

		cmd.add(template + ".m3u8");
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
					System.out.println("BAD ENTRY: " + username + " | " + permlink);
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

	@Override
	public IsVerifiedResponse getIsVerified(String username) {
		return new IsVerifiedResponse(username, SteemJInstance.get().getDpornVerifiedSet().contains(username));
	}

	@Override
	public NotificationsResponse getNotifications(String username, String authorization) {
		if (!isAuthorized(username, authorization)) {
			setResponseAsUnauthorized();
			return null;
		}
		return new NotificationsResponse(Notifications.getNotifications(authorization));
	}

}
