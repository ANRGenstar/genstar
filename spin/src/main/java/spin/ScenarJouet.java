package spin;

import spin.algo.factory.GraphStreamFactory;
import spin.algo.factory.StatFactory;
import spin.interfaces.EGraphStreamNetworkType;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

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
		stat.getAPL(EGraphStreamNetworkType.fileRead);
		
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
		
		factory.getGraphStreamGraph(spinNetwork);
		// II - D Stat
		stat.getAPL(EGraphStreamNetworkType.spinNetwork);
		
	}
}
