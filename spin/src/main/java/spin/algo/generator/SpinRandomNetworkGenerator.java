package spin.algo.generator;

import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandom;
import spin.SpinNetwork;
import spin.algo.factory.SpinNetworkFactory;


public class SpinRandomNetworkGenerator<E extends ADemoEntity>  extends  AbstractSpinNetworkGenerator<E>  {
	private double probability;
	
	public SpinRandomNetworkGenerator(String networkName, double proba) {
		super(networkName);
		this.probability = proba;
	}

	@Override
	public SpinNetwork generate(IPopulation<E, Attribute<? extends IValue>> pop) {
		SpinNetwork network = SpinNetworkFactory.loadPopulation(pop);

		GnpRandomGraphGenerator<ADemoEntity, DefaultEdge> generator = new GnpRandomGraphGenerator<>(pop.size(), 
				probability, GenstarRandom.getInstance(), true);
		
		generator.generateGraph(network.getNetwork());
		
		return network;
	}
}
