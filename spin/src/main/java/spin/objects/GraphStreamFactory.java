package spin.objects;

import java.util.Map;

import org.graphstream.graph.Graph;

public class GraphStreamFactory {

	// Map de string graph, plusieurs graphes sont possibles, ceux lu pour avoir les données, ceux en cours, etc. 
	Map<String, Graph> graphes ;
	Graph graph;
	
	
	private static GraphStreamFactory INSTANCE;
	// Singleton
	
	public static GraphStreamFactory getIntance(){
		if(INSTANCE == null)
			INSTANCE = new GraphStreamFactory();
		return INSTANCE;
	}
	
	private GraphStreamFactory(){		
	}
	
	/** TODO Parcours des éléments du spinGraph pour en faire un graphStream
	 * 
	 * @param spinNetwork
	 * @return
	 */
	public Graph getGraphStreamGraph(SpinNetwork spinNetwork){
	 return null;	// ou bien this.graph = graph?
	}
	
	/** lit un fichier texte et le converti en graph stream
	 * 
	 * @return
	 */
	public Graph readFile(){
		return null;
	}
	
	/** Export un graph 
	 * 
	 * @return
	 */
	public void exportFile(){
		
	}
	
	/** Donne un ensemble de stat sur le graph. Créer une classe de stat pour ce faire? 
	 *  
	 */
	public void ensembleDesFonctionsDeStat(){
		
	}
	
}
