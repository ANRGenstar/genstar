package spin.algo.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinNetwork;
import spin.algo.factory.SpinNetworkFactory;

/**Générateur SmallWorld
 * 
 *
 */
public class SpinSWNetworkGenerator<E extends ADemoEntity>  extends  AbstractSpinNetworkGenerator<E> {

	private int k;
	private double beta;
	
	public SpinSWNetworkGenerator(String networkName, int _k, double _beta){
		super(networkName);
		this.k = _k;
		this.beta = _beta;
	}
	
	/** Generation of a SmallWorld network. 
	 * 
	 * @param myNetwork base network
	 * @param k network connectivity
	 * @param beta rewiring rate
	 * @return myNetwork final network
	 */
	@Override
	public SpinNetwork generate(IPopulation<E, Attribute<? extends IValue>> myPop) {
		SpinNetwork network = SpinNetworkFactory.loadPopulation(myPop);

		SpinRegularNetworkGenerator<E> spinPopGen = new SpinRegularNetworkGenerator<>(networkName, k);
		@SuppressWarnings("unused")
		SpinNetwork networkedPop = spinPopGen.generate(myPop);
				
		//parcourir tous les liens
		HashSet<Edge> links = new HashSet<>(network.getLinks());
		int nbLinks = links.size();
		List<Node> nodes = new ArrayList<>(network.getNodes());
		int nbNodes = nodes.size();
		
		//pour chacun si proba < beta ; supprimer (des deux cotes) et rebrancher aleatoirement 
		Random rand = new Random();
		
		int link_id = nbLinks;
		for(Edge l : links){
			if(rand.nextDouble()<beta){
				network.removeLink(l);
				
				// create the links
				Node nodeFrom, nodeTo;
				boolean linkCreated=false;
				
				while (!linkCreated) {
					nodeFrom = nodes.get(rand.nextInt(nbNodes));
					nodeTo = nodes.get(rand.nextInt(nbNodes));
					
					if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasEdgeBetween(nodeTo)){
						network.putLink(String.valueOf(link_id), nodeFrom, nodeTo);
						linkCreated=true;
						link_id++;
					}					
				}
			}
		}
				
		return network;	
	}	
}



