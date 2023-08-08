package algscom.experiment.it.model;

import java.util.List;

public class MatchConProb extends Match {

	private List<QuotaConProb> quoteConProb;
	
	public MatchConProb() {
		super();
	}

	public List<QuotaConProb> getQuoteConProb() {
		return quoteConProb;
	}

	public void setQuoteConProb(List<QuotaConProb> quoteConProb) {
		this.quoteConProb = quoteConProb;
	}
}
