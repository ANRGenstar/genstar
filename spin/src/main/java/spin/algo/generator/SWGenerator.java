package spin.algo.generator;

import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import gospl.metamodel.GosplPopulation;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

public class SWGenerator implements INetworkGenerator
{
	
	@Override
	public SpinNetwork generateNetwork(GosplPopulation population) {
		return this.generateNetwork(population,4,0.1D);
	}
	
	public SpinNetwork generateNetwork(GosplPopulation population, int k, double beta){
		//int k connectivity of the network
		//double beta noise introduced on the regular network
		// crée un réseau régulier 
		SpinNetwork myNetwork = (new RegularNetworkGenerator()).generateNetwork(population,k);
				
		//parcourir tous les liens
		HashSet<NetworkLink> links = new HashSet<>(myNetwork.getLinks());
		List<NetworkNode> nodes = new ArrayList<>(myNetwork.getNodes());
		int nbNodes = nodes.size();
		
		//pour chacun si proba < beta ; supprimer (des deux cotés) et rebrancher aléatoirement 
		Random rand = new Random();
		for(NetworkLink l : links){
			if(rand.nextDouble()<beta){
				l.getFrom().removeLink(l);
				l.getTo().removeLink(l);
				
				NetworkNode nodeFrom, nodeTo;
				NetworkLink link;
				boolean linkCreated=false;
				// create the links
				while (!linkCreated) {
					nodeFrom = nodes.get(rand.nextInt(nbNodes));
					nodeTo = nodes.get(rand.nextInt(nbNodes));
					link = new NetworkLink(nodeFrom,nodeTo,false);//link is not oriented
					
					if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasLink(link)){
						linkCreated=true;
						nodeFrom.addLink(link);
						nodeTo.addLink(link);
					}					
				}
				
			}
		}
		
		/** A Suppr ???
		// table temporaire pour garder une référence indexé sur les noeuds créés
		Hashtable<Integer, NetworkNode> nodeCreated = new Hashtable<Integer, NetworkNode>();
		int i = 0;
		
		
		// cyclé sur la pop
		for (IEntity<A,V> entity : population) {
			
			// créer un objet noeud et lui associer l'entité, puis ajouter le node a spinNetwork
			// on crée un objet noeud et un set de lien vide qui sera rempli apres
//			N node = new N(entity);
			
//			 new NetworkNode<E, V, A>(entity);
			NetworkNode node =  new NetworkNode(entity);
			nodeCreated.put(i++, node);
			myNetwork.putNode(node);
		}
		
		
		
		// fin de création de tous les noeuds du réseau 
		
		// Cycle sur les noeuds du réseau
		/** FA: creer des Liens puis les ajouter aux links de chaque Node
		 for (int j = 0; j < nodeCreated.size(); j++) {
		 
			int indexDestination = j;
			while (indexDestination == j)
				indexDestination = new Random().nextInt(nodeCreated.size() - 1);
			if(nodeCreated.get(j).
					connectedNodes.
					contains(
							nodeCreated.get(indexDestination))){
				// Rien
			}
			else {
			nodeCreated.get(j).connectedNodes.add(nodeCreated.get(indexDestination));
			nodeCreated.get(indexDestination).connectedNodes.add(nodeCreated.get(j));
			}
		}*/
		
		/**
		for (NetworkNode node : myNetwork.getNodes()) {
			
			
			// créer un lien vers d'autre noeud puis ajouter les liens a spin ET mettre a jour la liste des noeuds connectés a un noeud
//			nodeCreated.get(0).add(nodeCreated.get(3));
		}
		//for
				
		// fin de création des liens
		*/
		
		return myNetwork;
	}

	
}



