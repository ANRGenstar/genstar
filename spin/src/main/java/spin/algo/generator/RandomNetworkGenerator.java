package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

public class RandomNetworkGenerator extends BaseGenerator 
{

	/**
	 * 
	 * @param population
	 * @param proba
	 * @return
	 */
	public SpinNetwork generateNetwork(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population, double proba){
		// TODO: check random generator 
		Random rand = new Random();
		
		// create the spinNetwork
		SpinNetwork myNetwork = loadPopulation(population);
		
		// List the created nodes
		List<NetworkNode> nodes = new ArrayList<>(myNetwork.getNodes());
		
		// Compute the number of links to generate
		// TODO: revoir le type de réseau à générer (diriger ou non ?) 
		int nbLink = (int) Math.round(population.size()*(population.size()-1)*proba);
		int nbNodes = nodes.size();
		NetworkNode nodeFrom, nodeTo;
		NetworkLink link;
		
		// create the links
		int link_id = 0;
		while (nbLink>0) {
			nodeFrom = nodes.get(rand.nextInt(nbNodes));
			nodeTo = nodes.get(rand.nextInt(nbNodes));
			link = new NetworkLink(nodeFrom,nodeTo,false,String.valueOf(link_id));//link is not oriented
			
			if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasLink(link)){
				nbLink--;
				link_id++;
				nodeFrom.addLink(link);
				nodeTo.addLink(link);
			}
			// TODO : create links
			
		}
		return myNetwork;
	}

}
