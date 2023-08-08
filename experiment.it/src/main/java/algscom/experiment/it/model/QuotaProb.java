package algscom.experiment.it.model;

public class QuotaProb {

	public double quota;
	public double prob;
	
	public QuotaProb() {
		super();
	}
	
	public QuotaProb(double quota, double prob) {
		this.quota = quota;
		this.prob = prob;
	}

	public double getQuota() {
		return quota;
	}

	public void setQuota(double quota) {
		this.quota = quota;
	}

	public double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}
	
	
}
