package gospl.algo.sr.bn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
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
	public static int minCachedCount = 500;
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
	public Set<NodeCategorical> varsUnion = null;
	public Set<NodeCategorical> varsInter = null;

	
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
	 * Creates a node which is a clone of another node
	 * @param o
	 */
	protected DNode(DNode o) {
		this.n = o.n;
		this.f = o.f==null?null:o.f.clone();
		this.bn = o.bn;
		this.acutset = o.acutset;
		this.cacheEvidenceInContext2proba = o.cacheEvidenceInContext2proba;
		this.cluster = o.cluster;
		this.context = o.context;
		this.cutset = o.cutset;
		this.parent = null; // to be replaced later by the cloning process
		
		// duplicate recursively our children
		if (o.right != null) {
			this.right = o.right.clone();
			this.right.parent = this;
			this.right.resetCache();
		}
		if (o.left != null) {
			this.left = o.left.clone();
			this.left.parent = this;	
			this.left.resetCache();
		}
	}
	

	protected void resetCache() {
		if (cacheEvidenceInContext2proba != null)
			cacheEvidenceInContext2proba.clear();
		cluster = null;
		context = null;
		acutset = null;
		cutset = null;
		varsUnion = null;
		varsInter = null;
	}
	
	public int getDepth() {
		if (this.parent == null)
			return 0;
		else 
			return this.parent.getDepth()+1;
	}
	
	/**
	 * Deep clone of this node
	 */
	public DNode clone() {
		return new DNode(this);
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
			long card = 1;
			for (NodeCategorical n: context()) { // TODO
				card *= n.getDomainSize();
			}
			int toCache = (int) Math.round(cacheRatio*card);
			if (toCache < minCachedCount && card > 0) {
				toCache = (int)Math.min(card, minCachedCount);
			}
			if (toCache > maxCachedCount || card < 0) { // in case we overflow... for sure we better use as much storage as possible !
				toCache = maxCachedCount;
			}
			if (toCache < 1)
				toCache = 1; // technical for LRUMap
			logger.info("cache: max values to query: {}, will cache {}\n{}", card, toCache,this);
			cacheEvidenceInContext2proba = new LRUMap<Map<NodeCategorical,String>, Double>(toCache);
		}
		return cacheEvidenceInContext2proba;
		
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

		res.resetCache();
		
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
		Set<DNode> nodes = bn.getNodes().stream()
				.filter(v -> cutset.contains(v))
				.map(m->new DNode(m))
				.collect(Collectors.toSet());
		
		for (NodeCategorical toCut: cutset) {
			
			logger.debug("now decomposing on {}", toCut);

			Set<DNode> toCompose = nodes.stream().filter(t -> t.varsInter().contains(toCut)).collect(Collectors.toSet());
			
			if (toCompose.size() <= 1)
				continue;
			
			logger.debug("composition of {}", toCompose);
			nodes.removeAll(toCompose);
			
			DNode composed = compose(bn, toCompose);
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
		resetCache();
	}
	
	public void setRight(DNode right) {
		if (this.f != null)
			throw new IllegalArgumentException("cannot add a child to a factor node");
		this.right = right;
		resetCache();
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
	public Set<NodeCategorical> varsUnion() {
		
		if (left == null && right == null) {
			return f.variables;
		}
		
		if (varsUnion == null) {
			varsUnion = new HashSet<>(left.varsUnion());
			// in its 2001 paper Darwich defines it as intersection: 
			//vars.retainAll(right.vars());
			// but in its book its actually union !
			varsUnion.addAll(right.varsUnion());
			logger.trace("computed vars {}", varsUnion);
		}
		
		return varsUnion;
	}
	
	public Set<NodeCategorical> varsInter() {
		
		if (left == null && right == null) {
			return f.variables;
		}
		
		if (varsInter == null) {
			varsInter = new HashSet<>(left.varsInter());
			// in its 2001 paper Darwich defines it as intersection: 
			//vars.retainAll(right.vars());
			// but in its book its actually union !
			varsInter.retainAll(right.varsInter());
			logger.trace("computed varsInter {}", varsInter);
		}
		
		return varsInter;
	}
	
	private Set<NodeCategorical> cutsetWithACutset() {
		Set<NodeCategorical> cutset = new HashSet<>(left.varsUnion());
		cutset.retainAll(right.varsUnion());
		
		return cutset;
	}

	/**
	 * Returns the cutset for this node, that is the variables of this node 
	 * excludign the parent cutset.
	 * @return
	 */
	public Set<NodeCategorical> cutset() {
		
		if (cutset == null) {
			cutset = new HashSet<>(left.varsUnion());
			cutset.retainAll(right.varsUnion());
			
			cutset.removeAll(this.acutset());
			
			logger.trace("computed cutset {}", cutset);
		}
		return cutset;
	}

	protected void getUnionCutsetParents(Set<NodeCategorical> result) {
		
		result.addAll(cutsetWithACutset());
		
		if (parent != null)
			result.addAll(parent.cutsetWithACutset());
	}
	
	/**
	 * Returns the parent cutset
	 * @return
	 */
	public Set<NodeCategorical> acutset() {
		
		if (isRoot())
			return Collections.emptySet();
		
		if (acutset == null) {
			acutset = new HashSet<>(bn.getNodes().size());
			parent.getUnionCutsetParents(acutset);
			//acutset.addAll(this.cutset());
			logger.trace("computed acutset {}", acutset);
		}
		
		return acutset;
	}
	
	public Set<NodeCategorical> context() {
		
		if (context == null) {
			context = new HashSet<>(varsUnion());
			context.removeAll(acutset());
		}
		
		return context;
	}
	
	public Set<NodeCategorical> cluster() {
		
		if (isLeaf())
			return varsUnion();
					
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
	
	public double recursiveConditionning(String ... ss) {
		return recursiveConditionning(bn.toNodeAndValue(ss));
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
		
		if (logger.isDebugEnabled()) 
			logger.debug("Recursive Conditionning for {} on:\n {}", n2v, this);
		
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
		y.keySet().retainAll(varsUnion());
		
		// NOT WORKING ! 
		//y.keySet().retainAll(context()); 
		
		if (!context().isEmpty() && !y.isEmpty()) {
			logger.trace("search in cache {}", y);
			Double cached = getCache().get(y);
			if (cached != null) {
				//logger.info("cached :-)");
				InferencePerformanceUtils.singleton.incCacheHit();
				// TODO reactivate !!! 
				return cached;
			} else {
				//logger.info("Not found in the {} cached items evidences", cacheEvidenceInContext2proba.size());
				InferencePerformanceUtils.singleton.incCacheMiss();
	
			}
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("no leaf => summing over cutset {}", cutset().stream().map(v->v.name).collect(Collectors.joining(",")));
		}
		double p = 0.;

		// for each instantiation c of uninstantiated variables in cutset()
		Set<NodeCategorical> uninstantiated = new HashSet<>(cutset());
		// don't explore all the combinations already defined by evidence
		uninstantiated.removeAll(n2v.keySet());
		
		if (logger.isTraceEnabled()) {
			logger.trace("no leaf => exploring combinations over {}", uninstantiated.stream().map(v->v.name).collect(Collectors.joining(",")));
		}
		
		// before optim:
		// A => multiplication: 655, additions:655, cache hits:20245 and miss:616
		// B => multiplication: 9787, additions:9787, cache hits:40218 and miss:7659
		// C => multiplication: 761, additions:761, cache hits:20558 and miss:727

		// after optim based on union of variable sizes
		// A => multiplication: 620, additions:620, cache hits:20638 and miss:643
		// B => multiplication: 7446, additions:7446, cache hits:38222 and miss:5221
		
		// after optim based on cutset size
		// C => multiplication: 658, additions:658, cache hits:20474 and miss:622

		// on sachs:
		// without
		// multiplication: 5712, additions:5712, cache hits:34504 and miss:5121
		// multiplication: 7303, additions:7303, cache hits:37831 and miss:5135
		// with 
		// multiplication: 7418, additions:7418, cache hits:37979 and miss:4990
		// multiplication: 6014, additions:6014, cache hits:34585 and miss:5395
		// multiplication: 5782, additions:5782, cache hits:34420 and miss:5178
		// multiplication: 5782, additions:5782, cache hits:34420 and miss:5178

		// on gerland: 
		// before optim
		// multiplication: 799, additions:799, cache hits:201907 and miss:822
		// multiplication: 831, additions:831, cache hits:201685 and miss:823
		// multiplication: 656, additions:656, cache hits:202072 and miss:686
		// multiplication: 720, additions:720, cache hits:201665 and miss:692
		
		// after optim
		// multiplication: 737, additions:737, cache hits:202236 and miss:692
		// multiplication: 701, additions:701, cache hits:202227 and miss:721
		// multiplication: 674, additions:674, cache hits:201963 and miss:727
		// multiplication: 783, additions:783, cache hits:202301 and miss:831
		
		DNode first = left;
		DNode last = right;
		/*
		if (shouldComputeFirst(left, right, n2v)) {
			first = left;
			last = right;
		} else {
			first = right;
			last = left;
		}*/
		
		for (IteratorCategoricalVariables it = bn.iterateDomains(uninstantiated); it.hasNext(); ) {
		
			Map<NodeCategorical,String> instantiation = it.next();
			instantiation.putAll(n2v);
			
			if (logger.isTraceEnabled())
				logger.trace("Sum of {} over\n {}", instantiation, this);

			
				
			double pLeft = first.recursiveConditionning(instantiation);
			if (logger.isTraceEnabled())
				logger.trace("Sum of {} = {}  ", instantiation, pLeft);
			if (pLeft == 0.)
				// if pLeft is zero, then why computing the recursive conditionning of right ?
				continue; 

			double pRight = last.recursiveConditionning(instantiation);
			if (logger.isTraceEnabled())
				logger.trace("Sum of {} = {}", instantiation, pRight);

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
		
		if (cacheEvidenceInContext2proba != null && !y.isEmpty())
			cacheEvidenceInContext2proba.put(y,p);
		
		return p;
	}
	

	/**
	 * determines which one of these nodes should be computed first, 
	 * that is which one is more prone to quickly give a zero, so we can hopefully stop computations 
	 * earlier 
	 * @param left2
	 * @param right2
	 * @return
	 */
	private final boolean shouldComputeFirst(DNode candidate, DNode challenger, Map<NodeCategorical,String> n2v) {
		
		/*Set<NodeCategorical> left = new HashSet<>(candidate.varsUnion());
		left.removeAll(n2v.keySet());
		
		Set<NodeCategorical> right = new HashSet<>(challenger.varsUnion());
		right.removeAll(n2v.keySet());
		
		return left.size() < right.size();
		
		*/
		
		//return candidate.varsUnion().size() < challenger.varsUnion().size();
		
		if (candidate.isLeaf()) {
			if (challenger.isLeaf()) {
				return candidate.f.variables.size() <= challenger.f.variables.size();
			} else {
				return true;
			}
		} else {
			if (challenger.isLeaf()) {
				return false;
			} else {
				return candidate.cutset().size() <= challenger.cutset().size();
			}
		}
		
		//return candidate.cutset().size() <= challenger.cutset().size();
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
		sb.append(StringUtils.repeat("\t", getDepth()));
		if (isLeaf()) {
			sb.append("|-- ").append(n.toString()).append("\n");
		} else {
			sb.append("|-- vars: ").append(varsInter().stream().map(v->v.name).collect(Collectors.joining(",")));
			
			sb.append(" cutset: ").append(cutset().stream().map(v->v.name).collect(Collectors.joining(",")));
			sb.append(" acutset:").append(acutset().stream().map(v->v.name).collect(Collectors.joining(",")));
			sb.append(" context:").append(context().stream().map(v->v.name).collect(Collectors.joining(",")));
			sb.append("\n");
			sb.append(left.toString());
			sb.append(right.toString());
			
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
		resetCache();

	}
	
	public void becomeLeftChild(DNode parent) {
		this.parent = parent;
		this.bn = parent.bn;
		// cleanly disconnect the previous child
		if (parent.left != null && parent.left != this) {
			parent.left.parent = null;
		}
		parent.setLeft(this);
		resetCache();
	}


	public void setNetwork(CategoricalBayesianNetwork bn) {
		this.bn = bn;
	}
	

	public void instanciate(Map<NodeCategorical, String> evidenceVariable2value) {

		logger.debug("reducing {} based on evidence {}", this, evidenceVariable2value);
		if (isLeaf()) {
			this.f.reduce(evidenceVariable2value);
		} else {
			right.instanciate(evidenceVariable2value);
			left.instanciate(evidenceVariable2value);
		}
		
		// our caches are not valid anymore ! let's forget everything.
		resetCache();
		
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

	/**
	 * writes this dtree (or subtree) as a graphviz network
	 * @param file
	 */
	public final void exportAsGraphviz(File file) {

		StringBuffer sb = new StringBuffer();
		sb.append("# to generate it, use:\n# dot -Tjpg "+file.getAbsolutePath()+" -o "+file.getAbsolutePath()+".jpg\n");
		sb.append("# if you also want to open it under linux:\n# dot -Tjpg "+file.getAbsolutePath()+" -o "+file.getAbsolutePath()+".jpg && xdg-open "+file.getAbsolutePath()+".jpg\n");
		sb.append("digraph dtree {\n");
		sb.append("\trankdir=TB;\n");
		this.exportAsGraphvizInto(sb, new HashMap<DNode,String>());
		sb.append("}\n");
		
		FileWriter fw;
		try {
			fw = new FileWriter(file);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("error while exporting dtree into file "+file, e);
		}
		
	}

	private void exportAsGraphvizInto(StringBuffer sb, Map<DNode,String> node2id) {
		
		String thisId = node2id.get(this);
		if (thisId == null) {
			thisId = Integer.toString(node2id.size());
			node2id.put(this, thisId);
		}
		
		// add this node with its name
		sb.append("\t\"").append(thisId).append("\" [label=\"");
		if (this.n != null)
			sb.append(this.n);
		else {
			
			Set<NodeCategorical> eliminated = new HashSet<>(cluster());
			eliminated.removeAll(context());
			
			Set<NodeCategorical> toDisplay = cutset();
			sb.append(toDisplay.stream().map(v->v.name).collect(Collectors.joining(",")));

			/*
			sb.append("cutset:");
			sb.append(cutset().stream().map(v->v.name).collect(Collectors.joining(",")));
			sb.append(", acutset:");
			sb.append(acutset().stream().map(v->v.name).collect(Collectors.joining(",")));
			*/
		}
		sb.append("\"];\n");
		
		// add the link between the parent and us - if the parent was added already
		if (parent != null && node2id.containsKey(parent)) {
			sb.append("\t\"").append(node2id.get(parent)).append("\" -> ").append("\"").append(thisId).append("\";\n");
		}
		
		// recursively add the children nodes
		if (left != null)
			left.exportAsGraphvizInto(sb, node2id);
		if (right != null)
			right.exportAsGraphvizInto(sb, node2id);
		
		 	
	}

}
