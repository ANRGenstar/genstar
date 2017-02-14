package spin.algo.factory;

import java.util.HashSet;
import java.util.Set;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.graph.Graph;

import core.metamodel.pop.APopulationEntity;
import spin.interfaces.EGraphStreamNetwork;
import spin.interfaces.INetProperties;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

/** Factory de stat, donne les infos sur les graphes
 * implémente @ISpinNetProperties, interface qui sera disponible dans SpinPopulation
 * Ne crée pas de GraphStream tant qu'une opération nécessitant l'usage de ce dernier n'est 
 * pas demandé. 
 */
public class StatFactory implements INetProperties{
	
	// Singleton
	private static StatFactory INSTANCE;
	
	public static StatFactory getInstance(){
		if(INSTANCE == null)
			INSTANCE = new StatFactory();
		return INSTANCE;
	}
	
	private StatFactory(){
	}
	


	// -----------------------------------------
	// --- PARTIE OBTENIR LES INFOS GLOBALES ---
	// -----------------------------------------
	
	/** Avoir la densité du réseau spin courant. 
	 * 
	 * @return
	 */
	private double getDensitySpin(){
		SpinNetwork network = SpinNetworkFactory.getInstance().getSpinNetwork();
		double nbNodes = network.getNodes().size();
		double nbLinks = network.getLinks().size();
		return nbLinks / (nbNodes * (nbNodes-1));
	}
	
	/** Obtenir le clustering moyen sur l'ensemble du réseau
	 * 
	 * @param whichOne
	 * @return
	 */
	private double getAverageClustering(EGraphStreamNetwork whichOne){
		return 0;
	}
	
	/** Obtenir l'APL moyen sur un échantillon de chemin entre les noeuds.
	 * Utilise la représentation GraphStream. 
	 *  TODO Passer à un échantillon de chemin et non l'ensemble des chemins. 
	 * @param Précise le graphe sur lequel faire le calcul
	 * @return
	 */
	public double getAPL(EGraphStreamNetwork whichOne){
		GraphStreamFactory.getIntance().initialiseGraphStreamFromSpin();
		Graph graph = GraphStreamFactory.getIntance().getGraphStreamGraph(whichOne);
		APSP apsp = new APSP();
		apsp.init(graph);
		apsp.setDirected(false);
		apsp.compute();
		APSPInfo info;
		double total = 0;
		int nbValue = 0;
		for (int i = 0; i < graph.getNodeCount(); i++) {
			info = graph.getNode(i).getAttribute(APSPInfo.ATTRIBUTE_NAME);
			for (String string : info.targets.keySet()) {
				total += info.targets.get(string).distance;
				nbValue++;
			}
		}
		
		return total / nbValue;
	}

	
	// -----------------------------------------
	// --- PARTIE OBTENIR LES INFOS LOCALES  ---
	// -----------------------------------------
	
	private double getLocalClustering(NetworkNode node){
		SpinNetwork network = SpinNetworkFactory.getInstance().getSpinNetwork();
		Set<NetworkNode> voisins = new HashSet<NetworkNode>();
		
		try{
			
			
		}catch(NullPointerException e){
			System.err.println("SpinNetwork not yet initialized");
		}
		
		return 0;
	}
	
	// -------------------------------------------
	// --- PARTIE FONCTION LIEES A L'INTERFACE ---
	// -------------------------------------------
	
	@Override
	public double getAPL() {
		
		return getAPL(EGraphStreamNetwork.spinNetwork);
	}

	@Override
	public double getClustering(APopulationEntity entite) {
		SpinNetwork network = SpinNetworkFactory.getInstance().getSpinNetwork();
		NetworkNode node = network.kvEntityNodeFastList.get(entite);
		return getLocalClustering(node);
	}
	
	public Set<APopulationEntity> getNeighboor(APopulationEntity entite){
		Set<APopulationEntity> entities = new HashSet<APopulationEntity>();
		SpinNetwork network = SpinNetworkFactory.getInstance().getSpinNetwork();
		NetworkNode node = network.kvEntityNodeFastList.get(entite);
		
		for (NetworkNode nodeNeigh : node.getNeighbours()) {
			entities.add(network.kvNodeEntityFastList.get(nodeNeigh));
		}
		
		return entities;
	}
	
	public double getDensity(){
		return getDensitySpin();
	}
	
}
