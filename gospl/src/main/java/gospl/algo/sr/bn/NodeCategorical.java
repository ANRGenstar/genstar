package gospl.algo.sr.bn;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;

public class NodeCategorical extends AbstractNode<NodeCategorical> {

	protected NodeCategorical[] parentsArray = new NodeCategorical[0];
	
	protected SortedMap<NodeCategorical,Integer> variable2size = new TreeMap<>();
	
	protected List<String> domain = new LinkedList<>();
		
	private BigDecimal[] content;
	
	/**
	 * stores multipliers to compute indices
	 */
	private int[] multipliers; 

	public NodeCategorical(String name) {
		
		super(name);
		
	}
	
	/*
	public NodeCategorical(String name, LinearProbabilities probabilities, NodeCategorical ... parents) {
		
		super(name);
		
	}
	*/
	
	public NodeCategorical(String name, List<Double> probabilities, NodeCategorical ... parents) {
		
		super(name);
		
	}
	
	protected int getParentsCardinality() {
		return parents.stream().mapToInt(NodeCategorical::getDomainSize).reduce(1, Math::multiplyExact);
	}
	
	/**
	 * returns the number of values, aka size of CPT
	 * @return
	 */
	public int getCardinality() {
		return domain.size()*getParentsCardinality();
	}
	
	@Override
	public void addParent(NodeCategorical parent) {
		super.addParent(parent);
		parentsArray = Arrays.copyOf(parentsArray, parentsArray.length + 1);
		parentsArray[parentsArray.length-1] = parent;
		adaptContentSize();
	}
	
	protected void adaptContentSize() {
		
		final int card = getCardinality();
		//final int ndim = 1+parents.size();
				
		// TODO only if it changed ?
		
		// TODO keep the old array, reuse its probas, etc.
		content = new BigDecimal[card];
		
		// adapt the association domain / size
		
		// adapt multipliers to be able to fetch data later
	    multipliers = new int[parents.size()];
	    int currentfactor = 1;
	    for (int idxParent = 0; idxParent<parentsArray.length; idxParent++) {
	    	currentfactor *= parentsArray[idxParent].getDomainSize();
	    	multipliers[idxParent] = currentfactor;
	    }
	   // multipliers[multipliers.length-1] = 1; // currentfactor*getDomainSize();
	    
	}

	protected int _getIndex(int ourDomainIdx, int... parentIndices) {
		
		int idx = 0;
		
		// add our index
		idx += ourDomainIdx;	
		
		// add the indices of parents
		for (int idxP = 0; idxP < parentIndices.length; idxP++) {
			idx += parentIndices[idxP] * multipliers[idxP];
	    }
		
		return idx;
	}
	

	public void setProbabilities(double p, String key, Object ... parentAndValue) {
		setProbabilities(new BigDecimal(p), key, parentAndValue);
	}
	

	public void setProbabilities(BigDecimal p, String key, Object ... parentAndValue) {
		content[_getIndex(key, parentAndValue)] = p;
	}
	
	public void setProbabilities(BigDecimal[] values) {
		if (values.length != getParentsCardinality()*getDomainSize())
			throw new IllegalArgumentException("wrong size for the content");
		this.content = values;
	}


	public BigDecimal getProbability(String key, Object ... parentAndValue) {
		return content[_getIndex(key, parentAndValue)];
	}
	
	protected BigDecimal getProbability(int key, int[] values) {
		return content[_getIndex(key, values)];
	}
	
	
	protected int[] _getParentIndices(Object ... parentAndValue) {
		
		if (parentAndValue.length % 2 != 0)
			throw new IllegalArgumentException("expecting a list of parameters such as gender, male, age, 0-15");
		if (parentAndValue.length/2 != parents.size()) 
			throw new IllegalArgumentException("not enough parameters");
		
		// find the indices of parents
		int[] parentIndices = new int[parentAndValue.length/2];
		
		for (int i=0; i<parentAndValue.length; i=i+2) {
		
			// find parent based on our input
			Object parentRaw = parentAndValue[i];
			NodeCategorical parent = null;
			int idxParent = -1;
			
			if (parentRaw instanceof NodeCategorical) {
				parent = (NodeCategorical)parentRaw;
			} else if (parentRaw instanceof String) {
				// search for the parent by name 
				parent = getParent((String)parentRaw);
			} else {
				throw new IllegalArgumentException("unable to find parent "+parentRaw);
			}

			// TOOD inefficient
			idxParent = Arrays.asList(parentsArray).indexOf(parent);
			
			// find attribute
			String value = (String)parentAndValue[i+1];
			int idxInDomain = parent.getDomainIndex(value);
			
			parentIndices[idxParent] = idxInDomain;
		}
		
		return parentIndices;
	}
	
	protected final int _getIndex(String ourValue, Object ... parentAndValue) {
		
		return _getIndex(getDomainIndex(ourValue), _getParentIndices(parentAndValue));
	}
	
	
	private void _addDomain(String vv) {
		domain.add(vv);
	}
	
	public void addDomain(String vv) {
		if (domain.contains(vv)) {
			throw new IllegalArgumentException(vv+" is already part of the domain");
		}
		this._addDomain(vv);
		adaptContentSize();
	}
	
	public void addDomain(String ... vvs) {
		
		// check params
		for (String vv : vvs) {
			if (domain.contains(vv)) {
				throw new IllegalArgumentException(vv+" is already part of the domain");
			}
		}
		
		// add values
		for (String vv : vvs) {
			_addDomain(vv);
		}
		
		// adapt cpt size
		adaptContentSize();
	}
	
	
	/**
	 * returns the total of every single probability
	 * @return
	 */
	public final BigDecimal getSum() {
		// TODO not valid yet...
		return Arrays.stream(content).reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	public void normalize() {
		// TODOpublic void normalize() {
		
	}
	
	public Collection<String> getDomain() {
		return Collections.unmodifiableList(domain);
	}
	
	public String getValueIndexed(int v) {
		return domain.get(v);
	}
	
	public int getDomainIndex(String value) {
		return domain.indexOf(value);
	}

	public int getDomainSize() {
		return domain.size();
	}
	
	/**
	 * returns P(V=v | parents)
	 * @param d
	 * @return
	 */
	public BigDecimal getConditionalProbability(String att) {
		
		final int idxAtt = getDomainIndex(att);
		
		// [1,2,3,4]
		// [1,2]
		// [1,2,3]
		
		// cursor = 2
		// 1,1,1
		// 1,1,2,
		// 1,1,3
		
		// 1,2,1
		// 1,2,2
		// 1,2,3
		
		BigDecimal res = BigDecimal.ZERO;
		for (int nb=0; nb<getParentsCardinality();nb++) {
			
			// shift next
			int[] idxParents = new int[parents.size()];

			// climb upwards until we find a place where we can increase the index
			int cursorParents = parents.size()-1;
			while (cursorParents > -1 && idxParents[cursorParents] >= parentsArray[cursorParents].getDomainSize()) {
				idxParents[cursorParents] = 0;
				cursorParents--;
			}
			
			if (cursorParents > -1)
				// then shift next
				idxParents[cursorParents]++;
				
			res = res.add(getProbability(idxAtt, idxParents));

		}
	
		return res;
		
	}
	
	

	/**
	 * returns P(V=v | parents) TODO terms and semantics
	 * @param d
	 * @return
	 */
	public BigDecimal getConditionalProbabilityPosterior(String att, Map<NodeCategorical,String> evidence) {
		
		if (!hasParents())
			return getConditionalProbability(att);
		
		final int idxAtt = getDomainIndex(att);
		
		// [1,2,3,4]
		// [1,2]
		// [1,2,3]
		
		// cursor = 2
		// 1,1,1
		// 1,1,2,
		// 1,1,3
		
		// 1,2,1
		// 1,2,2
		// 1,2,3
		
		BigDecimal resCond = BigDecimal.ZERO;
		//BigDecimal resNonCond = BigDecimal.ZERO;

		// shift next
		int[] idxParents = new int[parents.size()];
		
		for (int nb=0; nb<getParentsCardinality();nb++) {
			

			// climb upwards until we find a place where we can increase the index
			int cursorParents = parents.size()-1;
			while (cursorParents > -1 && idxParents[cursorParents] >= parentsArray[cursorParents].getDomainSize()) {
				idxParents[cursorParents] = 0;
				cursorParents--;
			}
			
			BigDecimal pUsGivenSomething = getProbability(idxAtt, idxParents);
			BigDecimal pSomething = BigDecimal.ZERO;
			for (NodeCategorical p: parents) {
				int idxPValue = idxParents[Arrays.asList(parentsArray).indexOf(p)];
				String pAtt = p.getValueIndexed(idxPValue);
				
				if (!evidence.containsKey(p))
					pSomething = pSomething.add(p.getConditionalProbability(pAtt));
				else if (evidence.get(p).equals(pAtt)) {
					pSomething = pSomething.add(BigDecimal.ONE);
				}
					
			}
					
			//resNonCond = resNonCond.add(pUsGivenSomething);
			resCond = resCond.add(pUsGivenSomething.multiply(pSomething));


			if (cursorParents > -1)
				// then shift next
				idxParents[cursorParents]++;
			
		}
	
		return resCond; // .divide(resNonCond, BigDecimal.ROUND_CEILING);
		
	}
	
	
	public BigDecimal getConditionalProbabilityPosterior(String att) {
		
		return getConditionalProbabilityPosterior(att, Collections.emptyMap());
		
	}

	public BigDecimal getPosterior(String key, Object ... parentAndValue) {
		return content[_getIndex(key, parentAndValue)];
	}
	
	/**
	 * returns true if the conditional probability sums to 1
	 * @return
	 */
	public boolean isValid() {
		
		BigDecimal sumConditionals = BigDecimal.ZERO;
				
		for (String v: domain) {
			BigDecimal post = getConditionalProbability(v);
			if (post.setScale(6, BigDecimal.ROUND_DOWN).compareTo(BigDecimal.ONE.setScale(6)) == 1)
				return false;
			sumConditionals = sumConditionals.add(post);
		}
		
		return sumConditionals.setScale(6,BigDecimal.ROUND_DOWN).compareTo(new BigDecimal(1+parents.size()).setScale(6))==0;
	}
	
	public void toXMLBIF(StringBuffer sb) {
		
		// define the variable
		sb.append("<VARIABLE TYPE=\"").append("nature").append("\">\n");
		sb.append("\t<NAME>").append(StringEscapeUtils.escapeXml10(getName())).append("</NAME>\n");
		for (String s: domain)
			sb.append("\t<OUTCOME>").append(StringEscapeUtils.escapeXml10(s)).append("</OUTCOME>\n");
		sb.append("</VARIABLE>\n");
		sb.append("\n");
		
		// define the distribution
		sb.append("<DEFINITION>\n");
		sb.append("\t<FOR>").append(getName()).append("</FOR>\n");
		for (NodeCategorical n: parentsArray)
			sb.append("\t<GIVEN>").append(n.getName()).append("</GIVEN>\n");

		sb.append("\t<TABLE>");
		for (BigDecimal p: content) {
			sb.append(p.setScale(5,BigDecimal.ROUND_DOWN).toString()).append(" ");
		}
		sb.append("</TABLE>\n");

		sb.append("</DEFINITION>\n");
		
		
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("p(");
		sb.append(getName());
		if (hasParents())
			sb.append("|");
		boolean first = true;
		for (NodeCategorical p: getParents()) {
			sb.append(p.getName());
			if (first) 
				first = false;
			else
				sb.append(",");
				
		}
		sb.append(")");
		return sb.toString();
	}
	
}
