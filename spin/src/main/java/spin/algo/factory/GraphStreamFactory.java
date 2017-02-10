package spin.algo.factory;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkGML;
import org.graphstream.stream.file.FileSinkGraphML;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import spin.interfaces.EGraphStreamNetworkType;
import spin.interfaces.ENetworkFormat;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

/** Factory de graph stream, gère le lien entre représensation SpinNetwork et GraphStream,
 * lecture et ecriture de fichier
 * 
 */
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
	
	/** 
	 * 
	 * @param whichOne
	 * @return
	 */
	public Graph getGraphStreamGraph(EGraphStreamNetworkType whichOne){
		Graph toReturn = graphs.get(whichOne);
//		if(toReturn == null && whichOne == EGraph)
		return graphs.get(whichOne);
	}
	
	/** Parcours des éléments du spinGraph pour en faire un graphStream.
	 * Ajouté dans la liste des graphs,  associé a l'enum spinNetwork
	 * 
	 * @param spinNetwork a convertir en graphSteam
	 */
	public void generateGraphStreamGraph(SpinNetwork spinNetwork){
		System.out.println("Generation d'un graphStream depuis un spin");
		
		Graph g = new DefaultGraph("g");
		for (NetworkNode node : spinNetwork.getNodes()) {
			g.addNode(node.getId());
		}

		for (NetworkLink link : spinNetwork.getLinks()) {
			g.addEdge("e" + link.getFrom().getId() +"->" +link.getTo().getId(), link.getFrom().getId(), link.getTo().getId());
		}
		
		graphs.put(EGraphStreamNetworkType.spinNetwork, g);
//		g.display();
	}
	
	/** Lit un fichier texte et le converti en graph stream
	 * Ajouté dans la liste des graphs,  associé a l'enum fileRead
	 * @return
	 */
	public void readFile(String path){
		Graph g = new DefaultGraph("g");
		FileSource fs;
		try {
			fs = FileSourceFactory.sourceFor(path);
			fs.addSink(g);
			fs.readAll(path);
			fs.removeSink(g);
			
			graphs.put(EGraphStreamNetworkType.fileRead, g);
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/** Export un graph dans un fichier extérieur
	 * 
	 * @return
	 */
	public void exportFile(EGraphStreamNetworkType whichOne, ENetworkFormat format, String path){
		Graph g = graphs.get(whichOne);
		FileSink filesink = null;
		switch (format) {
		case GraphML:
			filesink = new FileSinkGraphML();
			break;

		case GML:
			filesink = new FileSinkGML();
			break;
			
		default:
			break;
		}

		try {
			filesink.writeAll(g, path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Libère une référence a un graph dans la list qu'il puisse etre garbagé?
	 * 
	 * @param graph
	 */
	public void forgetGraph(EGraphStreamNetworkType whichOne){
		graphs.remove(whichOne);
	}
	
	public boolean containsGraphType(EGraphStreamNetworkType whichOne){
		return graphs.containsKey(whichOne);
	}
	
	
}
