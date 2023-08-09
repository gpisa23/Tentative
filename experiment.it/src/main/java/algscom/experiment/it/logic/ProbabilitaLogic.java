package algscom.experiment.it.logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import algscom.experiment.it.model.Match;
import algscom.experiment.it.model.MatchConProb;
import algscom.experiment.it.model.Quota;
import algscom.experiment.it.model.QuotaConProb;
import algscom.experiment.it.model.QuotaProb;
import algscom.experiment.it.util.Constants;

public class ProbabilitaLogic {

	private final static Comparator<String> customComparator = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			String[] arr1 = s1.split(" ");
			String[] arr2 = s2.split(" ");

			for (int i = 0; i < 10 && i < arr2.length; i++) {
				String val1 = arr1[i];
				String val2 = arr2[i];

				if (val1.equals("1") && val2.equals("P"))
					return -1;
				else if (val1.equals("P") && val2.equals("1"))
					return 1;
				else if (val1.equals("P") && val2.equals("2"))
					return -1;
				else if (val1.equals("2") && val2.equals("P"))
					return 1;
				else if (val1.equals("1") && val2.equals("2"))
					return -1;
				else if (val1.equals("2") && val2.equals("1"))
					return 1;
			}

			return 0;
		}
	};

	static List<MatchConProb> matchesConProb = new ArrayList<>();
	static Consumer<Match> CONVERTI_IN_PROBABILITA = m -> {
		MatchConProb matchConProb = new MatchConProb();
		matchConProb.setEvento(m.getEvento());
		List<Quota> quote = m.getQuote();
		List<QuotaConProb> quoteConProb = new ArrayList<>();
		Double constConv = (quote.get(0).getQuota() * quote.get(1).getQuota() * quote.get(2).getQuota())
				/ (quote.get(0).getQuota() * quote.get(1).getQuota() + quote.get(1).getQuota() * quote.get(2).getQuota()
						+ quote.get(0).getQuota() * quote.get(2).getQuota());
		m.getQuote().stream().forEach(q -> {
			QuotaConProb quotaProb = new QuotaConProb();
			quotaProb.setEsito(q.getEsito());
			quotaProb.setProb((1 / q.getQuota()) * constConv);
			quotaProb.setQuota(q.getQuota());
			quoteConProb.add(quotaProb);
		});
		matchConProb.setQuoteConProb(quoteConProb);
		matchesConProb.add(matchConProb);
	};

	public static void conProbabilita(List<Match> matches, Double costNorm) {
		System.out.println("\nAltro metodo\n");
		matches.stream().forEach(CONVERTI_IN_PROBABILITA);

		Map<String, QuotaProb> prodotti = new HashMap<>();
		prodotti.put(matches.get(0).getQuote().get(0).getEsito(),
				new QuotaProb(matchesConProb.get(0).getQuoteConProb().get(0).getQuota(),
						matchesConProb.get(0).getQuoteConProb().get(0).getProb()));
		prodotti.put(matches.get(0).getQuote().get(1).getEsito(),
				new QuotaProb(matchesConProb.get(0).getQuoteConProb().get(1).getQuota(),
						matchesConProb.get(0).getQuoteConProb().get(1).getProb()));
		prodotti.put(matches.get(0).getQuote().get(2).getEsito(),
				new QuotaProb(matchesConProb.get(0).getQuoteConProb().get(2).getQuota(),
						matchesConProb.get(0).getQuoteConProb().get(2).getProb()));
		Set<String> keySet = new HashSet<String>();
		String ordine = matchesConProb.get(0).getEvento();
		for (int i = 1; i < matchesConProb.size(); i++) {
			keySet.addAll(prodotti.keySet());
			for (String s : keySet) {
				double q = prodotti.get(s).getQuota();
				double p = prodotti.get(s).getProb();
				for (QuotaConProb quota : matchesConProb.get(i).getQuoteConProb()) {
					prodotti.put(s.concat(" " + quota.getEsito()),
							new QuotaProb(q * quota.getQuota(), p * quota.getProb()));
				}
				prodotti.remove(s);
			}
			keySet.clear();
			ordine = ordine.concat(" ").concat(matches.get(i).getEvento());
		}
		prodotti.entrySet().stream().forEach(entry -> entry
				.setValue(new QuotaProb(entry.getValue().getQuota() / costNorm, entry.getValue().getProb())));
		// calcoliamo i parametri media, devStd
		QuotaProb[] quotaProbArray = new QuotaProb[prodotti.size()];
		double[] quotaProdotto = new double[prodotti.size()];
		double[] probProdotto = new double[prodotti.size()];
		prodotti.values().toArray(quotaProbArray);
		for (int i = 0; i < quotaProbArray.length; i++) {
			quotaProdotto[i] = quotaProbArray[i].getQuota();
			probProdotto[i] = quotaProbArray[i].getProb();
		}
		double sumWeighted = 0.0;
		double sumSquaredWeightedDiff = 0.0;
		double totalWeight = 0.0;

		for (int i = 0; i < quotaProbArray.length; i++) {
			double value = quotaProdotto[i];
			double probability = probProdotto[i];

			sumWeighted += value * probability;
			sumSquaredWeightedDiff += probability * Math.pow(value, 2);
			totalWeight += probability;
		}

		double mean = sumWeighted / totalWeight;
		double variance = (sumSquaredWeightedDiff / totalWeight) - Math.pow(mean, 2);
		double standardDeviation = Math.sqrt(variance);

		System.out.println("Media: " + mean);
		System.out.println("Deviazione standard: " + standardDeviation);
		Map<Double, Double> mappaAusiliaria = new HashMap<>();

		double cumulativeProbability = 0.0;
		for (int i = 0; i < quotaProbArray.length; i++) {
			mappaAusiliaria.put(quotaProdotto[i], probProdotto[i]);
		}
		TreeMap<Double, Double> sortedData = new TreeMap<>(mappaAusiliaria);
		double t = 1.0;
		for (Entry<Double, Double> entry : sortedData.entrySet()) {
			cumulativeProbability += entry.getValue();
			if (cumulativeProbability >= 0.5) {
				System.out.println("Mediana: " + entry.getKey());
				t = entry.getKey();
				break;
			}
		}
		double mediana = t;
		System.out.println("Le combinazioni venute fuori sono:\n");
		System.out.println(ordine);
		Set<Entry<String, QuotaProb>> combinazioni = new HashSet<>();
		prodotti.entrySet().forEach(entry -> {
			if (entry.getValue().getQuota() > (mean - mediana / (Math.pow(2, matches.size())))
					&& entry.getValue().getQuota() < (mean + mediana / (Math.pow(2, matches.size())))) {
				combinazioni.add(entry);
			}
		});

		System.out.println(combinazioni.size());
		String[] combinazioniOrdinate = new String[combinazioni.size()];
		Iterator<Entry<String, QuotaProb>> iter = combinazioni.iterator();
		for (int i = 0; i < combinazioni.size(); i++) {
			Entry<String, QuotaProb> entry = iter.next();
			combinazioniOrdinate[i] = entry.getKey().concat(" con probabilita: ")
					.concat(String.valueOf(entry.getValue().getProb()));
		}
		Arrays.sort(combinazioniOrdinate, customComparator);
		for (String s : combinazioniOrdinate) {
			System.out.println(s);
		}
		if (Constants.PRINT) {
			// salva in file
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.PATH_TO_WRITE_INTO, true))) {
				writer.write("--- Con altro metodo ---");
				writer.newLine();
				writer.newLine();
				writer.write(ordine);
				writer.newLine();
				// Scrittura delle stringhe nel file, una per riga
				for (String stringa : combinazioniOrdinate) {
					writer.write(stringa);
					writer.newLine(); // Vai alla riga successiva
				}
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
