package spin;

import spin.interfaces.ENetworkFormat;
import useless.EGraphStreamNetwork;
import useless.GraphStreamFactory;


/** Classe de main pour la génération de réseau sur une population
 *
 */
public class NetworkGeneration {

	/** Main.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int nbNode = 100;
		
		// 1 instancier la factory
		// 2 prendre une population en param
		//ISyntheticGosplPopGenerator populationGenerator = new UtilGenerator(4,2);
		//IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population = populationGenerator.generate(nbNode);

		// 3 la factory choisit un générator grace a une unum
		// 4 le générator renvoi le réseau obtenu sur la population
		//@SuppressWarnings("unused")
		//SpinNetwork aNetwork = NetworkFactory.getNetwork(ENetworkEnumGenerator.SmallWorld, population);
		
		GraphStreamFactory factory = GraphStreamFactory.getIntance();
		factory.readFile("/Users/csg/Desktop/simple.graphml.xml"); 
		
		/*SpinNetwork spinNetwork = new SpinNetwork();
		NetworkNode n1 = new NetworkNode(null,"1");
		NetworkNode n2 = new NetworkNode(null,"2");
		NetworkNode n3 = new NetworkNode(null,"3");
		spinNetwork.putNode(n1);
		spinNetwork.putNode(n2);
		spinNetwork.putNode(n3);
		spinNetwork.putLink(new NetworkLink(n1,n2,"1"));
		spinNetwork.putLink(new NetworkLink(n1,n3,"2"));
		factory.getGraphStreamGraph(spinNetwork);
		*/
		
		
		factory.exportFile(EGraphStreamNetwork.fileRead, ENetworkFormat.GML, "/Users/csg/Desktop/simple.gml");;
		
 		
	}
}
