package spin.algo.generator;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.GosplEntity;
import gospl.GosplPopulation;
import spin.SpinPopulation;
import spin.algo.generator.network.CompleteNetworkGenerator;
import spin.algo.generator.network.INetworkGenerator;
import spin.algo.generator.network.RandomNetworkGenerator;
import spin.algo.generator.network.RegularNetworkGenerator;
import spin.algo.generator.network.SFNetworkGenerator;

public class SpinPopulationGenerator {
	private INetworkGenerator networkGenerator;
	
	public SpinPopulationGenerator(INetworkGenerator generator) {
		this.networkGenerator = generator;
	}
	
	public SpinPopulation<? extends ADemoEntity> generate(IPopulation<? extends ADemoEntity, DemographicAttribute<? extends IValue>> pop) {
		return networkGenerator.generateNetwork(pop);
	}
	
	@SuppressWarnings("unchecked")
	public SpinPopulation<GosplEntity> generate(int numberOfIndividual) {
		GosplPopulation pop = new GosplPopulation();
		for(int i = 0 ; i<numberOfIndividual;i++) {
			pop.add(new GosplEntity());
		}
		return (SpinPopulation<GosplEntity>) networkGenerator.generateNetwork(pop);
	}

	public static void main(String[] argc) {
		SpinPopulationGenerator spinPopGen = 
				new SpinPopulationGenerator(new CompleteNetworkGenerator<GosplEntity>());
		SpinPopulation<GosplEntity> networkedPop = spinPopGen.generate(30);
		
		System.out.println(networkedPop.toString());		

		
		spinPopGen = 
				new SpinPopulationGenerator(new RegularNetworkGenerator<>(4));
		networkedPop = spinPopGen.generate(30);
		
		System.out.println(networkedPop.toString());		

		
		spinPopGen = 
				new SpinPopulationGenerator(new RandomNetworkGenerator<>(0.1));
		networkedPop = spinPopGen.generate(30);
		
		System.out.println(networkedPop.toString());	
		
		
		spinPopGen = 
				new SpinPopulationGenerator(new SFNetworkGenerator<>());
		networkedPop = spinPopGen.generate(30);
		
		System.out.println(networkedPop.toString());	
		
	}
}
