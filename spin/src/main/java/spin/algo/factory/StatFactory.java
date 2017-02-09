package spin.algo.factory;

import java.util.Hashtable;
import java.util.Map;

import org.graphstream.graph.Graph;

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
		return 0;
	}
	
	
	
}
