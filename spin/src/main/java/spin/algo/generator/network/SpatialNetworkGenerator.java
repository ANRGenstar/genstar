package spin.algo.generator.network;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinNetwork;
import spin.SpinPopulation;
import spin.algo.factory.SpinNetworkFactory;
import spll.SpllEntity;
import spll.SpllPopulation;

public class SpatialNetworkGenerator<E extends ADemoEntity> implements INetworkGenerator<E> {
	
	private double distance;
	
	public SpatialNetworkGenerator(double _distance){
		this.distance = _distance;
	}

	@Override
	public SpinPopulation<E> generateNetwork(IPopulation<E, DemographicAttribute<? extends IValue>> myPop) {
		if(myPop instanceof SpllPopulation) {
			return (SpinPopulation<E>) generateNetwork((SpllPopulation) myPop);
		} else {
			return null;			
		}
	}	
	
	/** Spatial network generation 
	 * 
	 * @param myNetwork r�seau de base
	 * @param xMax maximum abscisse
	 * @param yMax maximum ordinate
	 * @param nbTypes different node types
	 * @return myNetwork r�seau final
	 */
	
	public SpinPopulation<SpllEntity> generateNetwork(SpllPopulation myPop) {
		SpinNetwork network = SpinNetworkFactory.loadPopulation(myPop);			
		
		//TODO : use genstar Random Generator 
		//Random rand = new Random();
		
		// List the nodes
		//List<Node> nodes = new ArrayList<>(network.getNodes());
		//int nbNodes = nodes.size();
		
		int link_id = 0;		
		for(SpllEntity e1 : myPop) {
			for(SpllEntity e2 : myPop) {
				double d = e1.getLocation().distance(e2.getLocation());
				if( d <= distance ) {
					network.putLink(Integer.toString(link_id), e1, e2);
					link_id++;
				}				
				
			}
		}

		return new SpinPopulation<>(myPop, network);
	}
}
