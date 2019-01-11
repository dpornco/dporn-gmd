package steem.model;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrendingTag {
	private BigInteger comments;
	@JsonProperty("total_payouts")
	private String totalPayouts;
	@JsonProperty("top_posts")
	private BigInteger topPosts;
	private String name;
	@JsonProperty("net_votes")
	
	@Deprecated
	private BigInteger netVotes;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrendingTag [comments=");
		builder.append(comments);
		builder.append(", name=");
		builder.append(name);
		builder.append(", netVotes=");
		builder.append(netVotes);
		builder.append(", topPosts=");
		builder.append(topPosts);
		builder.append(", totalPayouts=");
		builder.append(totalPayouts);
		builder.append(", trending=");
		builder.append(trending);
		builder.append("]");
		return builder.toString();
	}

	private BigInteger trending;

	public BigInteger getComments() {
		return comments;
	}

	public String getName() {
		return name;
	}

	@Deprecated
	@JsonProperty("net_votes")
	public BigInteger getNetVotes() {
		return netVotes;
	}

	@JsonProperty("top_posts")
	public BigInteger getTopPosts() {
		return topPosts;
	}

	@JsonProperty("total_payouts")
	public String getTotalPayouts() {
		return totalPayouts;
	}

	public BigInteger getTrending() {
		return trending;
	}

	public void setComments(BigInteger comments) {
		this.comments = comments;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Deprecated
	@JsonProperty("net_votes")
	public void setNetVotes(BigInteger netVotes) {
		this.netVotes = netVotes;
	}

	@JsonProperty("top_posts")
	public void setTopPosts(BigInteger topPosts) {
		this.topPosts = topPosts;
	}

	@JsonProperty("total_payouts")
	public void setTotalPayouts(String totalPayouts) {
		this.totalPayouts = totalPayouts;
	}

	public void setTrending(BigInteger trending) {
		this.trending = trending;
	}
}
