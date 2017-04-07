package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.Node;

import spin.objects.SpinNetwork;

public class RandomNetworkGenerator 
{

	/** Génération d'un spinNetwork. 
	 * 
	 * @param population
	 * @param proba
	 * @return
	 */
	public SpinNetwork generateNetwork(SpinNetwork myNetwork, double proba){
		// TODO: check random generator 
		Random rand = new Random();
		
		// List the created nodes
		List<Node> nodes = new ArrayList<>(myNetwork.getNodes());
		int nbNodes = nodes.size();
		
		// Compute the number of links to generate
		// TODO: revoir le type de reseau à generer (diriger ou non ?) 
		int nbLink = (int) Math.round(nbNodes*(nbNodes-1)*proba);
		Node nodeFrom, nodeTo;
		
		// create the links
		int link_id = 0;
		while (nbLink>0) {
			nodeFrom = nodes.get(rand.nextInt(nbNodes));
			nodeTo = nodes.get(rand.nextInt(nbNodes));
			
			if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasEdgeBetween(nodeTo)){
				myNetwork.putLink(String.valueOf(link_id), nodeFrom, nodeTo);
				nbLink--;
				link_id++;
			}
			// TODO : create links
			
		}
		return myNetwork;
	}

}
