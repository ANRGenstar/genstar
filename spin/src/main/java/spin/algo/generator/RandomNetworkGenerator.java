package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.io.survey.entity.AGenstarEntity;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.metamodel.IPopulation;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

public class RandomNetworkGenerator implements INetworkGenerator 
{
	
	public SpinNetwork generateNetwork(IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> population) {
		return this.generateNetwork(population,0D);
	}
	
	public SpinNetwork generateNetwork(IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> population, double proba){
		// TODO: check random generator 
		Random rand = new Random();
		
		// create the spinNetwork
		SpinNetwork myNetwork = INetworkGenerator.loadPopulation(population);
		
		// List the created nodes
		List<NetworkNode> nodes = new ArrayList<>(myNetwork.getNodes());
		
		// Compute the number of links to generate
		// TODO: revoir le type de réseau à générer (diriger ou non ?) 
		int nbLink = (int) Math.round(population.size()*(population.size()-1)*proba);
		int nbNodes = nodes.size();
		NetworkNode nodeFrom, nodeTo;
		NetworkLink link;
		
		// create the links
		while (nbLink>0) {
			nodeFrom = nodes.get(rand.nextInt(nbNodes));
			nodeTo = nodes.get(rand.nextInt(nbNodes));
			link = new NetworkLink(nodeFrom,nodeTo,false);//link is not oriented
			
			if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasLink(link)){
				nbLink--;
				nodeFrom.addLink(link);
				nodeTo.addLink(link);
			}
			// TODO : create links
			
		}
		return myNetwork;
	}

}
