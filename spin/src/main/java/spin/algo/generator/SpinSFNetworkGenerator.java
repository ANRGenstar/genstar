package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DefaultEdge;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandom;
import spin.SpinNetwork;
import spin.algo.factory.SpinNetworkFactory;

public class SpinSFNetworkGenerator<E extends ADemoEntity>  extends  AbstractSpinNetworkGenerator<E> {

	public SpinSFNetworkGenerator(String networkName) {
		super(networkName);
	}
	
	/** Generation of a ScaleFree network 
	 * 
	 * @param myPop base population
	 * @return network final network
	 */
	@Override
	public SpinNetwork generate(IPopulation<E, Attribute<? extends IValue>> myPop) {
		SpinNetwork network = SpinNetworkFactory.loadPopulation(myPop);	
		
		// Listing the nodes
		List<ADemoEntity> nodes = new ArrayList<>(network.getNodes());
		int nbNodes = nodes.size();
		
		// List of links for the selection phase
		List<DefaultEdge> links = new ArrayList<>();
		
		// Adding the first link
		// At this point, the graph is composed of two linked nodes and a list of unlinked ones
		network.putLink(String.valueOf(0), nodes.get(0), nodes.get(1));
		links.add(network.getNetwork().getEdge(nodes.get(0), nodes.get(1)));
		int nbLinks = 1;
		
		ADemoEntity nodeFrom, nodeTo;
		DefaultEdge ticket;
		int newLinkId = 1;
		
		for(int i=2 ; i<nbNodes ; i++) {
			// The node we want to add to the graph
			nodeFrom = nodes.get(i);
			
			// To choose the node to which we are going to link our new one, we select a random link
			// from our list and choose one of its extremities. This way, the more link a node has,
			// the higher its chances to be chosen are. This system is similar to a lottery, where
			// each link a node has acts as a ticket.
			
			// Choosing a random link from the list
			ticket = links.get(GenstarRandom.getInstance().nextInt(nbLinks));
			
			// Choosing one of the link's extremities
			double d = Math.random();
			if(d<0.5) {
				nodeTo = network.getNetwork().getEdgeSource(ticket);
			} else {
				nodeTo = network.getNetwork().getEdgeTarget(ticket);
			}
			
			// Linking the new node and adding the new link to the list
			if(!nodeFrom.equals(nodeTo) && !network.getNetwork().containsEdge(nodeFrom, nodeTo)) {
				network.putLink(String.valueOf(newLinkId), nodeFrom, nodeTo);
				links.add(network.getNetwork().getEdge(nodeFrom,nodeTo));
				nbLinks ++;
				newLinkId++;
			}
		}
				
		return network;	
	}
}
