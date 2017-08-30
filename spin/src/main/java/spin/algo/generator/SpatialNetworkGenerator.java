package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.Node;

import spin.objects.SpinNetwork;

public class SpatialNetworkGenerator {
	
	/** Génération d'un réseau spatialisé. 
	 * 
	 * @param myNetwork réseau de base
	 * @param xMax abscisse maximum
	 * @param yMax ordonnée maximum
	 * @param nbTypes nombre de types de noeuds différents
	 * @return myNetwork réseau final
	 */
	public SpinNetwork generateNetwork(SpinNetwork myNetwork, int xMax, int yMax, int nbTypes) {
		// Right now this program uses randomly generated coordinates to locate each node
		// TODO Use a SpllPopulation to locate the nodes
		
		Random rand = new Random();
		
		List<Node> nodes = new ArrayList<>(myNetwork.getNodes());
		int nbNodes = nodes.size();
		
		int reach1 = 50;
		int reach2 = 60;
		int reach3 = 70;
		int nbNodes1 = 0;
		int nbNodes2 = 0;
		int nbNodes3 = 0;
		
		if(nbTypes==1) {
			nbNodes1 = nbNodes;
		} else if(nbTypes==2) {
			nbNodes1 = (int) (nbNodes*0.7);
			nbNodes2 = (int) (nbNodes*0.3);
		} else if(nbTypes==3) {
			nbNodes1 = (int) (nbNodes*0.5);
			nbNodes2 = (int) (nbNodes*0.3);
			nbNodes3 = (int) (nbNodes*0.2);
		} else {
			System.out.println("ERROR : nbTypes must be equal to 1, 2 or 3");
			System.exit(1);
		}
		
		int i=0;
		int j=0;
		while(i<nbNodes) {
			while(j<nbNodes1) {
				Node n = nodes.get(i);
				n.setAttribute("x", rand.nextInt(xMax));
				n.setAttribute("y", rand.nextInt(yMax));
				n.addAttribute("reach", reach1);
				n.addAttribute("ui.label", 1);
				i++;
				j++;
			}
			j=0;
			while(j<nbNodes2) {
				Node n = nodes.get(i);
				n.setAttribute("x", rand.nextInt(xMax));
				n.setAttribute("y", rand.nextInt(yMax));
				n.addAttribute("reach", reach2);
				n.addAttribute("ui.label", 2);
				i++;
				j++;
			}
			j=0;
			while(j<nbNodes3) {
				Node n = nodes.get(i);
				n.setAttribute("x", rand.nextInt(xMax));
				n.setAttribute("y", rand.nextInt(yMax));
				n.addAttribute("reach", reach3);
				n.addAttribute("ui.label", 3);
				i++;
				j++;
			}
		}
		
		ArrayList<Node> computedNodes = new ArrayList<Node>();
		
		int link_id = 0;
		for(Node n1 : nodes) {
			int x1 = n1.getAttribute("x");
			int y1 = n1.getAttribute("y");
			int r1 = n1.getAttribute("reach");
			for(Node n2 : nodes) {
				if(!computedNodes.contains(n2) && n2!=n1) {
					int x2 = n2.getAttribute("x");
					int y2 = n2.getAttribute("y");
					int r2 = n2.getAttribute("reach");
					double d = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
					if(d<=r1 && d<=r2 && !n1.hasEdgeBetween(n2)) {
						myNetwork.putLink(Integer.toString(link_id), n1, n2);
						link_id++;
					}
				}
			}
			computedNodes.add(n1);
		}
		
		return myNetwork;
	}
}
