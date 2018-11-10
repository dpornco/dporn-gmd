package steem.model;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
	private BigInteger id;
	public BigInteger getId() {
		return id;
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
	public void setId(BigInteger id) {
		this.id = id;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getPermlink() {
		return permlink;
	}
	public void setPermlink(String permlink) {
		this.permlink = permlink;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getParentAuthor() {
		return parentAuthor;
	}
	public void setParentAuthor(String parentAuthor) {
		this.parentAuthor = parentAuthor;
	}
	public String getParentPermlink() {
		return parentPermlink;
	}
	public void setParentPermlink(String parentPermlink) {
		this.parentPermlink = parentPermlink;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getJsonMetadata() {
		return jsonMetadata;
	}
	public void setJsonMetadata(String jsonMetadata) {
		this.jsonMetadata = jsonMetadata;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getLastPayout() {
		return lastPayout;
	}
	public void setLastPayout(Date lastPayout) {
		this.lastPayout = lastPayout;
	}
	public long getChildren() {
		return children;
	}
	public void setChildren(long children) {
		this.children = children;
	}
	public String getTotalPayoutValue() {
		return totalPayoutValue;
	}
	public void setTotalPayoutValue(String totalPayoutValue) {
		this.totalPayoutValue = totalPayoutValue;
	}
	public String getCuratorPayoutValue() {
		return curatorPayoutValue;
	}
	public void setCuratorPayoutValue(String curatorPayoutValue) {
		this.curatorPayoutValue = curatorPayoutValue;
	}
	public String getRootAuthor() {
		return rootAuthor;
	}
	public void setRootAuthor(String rootAuthor) {
		this.rootAuthor = rootAuthor;
	}
	public String getRootPermlink() {
		return rootPermlink;
	}
	public void setRootPermlink(String rootPermlink) {
		this.rootPermlink = rootPermlink;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getRootTitle() {
		return rootTitle;
	}
	public void setRootTitle(String rootTitle) {
		this.rootTitle = rootTitle;
	}
	public String getPendingPayoutValue() {
		return pendingPayoutValue;
	}
	public void setPendingPayoutValue(String pendingPayoutValue) {
		this.pendingPayoutValue = pendingPayoutValue;
	}
	public String getTotalPendingPayoutValue() {
		return totalPendingPayoutValue;
	}
	public void setTotalPendingPayoutValue(String totalPendingPayoutValue) {
		this.totalPendingPayoutValue = totalPendingPayoutValue;
	}
	public List<Vote> getActiveVotes() {
		return activeVotes;
	}
	public void setActiveVotes(List<Vote> activeVotes) {
		this.activeVotes = activeVotes;
	}
	private String author;
	private String permlink;
	private String category;
	private String parentAuthor;
	private String parentPermlink;
	private String title;
	private String body;
	private String jsonMetadata;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
	private Date lastUpdate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
	private Date created;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
	private Date lastPayout;
	private long children;
	private String totalPayoutValue;
	private String curatorPayoutValue;
	private String rootAuthor;
	private String rootPermlink;
	private String url;
	private String rootTitle;
	private String pendingPayoutValue;
	private String totalPendingPayoutValue;
	private List<Vote> activeVotes;
	
}
