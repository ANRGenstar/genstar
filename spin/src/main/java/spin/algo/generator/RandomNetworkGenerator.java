package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.*;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.SpinPopulation;
import spin.objects.SpinNetwork;
import useless.BaseGenerator;
import useless.NetworkLink;
import useless.NetworkNode;

public class RandomNetworkGenerator 
{

	/** Génération d'un spinNetwork. 
	 * 
	 * @param population
	 * @param proba
	 * @return
	 */
	public SpinNetwork generateNetwork(SpinPopulation population, double proba){
		// TODO: check random generator 
		Random rand = new Random();
		
		// create the spinNetwork
		SpinNetwork myNetwork = population.getNetwork();
		
		// List the created nodes
		List<Node> nodes = new ArrayList<>(myNetwork.getNodes());
		
		
		// Compute the number of links to generate
		// TODO: revoir le type de reseau à generer (diriger ou non ?) 
		int nbLink = (int) Math.round(population.size()*(population.size()-1)*proba);
		int nbNodes = nodes.size();
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
				//nodeFrom.addLink(link);
				//nodeTo.addLink(link);
			}
			// TODO : create links
			
		}
		return myNetwork;
	}

}
