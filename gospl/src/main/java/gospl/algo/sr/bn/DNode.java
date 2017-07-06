package gospl.algo.sr.bn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.util.random.GenstarRandom;

/**
 * A DNode of a DTree - by extension, a DNode is the DTree rooted on this node. 
 * 
 * You might tune the cache ratio and other characteristics by tuning the static variables prior 
 * to computation.
 * 
 * @author Samuel Thiriot
 *
 */
public final class DNode {
	
	/**
	 * The cache ratio: if 0.5, half of the the possible values to cache will be cached.
	 */
	public static double cacheRatio = 0.5; 
	
	/**
	 * Always accept to cache up to that amount of values (makes no sense to cache only 1 result over 2 possible !)
	 */
	public static int minCachedCount = 100;
	public static int maxCachedCount = 100000;

	
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * the factor (if any) associated to this node in the dtree
	 */
	public final Factor f;
	
	public final NodeCategorical n;
	
	public CategoricalBayesianNetwork bn;

	
	protected DNode left;
	protected DNode right;
	
	protected DNode parent;
	
	/**
	 * Cached computation of left.vars() <intersect> right.vars()
	 */
	public Set<NodeCategorical> vars = null;
	
	/** 
	 * Cached computation of left.varts() <intersect> right.vars - acutset()
	 */
	public Set<NodeCategorical> cutset = null;
	
	/**
	 * Cached computation of parent.cutset() 
	 */
	public Set<NodeCategorical> acutset = null;

	public Set<NodeCategorical> context = null;
	
	public Set<NodeCategorical> cluster = null;

	private NodeCategorical eliminated = null;
	
	/**
	 * The cache which associates to each evidence (of interest to this node) the computed probability.
	 */
	private LRUMap<Map<NodeCategorical,String>,Double> cacheEvidenceInContext2proba = null;
	
	/**
	 * creates a node with no specific role. 
	 * SHould be defined later, as this node is in an inconsistent state.
	 * @param m
	 */
	public DNode() {
		this.n = null;
		this.f = null;
		this.bn = null;
		
	}
	
	/**
	 * Returns (and constructs) the cache depending to the characteristics of the dnode, 
	 * and the parameters for cache ration, min and max.
	 * @return
	 */
	private final LRUMap<Map<NodeCategorical,String>,Double> getCache() {
		
		if (cacheEvidenceInContext2proba == null) {
			// create a cache with the right loading factor 
			// how many values should we compute at most ? 
			int card = 1;
			for (NodeCategorical n: vars()) {
				card *= n.getDomainSize();
			}
			int toCache = (int) Math.round(cacheRatio*card);
			if (toCache < minCachedCount) {
				toCache = Math.min(card, minCachedCount);
			}
			if (toCache > maxCachedCount) {
				toCache = maxCachedCount;
			}
			if (toCache < 1)
				toCache = 1; // technical for LRUMap
			logger.debug("cache: max values to query: {}, will cache {}", card, toCache);
			cacheEvidenceInContext2proba = new LRUMap<Map<NodeCategorical,String>, Double>(toCache);
		}
		return cacheEvidenceInContext2proba;
		
	}
	
	
	/**
	 * creates an intermediate node
	 * @param m
	 */
	public DNode(DNode m) {
		this.n = null;
		this.f = null;
		this.bn = m.bn;
	}
	
	/**
	 * creates a leaf node for the given variable
	 * @param n
	 */
	public DNode(NodeCategorical n) {
		this.n = n;
		this.f = n.asFactor();
		this.bn = n.cNetwork;
	}
	
	/**
	 * Creates a root Node
	 * @param bn
	 */
	public DNode(CategoricalBayesianNetwork bn) {
		this.n = null;
		this.f = null;
		this.bn = bn;
	}


	/**
	 * Composes a set of DNode in a DTree in an arbitrary order
	 * @param toCompose
	 * @return
	 */
	protected static DNode compose(CategoricalBayesianNetwork bn, Set<DNode> toCompose) {
		
		if (toCompose.size() == 1)
			return toCompose.iterator().next();
		
		logger.trace("composing {}", toCompose);

		List<DNode> toComposeL = new ArrayList<>(toCompose);
		Collections.shuffle(toComposeL);
		int cutIdx = toComposeL.size()/2;
		Set<DNode> left = new HashSet<>(toComposeL.subList(0, cutIdx));
		Set<DNode> right = new HashSet<>(toComposeL.subList(cutIdx, toComposeL.size()));
		
		DNode res = new DNode(bn);
		compose(bn, left).becomeLeftChild(res);
		compose(bn, right).becomeRightChild(res);
		
		logger.debug("composed {} into {}", toCompose, res);

		return res;
	}

	/**
	 * construct a DTree, returned as a DNode, following the given elimination order.
	 * @param bn
	 * @param cutset
	 * @return
	 */
	protected static DNode eliminationOrder2DTree(CategoricalBayesianNetwork bn, List<NodeCategorical> cutset) {
		
		logger.debug("create DTree from elimination order {}", cutset);

		// create the leafs of the future tree for each variable
		Set<DNode> nodes = bn.getNodes().stream().map(m->new DNode(m)).collect(Collectors.toSet());
		
		for (NodeCategorical toCut: cutset) {
			
			logger.debug("now decomposing on {}", toCut);

			Set<DNode> toCompose = nodes.stream().filter(t -> t.vars().contains(toCut)).collect(Collectors.toSet());
			
			if (toCompose.size() <= 1)
				continue;
			
			logger.debug("composition of {}", toCompose);
			nodes.removeAll(toCompose);
			
			DNode composed = compose(bn, toCompose);
			composed.setEliminated(toCut);
			nodes.add(composed);
			
		}
		
		logger.debug("final composition of {}", nodes);
		DNode res = compose(bn, nodes); 
		
		res.setNetwork(bn);
		
		logger.debug("decomposed into: {}", res);

		return res;
	}
	

	public void setLeft(DNode left) {
		if (this.f != null)
			throw new IllegalArgumentException("cannot add a child to a factor node");
		this.left = left;
	}
	
	public void setRight(DNode right) {
		if (this.f != null)
			throw new IllegalArgumentException("cannot add a child to a factor node");
		this.right = right;
	}
	
	public boolean isRoot() {
		return parent == null;
	}
	

	public boolean isLeaf() {
		return left == null && right == null;
	}
	
	public boolean isInternal() {
		return parent != null && left != null & right != null;
	}
	
	/**
	 * returns the variables shared on the left and right subtress - 
	 * or the variables of this variable if it is a leaf
	 * @return
	 */
	public Set<NodeCategorical> vars() {
		
		if (left == null && right == null) {
			return f.variables;
		}
		
		if (vars == null) {
			vars = new HashSet<>(left.vars());
			// in its 2001 paper Darwich defines it as intersection: 
			//vars.retainAll(right.vars());
			// but in its book its actually union !
			vars.addAll(right.vars());
			logger.trace("computed vars {}", vars);
		}
		
		return vars;
	}

	/**
	 * Returns the cutset for this node, that is the variables of this node 
	 * excludign the parent cutset.
	 * @return
	 */
	public Set<NodeCategorical> cutset() {
		
		if (isRoot())
			return this.vars();
		
		if (cutset == null) {
			cutset = new HashSet<>(this.vars());
			cutset.removeAll(this.acutset());
			logger.trace("computed cutset {}", cutset);
		}
		return cutset;
	}

	/**
	 * Returns the parent cutset
	 * @return
	 */
	public Set<NodeCategorical> acutset() {
		
		if (isRoot())
			return Collections.emptySet();
		
		if (acutset == null) {
			acutset = new HashSet<>(parent.acutset());
			//acutset.addAll(this.cutset());
			logger.trace("computed acutset {}", cutset);
		}
		
		return acutset;
	}
	
	public Set<NodeCategorical> context() {
		
		if (context == null) {
			context = new HashSet<>(vars());
			context.retainAll(acutset());
		}
		
		return context;
	}
	
	public Set<NodeCategorical> cluster() {
		
		if (isLeaf())
			return vars();
					
		if (cluster == null) {
			cluster = new HashSet<>(cutset());
			cluster.addAll(context());
		}
		
		return context;
	}
	
	/**
	 * The context width is the size of the maximal context
	 * @return
	 */
	public int contextWidth() {
		int mine = context().size();
		if (!isLeaf())
			mine = Math.max(mine, Math.max(this.left.contextWidth(), this.right.contextWidth()));
		return mine;
	}
	
	/**
	 * Lookup algorithm as defined by Derwiche 
	 * @param n2v
	 * @return
	 */
	protected double lookup(Map<NodeCategorical,String> n2v) {
		
		logger.debug("Lookup on {} for {}", f, n2v);

		//String vMe = n2v.get(this.n);
			
		if (!n2v.containsKey(this.n)) {
			logger.trace("Not concerned by evidence => 1.");
			// our variable is not instantiated
			return 1.;
		}
		/*
		if (!n2v.keySet().containsAll(this.f.variables)) {
			logger.trace("Not concerned by evidence => 1.");
			// our variable is not instantiated
			return BigDecimal.ONE;
		}
		*/
		// X is instantiated
		
		//String v = n2v.get(this.n);
		
		// Factor x = this.f;
		
		Map<NodeCategorical,String> coord = new HashMap<>(n2v);
		coord.keySet().retainAll(f.variables);
		// we received the complete computation 
		
		return f.get(coord);
		
		/*
		
		
		return f.reduceTo(coord.keySet()).get(coord);
		
		// easiets solution: direct read
		if (coord.size() == f.variables.size()) 
			return f.get(coord);
			
		// else, sum !
		
		BigDecimal sum = BigDecimal.ZERO;
		Set<NodeCategorical> remaining = new HashSet<>(f.variables);
		remaining.removeAll(coord.keySet());
		for (IteratorCategoricalVariables it = bn.iterateDomains(remaining); it.hasNext(); ) {
			Map<NodeCategorical,String> v2n = it.next();
			v2n.putAll(coord);
			BigDecimal pCond = f.get(v2n);
			logger.trace("Sum on {} = {}", v2n, pCond);
			sum = sum.add(pCond);
			InferencePerformanceUtils.singleton.incAdditions();
		}
		
		logger.trace("Lookup on {} = {}", coord, sum);

		return sum;
		*/
	}
	
	/**
	 * Implements RC1 by Derwiche
	 * 
	 * @param n2v
	 * @return
	 */
	public double recursiveConditionning(Map<NodeCategorical,String> n2v) {
		
		if (n2v.isEmpty())
			return 1.0;
		
		logger.debug("Recursive Conditionning on {} for {}", this, n2v);
		
		if (isLeaf()) {
			logger.trace("is leaf => lookup");
			return lookup(n2v);
		}
		

		// cache ? 
		// caching 
		// warning: this is NOT what is described by Darwiche.
		// Darwiche defines sometimes vars(T) as the intersection of other vars(Tl) inter vars(Tl)
		// this solution is not working for recursive conditionning, only union makes sense (as defined in his book in some places)
		// If we refer here to context, we don't have any variable remaining most of the time, so the cache is returning 
		// meaningless results
		// therefore we reuse here the intuition but not what is defined by Darwiche
		// start from evidence (which has to be taken in the cache, else results would not depend on it)
		// keep only our variables defined as the union of children variable (because the others will not play any role for us, so the result would be the same)
		Map<NodeCategorical,String> y = new HashMap<>(n2v);
		y.keySet().retainAll(this.vars()); 
				
		logger.trace("search in cache {}", y);
		Double cached = getCache().get(y);
		if (cached != null) {
			//logger.info("cached :-)");
			return cached;
		} else {
			//logger.info("Not found in the {} cached items evidences", cacheEvidenceInContext2proba.size());
		}
		
		

		logger.trace("no leaf => summing over cutset {}", cutset());
		double p = 0.;

		// for each instantiation c of uninstantiated variables in cutset()
		Set<NodeCategorical> uninstantiated = new HashSet<>(cutset());
		// don't explore all the combinations already defined by evidence
		uninstantiated.removeAll(n2v.keySet());
		
		logger.trace("no leaf => exploring combinations over {}", uninstantiated);

			
		for (IteratorCategoricalVariables it = bn.iterateDomains(uninstantiated); it.hasNext(); ) {
		
			Map<NodeCategorical,String> instantiation = it.next();
			instantiation.putAll(n2v);
			
			logger.trace("Sum of {} over {} ", this, instantiation);

			double pLeft = left.recursiveConditionning(instantiation);
			logger.trace("Sum of {} over {} = {}", left, instantiation, pLeft);
			if (pLeft == 0)
				// if pLeft is zero, then why computing the recursive conditionning of right ?
				continue; 

			double pRight = right.recursiveConditionning(instantiation);
			logger.trace("Sum of {} over {} = {}", left, instantiation, pRight);

			double m = pLeft * pRight;
			p += m;
			
			InferencePerformanceUtils.singleton.incAdditions();
			InferencePerformanceUtils.singleton.incMultiplications();
			
			if (p >= 1) {
				//logger.info("1 is reached in the sum already, stopping computations!");
				p = 1;
				break;
			}
			
		}
		
		cacheEvidenceInContext2proba.put(y,p);
		
		return p;
	}
	
	public void notifyDTreeChanged() {
		vars = null;
		cutset = null;
		acutset = null;
	}


	/**
	 * Ensures the basic principles of a dtree are enforced 
	 */
	public void checkConsistency() {
		
		if (this.f != null && (this.left != null || this.left != null))
			throw new IllegalArgumentException("only leafs can contain CPTs");
		
		if (this.left == null ^ this.right == null) 
			throw new IllegalArgumentException("can have either zero or two children");
			
		if (parent != null) {
			if (parent.right != this && parent.left != this)
				throw new IllegalArgumentException("inconsistent hierarchy: parent should have us as a child");
		}
		
		if (left != null) {
			if (left.parent != this)
				throw new IllegalArgumentException("child should have us as a parent ");
			left.checkConsistency();
		}
		if (right != null) {
			if (right.parent != this)
				throw new IllegalArgumentException("child should have us as a parent ");
			right.checkConsistency();
		}
	
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		if (isLeaf())
			sb.append(n.toString());
		else {
			
			sb.append("[ ").append(left.toString());
			
			sb.append(" /");
			if (this.eliminated == null)
				sb.append("?");
			else 
				sb.append(this.eliminated.name);
			sb.append("\\ ");
				
			sb.append(right.toString()).append(" ]");
		}
		
		return sb.toString();
		
	}


	public void becomeRightChild(DNode parent) {
		this.parent = parent;
		this.bn = parent.bn;
		// cleanly disconnect the previous child
		if (parent.right != null && parent.right != this) {
			parent.right.parent = null;
		}
		parent.setRight(this);
	}
	
	public void becomeLeftChild(DNode parent) {
		this.parent = parent;
		this.bn = parent.bn;
		// cleanly disconnect the previous child
		if (parent.left != null && parent.left != this) {
			parent.left.parent = null;
		}
		parent.setLeft(this);
	}


	public void setNetwork(CategoricalBayesianNetwork bn) {
		this.bn = bn;
	}
	

	public NodeCategorical getEliminated() {
		return eliminated;
	}


	public void setEliminated(NodeCategorical eliminated) {
		this.eliminated = eliminated;
	}


	public void instanciate(Map<NodeCategorical, String> evidenceVariable2value) {

		logger.debug("reducing {} based on evidence {}", this, evidenceVariable2value);
		if (isLeaf()) {
			this.f.reduce(evidenceVariable2value);
		} else {
			right.instanciate(evidenceVariable2value);
			left.instanciate(evidenceVariable2value);
		}
		
 	}

	public void generate(Map<NodeCategorical, String> defined) {

		logger.trace("generating for {} and known {}", this, defined);
		 
		double random = GenstarRandom.getInstance().nextDouble();

		if (isLeaf()){
			
			if (defined.containsKey(n))
				return;
			
			// that's a leaf ! 
			// it means we have a factor to take values from :-)
			logger.trace("picking a value from our factor {}", this.f);
			
			Factor reduced = f.reduction(defined);
			//logger.trace("reduced: {} sums up to {}", reduced, reduced.sum());

			// random should be updated, because this factor corresponds to conditional probabilities which sum up to 1 for each value
			random = random * this.n.getParents().size();
			
			double cumulated = 0.;
			for (Map.Entry<Map<NodeCategorical,String>,Double> e: reduced.values.entrySet()) {
				cumulated += e.getValue();
				if (cumulated >= random) {
					defined.putAll(e.getKey());
					logger.trace("picked from CPT: {}", e.getKey());
					return; // stop all !
				} 
			}
			
		} else {
			// not a leaf ! should call left and right
			logger.trace("calling left {}", left);
			left.generate(defined);
			logger.trace("calling right {}", right);
			right.generate(defined);	
		} 

		
		/*
		// if we know which variable we eliminate on this node, the process is ideal !
		if (eliminated != null) {
			
			if (defined.containsKey(eliminated)) {
				logger.trace("not computing {} which was already defined", eliminated);
				return;
			}
				
			logger.trace("we know we eliminate here {}", eliminated);
			
			double cumulated = 0.;

			// we know which variable we play with here !
			String value = null;


			double norm = recursiveConditionning(defined);
			
			for (String v: eliminated.getDomain()) {
				
				defined.put(eliminated, v);
				double p = recursiveConditionning(defined)/norm;
				cumulated += p;
				if (cumulated >= random) {
					value = v;
					logger.trace("picked {}={}", eliminated.name, v);
					break;
				}
			}

			if (value == null)
				throw new RuntimeException("oops, should have picked a value based on postererior probabilities, but they sum to "+cumulated);

		} else {
			// we have to compute over the set of our variable... 
			logger.trace("we don't know what we eliminate here; exploring all the variables {}", this.vars());
			
			
			double cumulated = 0.;
			for (
					IteratorCategoricalVariables it = new IteratorCategoricalVariables(this.vars()); 
					it.hasNext();
					) {
				
				Map<NodeCategorical,String> c = it.next();
				
				c.putAll(defined);
				double p = recursiveConditionning(c);
				cumulated += p;
				if (cumulated >= random) {
					defined.putAll(c);
					logger.trace("picked {}", c);
					break;
				}
			}
		}
		*/
	}


}
