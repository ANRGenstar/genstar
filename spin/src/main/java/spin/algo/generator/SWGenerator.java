package spin.algo.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.interfaces.INetworkGenerator;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

public class SWGenerator  extends BaseGenerator
{
	
	@Override
	public SpinNetwork generateNetwork(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) {
		return this.generateNetwork(population,4,0.1D);
	}
	
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
				while (!linkCreated) {
					nodeFrom = nodes.get(rand.nextInt(nbNodes));
					nodeTo = nodes.get(rand.nextInt(nbNodes));
					link = new NetworkLink(nodeFrom,nodeTo,false);//link is not oriented
					
					if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasLink(link)){
						linkCreated=true;
						nodeFrom.addLink(link);
						nodeTo.addLink(link);
					}					
				}
			}
		}
		
		return myNetwork;
	}

	
}



