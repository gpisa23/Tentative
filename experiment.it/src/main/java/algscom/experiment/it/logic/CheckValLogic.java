package algscom.experiment.it.logic;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckValLogic {
	
	public static void check(Double val, Map<String, Double> prodotti, Double min) {
		Set<Entry<String, Double>> combVal = prodotti.entrySet().stream().filter(e -> e.getValue() > (val - min/10) && 
				e.getValue() < (val + min/10)).collect(Collectors.toSet());
		if(combVal.isEmpty()) {
			System.out.println("Nessuna combinazione vicina al valore indicato \n");
			return;
		}
		System.out.println("Combinazioni trovate prossime al valore indicato sono: " + combVal.size() + "\n");
		combVal.stream().forEach(e -> System.out.println(e.getKey()));
	}
}
