package spin.algo.generator;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.quadtree.Quadtree;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinNetwork;
import spin.algo.factory.SpinNetworkFactory;
import spll.SpllEntity;
import spll.SpllPopulation;

public class SpinSpatialNetworkGenerator<E extends ADemoEntity>  extends  AbstractSpinNetworkGenerator<E>  {
	
	private double distance;
	
	public SpinSpatialNetworkGenerator(String networkName, double _distance){
		super(networkName);
		this.distance = _distance;
	}

	@Override
	public SpinNetwork generate(IPopulation<E, Attribute<? extends IValue>> myPop) {
		if(myPop instanceof SpllPopulation) {
			return generateNetwork((SpllPopulation) myPop);
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
	
	public SpinNetwork generateNetwork(SpllPopulation myPop) {
		SpinNetwork network = SpinNetworkFactory.loadPopulation(myPop);			
		
		Quadtree quad = new Quadtree();
		for(SpllEntity e1: myPop) {
			quad.insert(e1.getLocation().getEnvelopeInternal(), e1);
		}

		long link_id = 0;		

		for(SpllEntity e1 : myPop) {
			Geometry geom = e1.getLocation().buffer(distance);
			@SuppressWarnings("unchecked")
			List<SpllEntity> l = quad.query(geom.getEnvelopeInternal());
			for (SpllEntity sp : l) {
				if (sp != e1 && geom.intersects(sp.getLocation())) {
					network.putLink(Long.toString(link_id), e1, sp);
					link_id++;								
				}
			}
		}

		return network;
	}
}
