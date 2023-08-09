package algscom.experiment.it;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import algscom.experiment.it.logic.ProbabilitaLogic;
import algscom.experiment.it.model.Match;
import algscom.experiment.it.model.Quota;
import algscom.experiment.it.util.Constants;

public class App {

	private final static ObjectMapper om = new ObjectMapper();

	private final static Comparator<String> customComparator = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			String[] arr1 = s1.split(" ");
			String[] arr2 = s2.split(" ");

			for (int i = 0; i < arr1.length && i < arr2.length; i++) {
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

	public static void main(String[] args) throws StreamReadException, DatabindException, IOException {
		System.out.println("W I L L K O M M E N !\n");
		// Inizio recuper match
		System.out.println("Recupero matches... \n");
		List<Match> matches = om.readValue(new File(Constants.PATH_TO_READ_FROM), new TypeReference<List<Match>>() {
		});
		// Stampa alcuni valori di utilità
		// match recuperati
		System.out.println("Match recuperati: " + matches.size());
		Double totaleComb = Math.pow(3, matches.size());
		System.out.println("Totale combinazioni: " + totaleComb);
		// combinazione massima
		Double max = 1.0;
		for (Match m : matches) {
			max = max * calcolaMax(m);
		}
		System.out.println("Valore combinazione massima: " + max);
		// combinazione minima
		Double min = 1.0;
		for (Match m : matches) {
			min = min * calcolaMin(m);
		}
		System.out.println("Valore combinazione minima: " + min + "\n");

		System.out.println("Inizio a kind of magic...");
		// metto tutte le possibili combinazioni in una mappa
		Map<String, Double> prodotti = new HashMap<>();
		prodotti.put(matches.get(0).getQuote().get(0).getEsito(), matches.get(0).getQuote().get(0).getQuota());
		prodotti.put(matches.get(0).getQuote().get(1).getEsito(), matches.get(0).getQuote().get(1).getQuota());
		prodotti.put(matches.get(0).getQuote().get(2).getEsito(), matches.get(0).getQuote().get(2).getQuota());
		Set<String> keySet = new HashSet<String>();
		String ordine = matches.get(0).getEvento();
		for (int i = 1; i < matches.size(); i++) {
			keySet.addAll(prodotti.keySet());
			for (String s : keySet) {
				Double val = prodotti.get(s);
				for (Quota quota : matches.get(i).getQuote()) {
					prodotti.put(s.concat(" " + quota.getEsito()), val * quota.getQuota());
				}
				prodotti.remove(s);
			}
			keySet.clear();
			ordine = ordine.concat(" ").concat(matches.get(i).getEvento());
		}
		System.out.println("Numero prodotti: " + prodotti.size());
		// Normalizzo la lista
		Double costNorm = max / prodotti.size();
		System.out.println("La costante di normalizzazione e\': " + costNorm);
		prodotti.entrySet().stream().forEach(entry -> entry.setValue(entry.getValue() / costNorm));
		// Calcolo media campionaria
		Double sum = 0.0;
		for (Double val : prodotti.values()) {
			sum += val;
		}
		Double media = sum / prodotti.size();
		System.out.println("La media campionaria e\': " + media);
		// Calcolo la deviazione standard campionaria
		Double sommaScartiQuad = 0.0;
		for (Double val : prodotti.values()) {
			Double scarto = val - media;
			sommaScartiQuad += scarto * scarto;
		}
		Double deviazioneStandard = Math.sqrt(sommaScartiQuad / (prodotti.size() - 1));
		System.out.println("La deviazione standard e\': " + deviazioneStandard + "\n");
		// Calcolo mediana
		Double[] datiDouble = new Double[prodotti.values().toArray().length];
		prodotti.values().toArray(datiDouble);
		Arrays.sort(datiDouble);
		Double mediana;
		int dim = datiDouble.length;
		if (dim % 2 == 0) {
			mediana = (datiDouble[(dim / 2) - 1] + datiDouble[dim / 2]) / 2;
		} else {
			int index = (dim + 1) / 2;
			mediana = datiDouble[index];
		}
		System.out.println("Mediana: " + mediana);
		// Calcolo e stampo le combinazioni
		System.out.println("Le combinazioni venute fuori sono:\n");
		System.out.println(ordine);
		Set<Entry<String, Double>> combinazioni = new HashSet<>();
		prodotti.entrySet().forEach(entry -> {
			if (entry.getValue() > (media - mediana / (Math.pow(2, matches.size())))
					&& entry.getValue() < (media + mediana / (Math.pow(2, matches.size())))) {
				combinazioni.add(entry);
			}
		});

		System.out.println(combinazioni.size());
		String[] combinazioniOrdinate = new String[combinazioni.size()];
		Iterator<Entry<String, Double>> iter = combinazioni.iterator();
		for (int i = 0; i < combinazioni.size(); i++) {
			combinazioniOrdinate[i] = iter.next().getKey();
		}
		Arrays.sort(combinazioniOrdinate, customComparator);
		for (String s : combinazioniOrdinate) {
			System.out.println(s);
		}
		// salvo in file con timestamp
		if (Constants.PRINT) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeStampString = dateFormat.format(new Date());
			System.out.println("TimeStamp corrente: " + timeStampString);

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.PATH_TO_WRITE_INTO, true))) {
				writer.write(timeStampString);
				writer.newLine();
				writer.write("Broker: " + Constants.BROKER);
				writer.newLine();
				writer.write(ordine);
				writer.newLine();
				// Scrittura delle stringhe nel file, una per riga
				for (String stringa : combinazioniOrdinate) {
					writer.write(stringa);
					writer.newLine(); // Vai alla riga successiva
				}
				writer.newLine();
				System.out.println("Le stringhe sono state salvate nel file: " + Constants.PATH_TO_WRITE_INTO);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// uso Apache Common Math
		System.out.println("\nE X T R A \n");

		double[] dati = new double[datiDouble.length];
		for (int i = 0; i < datiDouble.length; i++) {
			dati[i] = datiDouble[i];
		}
		DescriptiveStatistics stats = new DescriptiveStatistics(dati);
		double mediaA = stats.getMean();
		double deviazioneStandardA = stats.getStandardDeviation();
		double medianaA = StatUtils.percentile(dati, 50);

		System.out.println("Media: " + mediaA);
		System.out.println("Deviazione Standard: " + deviazioneStandardA);
		System.out.println("Mediana: " + medianaA);

		KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();

		double pValue = test.kolmogorovSmirnovStatistic(new NormalDistribution(), dati);
		System.out.println("p-value: " + pValue);
		if (pValue < 0.05) {
			System.out.println("I dati non seguono una distribuzione normale");
		} else {
			System.out.println("I dati seguono una distribuzione normale");
		}
		ProbabilitaLogic.conProbabilita(matches, costNorm);
	}

	private static List<Double> prendiListaQuote(Match m) {
		return Arrays.asList(m.getQuote().get(0).getQuota(), m.getQuote().get(1).getQuota(),
				m.getQuote().get(2).getQuota());
	}

	private static Double calcolaMax(Match m) {
		return Collections.max(prendiListaQuote(m));
	}

	private static Double calcolaMin(Match m) {
		return Collections.min(prendiListaQuote(m));
	}
}
