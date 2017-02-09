package spin.algo.factory;

import java.util.Hashtable;
import java.util.Map;

import org.graphstream.graph.Graph;

import spin.interfaces.EGraphStreamNetworkType;
import spin.objects.SpinNetwork;

public class GraphStreamFactory {

	// Map de networkType <-> graph, plusieurs graphes sont possibles, ceux lu pour avoir les données, ceux en cours, etc. 
	Map<EGraphStreamNetworkType, Graph> graphs;
	
	// Singleton
	private static GraphStreamFactory INSTANCE;
	
	public static GraphStreamFactory getIntance(){
		if(INSTANCE == null)
			INSTANCE = new GraphStreamFactory();
		return INSTANCE;
	}
	
	private GraphStreamFactory(){
		graphs = new Hashtable<EGraphStreamNetworkType, Graph>();
	}
	
	/** TODO Parcours des éléments du spinGraph pour en faire un graphStream.
	 * Ajouté dans la liste des graphs,  associé a l'enum spinNetwork
	 * 
	 * @param spinNetwork a convertir en graphSteam
	 */
	public void getGraphStreamGraph(SpinNetwork spinNetwork){
		// blablalba foreach node foreach link 
	}
	
	/** TODO lit un fichier texte et le converti en graph stream
	 * Ajouté dans la liste des graphs,  associé a l'enum fileRead
	 * @return
	 */
	public void readFile(){
		// TODO modifier les params
	}
	
	/** Export un graph dans un fichier extérieur
	 * 
	 * @return
	 */
	public void exportFile(EGraphStreamNetworkType whichOne){
		
	}
	
	/** Donne un ensemble de stat sur le graph. Créer une classe de stat pour ce faire? 
	 *  TODO renvoyer un objet de properties?
	 */
	public void ensembleDesFonctionsDeStat(EGraphStreamNetworkType whichOne){
		
	}
	
	/** Libère une référence a un graph dans la list qu'il puisse etre garbagé?
	 * 
	 * @param graph
	 */
	public void forgetGraph(EGraphStreamNetworkType whichOne){
		graphs.put(whichOne, null);
	}
	
}
