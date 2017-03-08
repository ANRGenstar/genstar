package spin;

import spin.objects.SpinNetwork;
import useless.EGraphStreamNetwork;
import useless.GraphStreamFactory;
import useless.NetworkLink;
import useless.NetworkNode;
import useless.StatFactory;

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
		StatFactory stat = StatFactory.getInstance();
		
		
		// I - A & I - B & I - C
		factory.readFile("/Users/felix/Desktop/simple.graphml.xml");
		// I - D
		stat.getAPL(EGraphStreamNetwork.fileRead);
		
		// II - C 
		SpinNetwork spinNetwork = new SpinNetwork();
		NetworkNode n1 = new NetworkNode(null,"1");
		NetworkNode n2 = new NetworkNode(null,"2");
		NetworkNode n3 = new NetworkNode(null,"3");
		spinNetwork.putNode(n1);
		spinNetwork.putNode(n2);
		spinNetwork.putNode(n3);
		spinNetwork.putLink(new NetworkLink(n1,n2,"1"));
		spinNetwork.putLink(new NetworkLink(n1,n3,"2"));
		
		factory.generateGraphStreamGraph(spinNetwork);
		// II - D Stat
		stat.getAPL(EGraphStreamNetwork.spinNetwork);
		
	}
}
