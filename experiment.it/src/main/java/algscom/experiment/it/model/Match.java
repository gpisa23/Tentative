package algscom.experiment.it.model;

import java.util.List;

public class Match {
	
	private String evento;
	private List<Quota> quote;
	
	public Match() {
		super();
	}

	public String getEvento() {
		return evento;
	}

	public void setEvento(String evento) {
		this.evento = evento;
	}

	public List<Quota> getQuote() {
		return quote;
	}

	public void setQuote(List<Quota> quote) {
		this.quote = quote;
	}

}
