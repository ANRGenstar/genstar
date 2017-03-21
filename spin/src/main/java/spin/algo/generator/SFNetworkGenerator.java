package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.SpinPopulation;
import spin.objects.SpinNetwork;

public class SFNetworkGenerator {

	public SpinNetwork generateNetwork(SpinNetwork myNetwork){
		
		// Listing the nodes
		List<Node> nodes = new ArrayList<>(myNetwork.getNodes());
		int nbNodes = nodes.size();
		
		// List of links for the selection phase
		List<Edge> links = new ArrayList<Edge>();
		
		// Adding the first link
		// At this point, the graph is composed of two linked nodes and a list of unlinked ones
		myNetwork.putLink(String.valueOf(0), nodes.get(0), nodes.get(1));
		links.add(myNetwork.network.getEdge(String.valueOf(0)));
		int nbLinks = 1;
		
		Random rand = new Random();
		Node nodeFrom, nodeTo;
		Edge ticket;
		int newLinkId = 1;
		
		for(int i=2 ; i<nbNodes ; i++) {
			// The node we want to add to the graph
			nodeFrom = nodes.get(i);
			
			// To choose the node to which we are going to link our new one, we select a random link
			// from our list and choose one of its extremities. This way, the more link a node has,
			// the higher its chances to be chosen are. This system is similar to a lottery, where
			// each link a node has acts as a ticket.
			
			// Choosing a random link from the list
			ticket = links.get(rand.nextInt(nbLinks));
			
			// Choosing one of the link's extremities
			double d = Math.random();
			if(d<0.5) {
				nodeTo = ticket.getNode0();
			} else {
				nodeTo = ticket.getNode1();
			}
			
			// Linking the new node and adding the new link to the list
			if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasEdgeBetween(nodeTo)) {
				myNetwork.putLink(String.valueOf(newLinkId), nodeFrom, nodeTo);
				links.add(myNetwork.network.getEdge(String.valueOf(newLinkId)));
				nbLinks ++;
				newLinkId++;
			}
		}
		
		return myNetwork;
	}
}
