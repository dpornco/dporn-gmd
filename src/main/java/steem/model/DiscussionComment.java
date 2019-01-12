package steem.model;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Represents the response object from "get content". Only implements fields we are interested in, all other data is ignored.
 * @author muksihs
 *
 */
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class DiscussionComment {
	private List<Vote> activeVotes;
	private String author;
	private String body;
	private String category;
	private long children;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
	private Date created;
	private String curatorPayoutValue;
	private BigInteger id;
	
	private String jsonMetadata;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
	private Date lastPayout;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
	private Date lastUpdate;
	private String parentAuthor;
	private String parentPermlink;
	private String pendingPayoutValue;
	private String permlink;
	private BigInteger netVotes;
	private String rootAuthor;
	private String rootPermlink;
	private String rootTitle;
	private String title;
	private String totalPayoutValue;
	private String totalPendingPayoutValue;
	private String url;
	@JsonProperty("active_votes")
	public List<Vote> getActiveVotes() {
		return activeVotes;
	}
	public String getAuthor() {
		return author;
	}
	public String getBody() {
		return body;
	}
	public String getCategory() {
		return category;
	}
	public long getChildren() {
		return children;
	}
	public Date getCreated() {
		return created;
	}
	@JsonProperty("curator_payout_value")
	public String getCuratorPayoutValue() {
		return curatorPayoutValue;
	}
	public BigInteger getId() {
		return id;
	}
	@JsonProperty("json_metadata")
	public String getJsonMetadata() {
		return jsonMetadata;
	}
	@JsonProperty("last_payout")
	public Date getLastPayout() {
		return lastPayout;
	}
	@JsonProperty("last_update")
	public Date getLastUpdate() {
		return lastUpdate;
	}
	@JsonProperty("parent_author")
	public String getParentAuthor() {
		return parentAuthor;
	}
	@JsonProperty("parent_permlink")
	public String getParentPermlink() {
		return parentPermlink;
	}
	@JsonProperty("pending_payout_value")
	public String getPendingPayoutValue() {
		return pendingPayoutValue;
	}
	public String getPermlink() {
		return permlink;
	}
	@JsonProperty("root_author")
	public String getRootAuthor() {
		return rootAuthor;
	}
	@JsonProperty("root_permlink")
	public String getRootPermlink() {
		return rootPermlink;
	}
	@JsonProperty("root_title")
	public String getRootTitle() {
		return rootTitle;
	}
	public String getTitle() {
		return title;
	}
	@JsonProperty("total_payout_value")
	public String getTotalPayoutValue() {
		return totalPayoutValue;
	}
	@JsonProperty("total_pending_payout_value")
	public String getTotalPendingPayoutValue() {
		return totalPendingPayoutValue;
	}
	public String getUrl() {
		return url;
	}
	public void setActiveVotes(List<Vote> activeVotes) {
		this.activeVotes = activeVotes;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public void setChildren(long children) {
		this.children = children;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public void setCuratorPayoutValue(String curatorPayoutValue) {
		this.curatorPayoutValue = curatorPayoutValue;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
	public void setJsonMetadata(String jsonMetadata) {
		this.jsonMetadata = jsonMetadata;
	}
	public void setLastPayout(Date lastPayout) {
		this.lastPayout = lastPayout;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public void setParentAuthor(String parentAuthor) {
		this.parentAuthor = parentAuthor;
	}
	public void setParentPermlink(String parentPermlink) {
		this.parentPermlink = parentPermlink;
	}
	public void setPendingPayoutValue(String pendingPayoutValue) {
		this.pendingPayoutValue = pendingPayoutValue;
	}
	public void setPermlink(String permlink) {
		this.permlink = permlink;
	}
	public void setRootAuthor(String rootAuthor) {
		this.rootAuthor = rootAuthor;
	}
	public void setRootPermlink(String rootPermlink) {
		this.rootPermlink = rootPermlink;
	}
	public void setRootTitle(String rootTitle) {
		this.rootTitle = rootTitle;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setTotalPayoutValue(String totalPayoutValue) {
		this.totalPayoutValue = totalPayoutValue;
	}
	public void setTotalPendingPayoutValue(String totalPendingPayoutValue) {
		this.totalPendingPayoutValue = totalPendingPayoutValue;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DiscussionComment [id=");
		builder.append(id);
		builder.append(", author=");
		builder.append(author);
		builder.append(", permlink=");
		builder.append(permlink);
		builder.append(", category=");
		builder.append(category);
		builder.append(", parentAuthor=");
		builder.append(parentAuthor);
		builder.append(", parentPermlink=");
		builder.append(parentPermlink);
		builder.append(", title=");
		builder.append(title);
		builder.append(", body=");
		builder.append(body);
		builder.append(", jsonMetadata=");
		builder.append(jsonMetadata);
		builder.append(", lastUpdate=");
		builder.append(lastUpdate);
		builder.append(", created=");
		builder.append(created);
		builder.append(", lastPayout=");
		builder.append(lastPayout);
		builder.append(", children=");
		builder.append(children);
		builder.append(", totalPayoutValue=");
		builder.append(totalPayoutValue);
		builder.append(", curatorPayoutValue=");
		builder.append(curatorPayoutValue);
		builder.append(", rootAuthor=");
		builder.append(rootAuthor);
		builder.append(", rootPermlink=");
		builder.append(rootPermlink);
		builder.append(", url=");
		builder.append(url);
		builder.append(", rootTitle=");
		builder.append(rootTitle);
		builder.append(", pendingPayoutValue=");
		builder.append(pendingPayoutValue);
		builder.append(", totalPendingPayoutValue=");
		builder.append(totalPendingPayoutValue);
		builder.append(", activeVotes=");
		builder.append(activeVotes);
		builder.append("]");
		return builder.toString();
	}
	@JsonProperty("net_votes")
	public BigInteger getNetVotes() {
		return netVotes;
	}
	public void setNetVotes(BigInteger netVotes) {
		this.netVotes = netVotes;
	}
	
}
