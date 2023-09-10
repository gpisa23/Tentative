package algscom.experiment.it.logic;

import java.util.Map;

import algscom.experiment.it.model.QuotaProb;

public class CheckCombLogic {

	public static void check(String toCheck, Map<String, Double> prodotti, Double media, Double mediana, Double deviazioneStd, int size) {
		System.out.println("Check");
		Double quotaFinale = prodotti.get(toCheck);
		System.out.println("Il valore della combinazione era di: " + quotaFinale);
		for(int i=size; i>=0; i--) {
			if((media - mediana / (Math.pow(2, i))) < quotaFinale && (media + mediana / (Math.pow(2, i))) > quotaFinale) {
				System.out.println("La combinazione era nell'intervallo di ordine: " + i + "\n");
				break;
			}
			if(i==0)
				System.out.println("La combinazione non rientrava in nessun intervallo \n");
		}
	}
	
	public static void checkProb(String toCheck, Map<String, QuotaProb> prodotti, Double media, Double mediana, Double deviazioneStd, int size) {
		System.out.println("Check con Prob");
		QuotaProb quotaFinale = prodotti.get(toCheck);
		System.out.println("Il valore della combinazione era di: " + quotaFinale.getQuota());
		for(int i=size; i>=0; i--) {
			if((media - mediana / (Math.pow(2, i))) < quotaFinale.getQuota() && (media + mediana / (Math.pow(2, i))) > quotaFinale.getQuota()) {
				System.out.println("La combinazione era nell'intervallo di ordine: " + i);
				break;
			}
			if(i==0)
				System.out.println("La combinazione non rientrava in nessun intervallo");
		}
	}
}
