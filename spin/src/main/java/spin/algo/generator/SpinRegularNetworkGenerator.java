package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinNetwork;
import spin.algo.factory.SpinNetworkFactory;


public class SpinRegularNetworkGenerator<E extends ADemoEntity> extends  AbstractSpinNetworkGenerator<E> {

	private int k;
	
	public SpinRegularNetworkGenerator(String networkName, int _k) {
		super(networkName);
		this.k = _k;
	}
	
	/** 
	 * Generation of a regular network
	 * 
	 * TODO : move to JGraphT generator ;)
	 * 
	 * @param myNetwork base network
	 * @param k network connectivity
	 * @return myNetwork final network
	 */
	@Override
	public SpinNetwork generate(IPopulation<E, Attribute<? extends IValue>> myPop) {
		SpinNetwork network = SpinNetworkFactory.loadPopulation(myPop);
	
		List<ADemoEntity> nodes = new ArrayList<>(network.getNodes());
		
		// for each node i, create a link to i+1 ... i+k/2
		int link_id = 0;
		for(int i=0; i<nodes.size();i++) {
			for (int j=1;j<=k/2;j++){
				network.putLink(String.valueOf(link_id), nodes.get(i), nodes.get((i+j)%nodes.size()));
				link_id++;				
			}
		}
		
		if(k%2 == 1 && nodes.size()%2 == 0) {
			for(int i=0 ; i<nodes.size() ; i++) {
				ADemoEntity n1 = nodes.get(i);
				int j = (k/2)+1;
				while(network.getNetwork().edgesOf(n1).size()<k) {
					
					ADemoEntity n2 = nodes.get((i+j)%nodes.size());
					
					if(network.getNetwork().edgesOf(n2).size()<k && !network.getNetwork().containsEdge(n1, n2)) {
						network.putLink(String.valueOf(link_id), n1, n2);
						link_id++;
					}
					j++;
				}
			}
		}
		
		return network;
	}
	

}
