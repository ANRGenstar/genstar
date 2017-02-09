package spin.algo.factory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.graph.Graph;

import core.metamodel.pop.APopulationEntity;
import spin.interfaces.EGraphStreamNetworkType;
import spin.interfaces.ISpinNetProperties;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

/** Factory de stat, donne les infos sur les graphes
 * 
 */
public class StatFactory implements ISpinNetProperties{

	// Map de networkType <-> graph, plusieurs graphes sont possibles, ceux lu pour avoir les données, ceux en cours, etc. 
	// TODO besoin éventuelle d'une synchronisation?
	Map<EGraphStreamNetworkType, Graph> graphs;
	
	// Singleton
	private static StatFactory INSTANCE;
	
	public static StatFactory getInstance(){
		if(INSTANCE == null)
			INSTANCE = new StatFactory();
		return INSTANCE;
	}
	
	private StatFactory(){
		graphs = new Hashtable<EGraphStreamNetworkType, Graph>();
	}
	
	/** A appeler dans le constrcuteur du GSFactory pour faire un lien direct
	 * entre les deux listes. 
	 * 
	 * @param GSFactoryList
	 */
	public void setRefToGraphList(Map<EGraphStreamNetworkType, Graph> GSFactoryList){
		this.graphs = GSFactoryList;
	}
	
	// -----------------------------------------
	// --- PARTIE OBTENIR LES INFOS GLOBALES ---
	// -----------------------------------------
	
	/**
	 * 
	 * @param whichOne
	 * @return
	 */
	private double getAverageClustering(EGraphStreamNetworkType whichOne){
		return 0;
	}
	
	/**
	 * 
	 * @param whichOne
	 * @return
	 */
	public double getAPL(EGraphStreamNetworkType whichOne){
		Graph graph = graphs.get(whichOne);
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
		
		System.out.println(total / nbValue);
		return total / nbValue;
	}

	
	// -----------------------------------------
	// --- PARTIE OBTENIR LES INFOS GLOBALES ---
	// -----------------------------------------
	
	private double getLocalClustering(NetworkNode node){
		
		
		return 0;
		
	}
	
	
	
	
	// -------------------------------------------
	// --- PARTIE FONCTION LIEES A L'INTERFACE ---
	// -------------------------------------------
	
	@Override
	public double getAPL() {
		return getAPL(EGraphStreamNetworkType.spinNetwork);
	}

	@Override
	public double getClustering(APopulationEntity entite) {
		SpinNetwork network = NetworkFactory.getIntance().getSpinNetwork();
		NetworkNode node = network.kvEntityNodeFastList.get(entite);
		return getLocalClustering(node);
	}
	
	public Set<APopulationEntity> getNeighboor(APopulationEntity entite){
		Set<APopulationEntity> entities = new HashSet<APopulationEntity>();
		SpinNetwork network = NetworkFactory.getIntance().getSpinNetwork();
		NetworkNode node = network.kvEntityNodeFastList.get(entite);
		
		for (NetworkNode nodeNeigh : node.getNeighbours()) {
			entities.add(network.kvNodeEntityFastList.get(nodeNeigh));
		}
		
		return entities;
	}
	
	
	
}
