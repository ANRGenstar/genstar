package spin;

import spin.algo.factory.GraphStreamFactory;

public class ScenarJouet {
	
	/**
	 * I - A Ouverture d'un fichier texte
	 * I - B Lecture du graphe 
	 * I - C Transformation en graphStream, 
	 * I - D Stat sur ce graph
	 * 
	 * II - A Creation d'un population, 
	 * II - B Création d'un spinnetwork dessus,
	 * II - C spinNetwork en graphStream, 
	 * II - D Stat. 
	 * II - E SpinNetwork//Graph écrit dans un fichier de texte
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		GraphStreamFactory factory = GraphStreamFactory.getIntance();
		
		// I - A & I - B & I - C
		factory.readFile("/Users/csg/Desktop/simple.graphml.xml");
		
		
	}
}
