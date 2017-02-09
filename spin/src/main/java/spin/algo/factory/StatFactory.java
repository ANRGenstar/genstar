package spin.algo.factory;

import java.util.Hashtable;
import java.util.Map;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import spin.interfaces.EGraphStreamNetworkType;

/** Factory de stat, donne les infos sur les graphes
 * 
 */
public class StatFactory {

	// Map de networkType <-> graph, plusieurs graphes sont possibles, ceux lu pour avoir les données, ceux en cours, etc. 
	// TODO besoin éventuelle d'une synchronisation?
	Map<EGraphStreamNetworkType, Graph> graphs;
	
	// Singleton
	private static StatFactory INSTANCE;
	
	public static StatFactory getIntance(){
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
	
	// --------------------------------
	// --- PARTIE OBTENIR LES INFOS ---
	// --------------------------------
	
	/**
	 * 
	 * @param whichOne
	 * @return
	 */
	public double getAverageClustering(EGraphStreamNetworkType whichOne){
		return 0;
	}
	
	/**
	 * 
	 * @param whichOne
	 * @return
	 */
	public double getAPL(EGraphStreamNetworkType whichOne){
//		Graph graph = graphs.get(EGraphStreamNetworkType.fileRead);
//		for (Node myNode : graph.getNodeSet()) {
//			System.out.println(myNode.getId());
//		}
//		
//		
//		return 0;
		
		Graph graph = graphs.get(EGraphStreamNetworkType.fileRead);
		APSP apsp = new APSP();
		apsp.init(graph);
		apsp.setDirected(false);
		apsp.compute();
		APSPInfo info;
		double total = 0;
		int nbValue = 0;
		for (int i = 0; i < graph.getNodeCount(); i++) {
			info =  graph.getNode("n"+i).getAttribute(APSPInfo.ATTRIBUTE_NAME);
			for (String string : info.targets.keySet()) {
				total += info.targets.get(string).distance;
				nbValue++;
			}
		}
		
		System.out.println(total / nbValue);
		return total / nbValue;
	}
	
	
	
}
