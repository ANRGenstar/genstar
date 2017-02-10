package spin.algo.factory;

import java.util.HashSet;
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
 * implémente @ISpinNetProperties, interface qui sera disponible dans SpinPopulation
 */
public class StatFactory implements ISpinNetProperties{
	
	// Singleton
	private static StatFactory INSTANCE;
	
	public static StatFactory getInstance(){
		if(INSTANCE == null)
			INSTANCE = new StatFactory();
		return INSTANCE;
	}
	
	private StatFactory(){
	}
	
	/** Pour les calculs qui nécessite la création d'un graphstream.
	 * 
	 */
	private void initialiseGraphStreamFromSpin(){
		GraphStreamFactory gsf = GraphStreamFactory.getIntance();
		if(!gsf.containsGraphType(EGraphStreamNetworkType.spinNetwork))
			gsf.generateGraphStreamGraph(SpinNetworkFactory.getInstance().getSpinNetwork());
			
	}

	// -----------------------------------------
	// --- PARTIE OBTENIR LES INFOS GLOBALES ---
	// -----------------------------------------
	
	private double getDensitySpin(){
		SpinNetwork network = SpinNetworkFactory.getInstance().getSpinNetwork();
		double nbNodes = network.getNodes().size();
		double nbLinks = network.getLinks().size();
		return nbLinks / (nbNodes * (nbNodes-1));
	}
	
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
		initialiseGraphStreamFromSpin();
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
		
		System.out.println(total / nbValue);
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
		
		return getAPL(EGraphStreamNetworkType.spinNetwork);
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
