package spin.algo.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.objects.SpinNetwork;
import useless.NetworkLink;
import useless.NetworkNode;

/**Générateur SmallWorld
 * 
 *
 */
public class SWNetworkGenerator extends BaseGenerator
{
	/**
	 * 
	 * @param population
	 * @param k
	 * @param beta
	 * @return
	 */
	public SpinNetwork generateNetwork(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population, int k, double beta){
		//int k connectivity of the network
		//double beta noise introduced on the regular network
		// crée un réseau régulier 
		SpinNetwork myNetwork = (new RegularNetworkGenerator()).generateNetwork(population,k);
				
		//parcourir tous les liens
		HashSet<NetworkLink> links = new HashSet<>(myNetwork.getLinks());
		List<NetworkNode> nodes = new ArrayList<>(myNetwork.getNodes());
		int nbNodes = nodes.size();
		
		//pour chacun si proba < beta ; supprimer (des deux cotés) et rebrancher aléatoirement 
		Random rand = new Random();
		for(NetworkLink l : links){
			if(rand.nextDouble()<beta){
				l.getFrom().removeLink(l);
				l.getTo().removeLink(l);
				
				NetworkNode nodeFrom, nodeTo;
				NetworkLink link;
				boolean linkCreated=false;
				// create the links
				int link_id = 0;
				while (!linkCreated) {
					nodeFrom = nodes.get(rand.nextInt(nbNodes));
					nodeTo = nodes.get(rand.nextInt(nbNodes));
					link = new NetworkLink(nodeFrom,nodeTo,false,String.valueOf(link_id));//link is not oriented
					
					if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasLink(link)){
						linkCreated=true;
						link_id++;
						nodeFrom.addLink(link);
						nodeTo.addLink(link);
					}					
				}
			}
		}
		
		return myNetwork;
	}

	
}



