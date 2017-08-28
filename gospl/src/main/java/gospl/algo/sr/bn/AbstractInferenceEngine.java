package gospl.algo.sr.bn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.util.random.GenstarRandom;

public abstract class AbstractInferenceEngine {

	private Logger logger = LogManager.getLogger();

	protected final CategoricalBayesianNetwork bn;
	
	/**
	 * evidence
	 */
	protected Map<NodeCategorical,String> evidenceVariable2value = new HashMap<>();
	
	/**
	 * should we recompute probabilities ?
	 */
	protected boolean dirty = true;
	
	
	public AbstractInferenceEngine(CategoricalBayesianNetwork bn) {
		this.bn = bn;
		
		
	}

	/**
	 * Adds evidence, in the form p(n=s)=1 | XXX. 
	 * If there was another evidence for this node, it will be replaced.
	 * @param n
	 * @param s
	 */
	public void addEvidence(NodeCategorical n, String s) {
		
		if (!this.bn.containsNode(n))
			throw new IllegalArgumentException("this node is not in the bn: "+n);
		
		if (!n.getDomain().contains(s))
			throw new IllegalArgumentException("value "+s+" unknown in node "+n);
		
		dirty = (s != evidenceVariable2value.put(n, s)) || dirty;
		
	}
	
	public void addEvidence(String n, String s) {
		NodeCategorical node = this.bn.getVariable(n);
		if (node == null)
			throw new IllegalArgumentException("unknown node "+n);
		this.addEvidence(node, s);
	}
	
	public void removeEvidence(NodeCategorical n) {
		dirty = evidenceVariable2value.remove(n)!=null || dirty;
	}
	
	public void clearEvidence() {
		
		if (!evidenceVariable2value.isEmpty()) {
			dirty = true;
			evidenceVariable2value = new HashMap<>(); // not clear: other places might keep references to the past evidence
		}

		
	}
	
	public void compute() {
		
		
		// propagate probabilities (belief propagation)
		
		dirty = false;
		
	}
	

	/**
	 * Computes all the values in the Bayesien network.
	 * Note this is most of the time useless.
	 */
	public void computeAll() {
		for (NodeCategorical n: bn.nodes) {
			logger.info("computing probability for {} ({} values: {})", n, n.getDomainSize(), n.getDomain());
			retrieveConditionalProbability(n);	
			InferencePerformanceUtils.singleton.display();

		}
		
	}
	
	protected abstract double retrieveConditionalProbability(NodeCategorical n, String s);
	
	protected abstract double[] retrieveConditionalProbability(NodeCategorical n);

	/**
	 * Returns the conditional probability based on the initial probability distribution of the network 
	 * conditioned by evidence. Note that if no evidence is defined, it will return directly the prior conditional probability
	 * from the Bayesian network.
	 * @param variableName
	 * @param s
	 * @return
	 */
	public final double getConditionalProbability(NodeCategorical n, String s) {
		
		// check args
		if (!n.getDomain().contains(s))
			throw new IllegalArgumentException("there is no value "+s+" in the domain of variable "+n+" (use one of "+n.getDomain()+")");
		
		// if there is no evidence, just return the prior probability !
		if (evidenceVariable2value.isEmpty()) {
			// TODO ? 
			//return n.getConditionalProbabilityPosterior(s);
		}
		
		// if that is directly conditionned by evidence, return it
		{
			String v = evidenceVariable2value.get(n);
			if (v != null) {
				if (v.equals(s))
					return 1.0;
				else 
					return 0.0;
			}
		}
		
		// compute if evidence is new
		if (dirty)
			compute();
		
		return retrieveConditionalProbability(n, s);
	}
	
	/**
	 * Returns the conditional probability based on the initial probability distribution of the network 
	 * conditioned by evidence. Note that if no evidence is defined, it will return directly the prior conditional probability
	 * from the Bayesian network.
	 * @param variableName
	 * @param s
	 * @return
	 */
	public final double getConditionalProbability(String variableName, String s) {
		NodeCategorical v = bn.getVariable(variableName);
		if (v == null)
			throw new IllegalArgumentException("this Bayesian network does not contains a variable named "+variableName);
		return this.getConditionalProbability(v, s);
	}
	

	/**
	 * Selects the variables relevant to assess the probability of node toCompute 
	 * knowing evidence and all the nodes that might be considered.
	 * @param toCompute
	 * @param evidence
	 * @param all
	 * @return
	 */
	protected Set<NodeCategorical> selectRelevantVariables(
			NodeCategorical toCompute,
			Map<NodeCategorical,String> evidence,
			Set<NodeCategorical> all
			) {
		Set<NodeCategorical>  relevant = new HashSet<>(all.size());
		
		if (toCompute != null)
			relevant.addAll(toCompute.getAllAncestors());
		
		for (NodeCategorical n: evidence.keySet()) {
			relevant.addAll(n.getAllAncestors());
		}
		
		return relevant;
	}
	
	
	protected Set<NodeCategorical> selectRelevantVariables(
			Set<NodeCategorical> toCompute,
			Map<NodeCategorical,String> evidence,
			Set<NodeCategorical> all
			) {
		
		if (logger.isTraceEnabled())
			logger.trace(
					"select relevant variables to compute {} for evidence {} among {}", 
					toCompute.stream().map(NodeCategorical::getName).collect(Collectors.joining(",")),
					evidence.entrySet().stream().map(e->e.getKey().name+"="+e.getValue()).collect(Collectors.joining(",")),
					all.stream().map(NodeCategorical::getName).collect(Collectors.joining(","))
					);
		Set<NodeCategorical>  relevant = new HashSet<>(all.size());
		

		// for sure, we need to compute the probabilities of the parents of the probabilities questionned here
		for (NodeCategorical n: toCompute)
			relevant.addAll(n.getAllAncestors());
		
		// for sure, nodes with evidence are impacted
		for (NodeCategorical n: evidence.keySet()) {
			relevant.addAll(n.getAllAncestors());
		}
		
		// TODO ? relevant.removeAll(evidence.keySet());
				
		if (logger.isTraceEnabled())
			logger.trace(
					"selected relevant variables {}", 
					relevant.stream().map(NodeCategorical::getName).collect(Collectors.joining(","))
					);
		return relevant;
	}

	public Factor computeFactorPriorMarginalsFromString(Set<String> variables) {
		return this.computeFactorPosteriorMarginals(
				variables.stream().map(s -> bn.getVariable(s)).collect(Collectors.toSet())
				);
	}
	
	public Factor computeFactorPosteriorMarginals(Set<NodeCategorical> variables) {
		throw new UnsupportedOperationException("this inference engine does not computes prior marginals as factors");
	}

	public void addEvidence(Map<NodeCategorical, String> systematicEvidence) {
		for (Map.Entry<NodeCategorical,String> e: systematicEvidence.entrySet()) {
			addEvidence(e.getKey(), e.getValue());
		}
	}
	
	/**
	 * Generates an instanciation of the network given current evidence. 
	 * The default implementation works for any inference engine, but inheriting classes
	 * might define more efficient methods.
	 * @return
	 */
	public Map<NodeCategorical,String> sampleOne() {
		
		double pEvidence = getProbabilityEvidence() ;
		
		if (pEvidence == 0.0)
			throw new IllegalArgumentException("cannot generate if the probability of evidence is 0 - evidence is not possible");
		
		Map<NodeCategorical,String> originalEvidence = new HashMap<>(evidenceVariable2value);
		
		// we start with the original evidence. 
		
		Map<NodeCategorical,String> node2attribute = new HashMap<>();
		// define values for each individual
		for (NodeCategorical n: bn.enumerateNodes()) {
			
			String value = null;
			
			if (originalEvidence.containsKey(n))
					value = originalEvidence.get(n);
			else {
				
				double random = GenstarRandom.getInstance().nextDouble();
				// pick up a value
				double cumulated = 0.;
				
				//System.err.println("should pick a value for "+n.name);
				for (String v : n.getDomain()) {
					
					double p = this.getConditionalProbability(n, v);
					cumulated += p;
					//System.err.println("p("+n.name+"="+v+")="+p+" => "+cumulated+" ("+random+")");
	
					if (cumulated >= random) {
						value = v;
						break;
					}
				}
				if (value == null)
					throw new RuntimeException("oops, should have picked a value based on postererior probabilities for variable "+n+" knowing "+evidenceVariable2value+", but they sum to "+cumulated);
				
			}
			// that' the property of this individual
			node2attribute.put(n, value);
			// store this novel value as evidence for this individual
			this.addEvidence(n, value);
		}
				
		// reset evidence to its original value
		this.clearEvidence();
		this.addEvidence(originalEvidence);
		
		return node2attribute;
	}
	
	/**
	 * returns the probability for the current evidence.
	 * @return
	 */
	public final double getProbabilityEvidence() {
		
		// quick exit 
		if (evidenceVariable2value.isEmpty())
			return 1.0;
		
		// another quick exit
		/*
		if (evidenceVariable2value.size()==1) {
			NodeCategorical n = evidenceVariable2value.keySet().iterator().next();
			String v = evidenceVariable2value.get(n);
			
			return n.getConditionalProbabilityPosterior(v);
			
		}
		*/
		
		// we cannot be quick. Let's delegate.
		if (dirty)
			this.compute();
		
		return computeProbabilityEvidence();
	}
	
	/**
	 * Called when evidence is not empty to compute the probability of evidence. 
	 * Ideally it might be computed once during the compute() method
	 * @return
	 */
	protected abstract double computeProbabilityEvidence();

	

	/**
	 * if this node is part of evidence, returns an array of double 
	 * with 1. when compliant with evidence or else 0.
	 * @param n
	 * @return
	 */
	protected double[] getEvidenceAsDoubleArray(NodeCategorical n) {

		String valueFromEvidence = evidenceVariable2value.get(n);
		
		if (valueFromEvidence == null)
			throw new IllegalArgumentException("cannot return evidence as a double array if there is not evidence for the current node");
		
		double[] values = new double[n.getDomainSize()]; // initialized to 0. by java
		values[n.getDomainIndex(valueFromEvidence)] = 1.0;
		
		return values;
	}
	
}
