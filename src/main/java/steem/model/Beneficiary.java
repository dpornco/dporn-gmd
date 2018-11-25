package steem.model;

public class Beneficiary {
	public Beneficiary() {
	}
	public Beneficiary(String account, int weight) {
		this.account=account;
		this.weight=weight;
	}
	private String account;
	private int weight;
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
}
