package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.Node;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinNetwork;
import spin.SpinPopulation;
import spin.algo.factory.SpinNetworkFactory;

public class SpinRandomNetworkGenerator<E extends ADemoEntity>  extends  AbstractSpinPopulationGenerator<E>  {
	private double probability;
	
	public SpinRandomNetworkGenerator(double proba) {
		this.probability = proba;
	}

	@Override
	public SpinPopulation<E> generate(IPopulation<E, DemographicAttribute<? extends IValue>> pop) {
		SpinNetwork network = SpinNetworkFactory.loadPopulation(pop);

		// TODO: check random generator 
		Random rand = new Random();
		
		// List the created nodes
		List<Node> nodes = new ArrayList<>(network.getNodes());
		int nbNodes = nodes.size();
		
		// Compute the number of links to generate
		// TODO: revoir le type de reseau à generer (diriger ou non ?) 
		int nbLink = (int) Math.round(nbNodes*(nbNodes-1)*probability);
		Node nodeFrom, nodeTo;
		
		// create the links
		int link_id = 0;
		while (nbLink>0) {
			nodeFrom = nodes.get(rand.nextInt(nbNodes));
			nodeTo = nodes.get(rand.nextInt(nbNodes));
			
			if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasEdgeBetween(nodeTo)){
				network.putLink(String.valueOf(link_id), nodeFrom, nodeTo);
				nbLink--;
				link_id++;
			}
			// TODO : create links
			
		}
		return new SpinPopulation<>(pop, network);
	}
}
