package gospl.algo.sr.bn;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeCategorical extends FiniteNode<NodeCategorical> {

	private Logger logger = LogManager.getLogger();
	
	protected NodeCategorical[] parentsArray = new NodeCategorical[0];
	
	protected SortedMap<NodeCategorical,Integer> variable2size = new TreeMap<>();

	private Integer countZeros = null;
		
	private BigDecimal[] content;
	
	/**
	 * stores multipliers to compute indices
	 */
	private int[] multipliers; 

	public NodeCategorical(CategoricalBayesianNetwork net, String name) {
		
		super(net, name);
		
		
	}
	

	public Integer getCountOfZeros() {
		if (countZeros == null) 
			computeCountOfZeros();
		return countZeros;
	}

	private void computeCountOfZeros() {
		int count = 0;
		for (BigDecimal x: content) {
			if (x.compareTo(BigDecimal.ZERO) == 0)
				count++;
		}
		this.countZeros = count;
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



	protected final int _getIndex(String ourValue, Object ... parentAndValue) {
		
		return _getIndex(getDomainIndex(ourValue), _getParentIndices(parentAndValue));
	}
	

	protected final int _getIndex(String ourValue, Map<NodeCategorical,String> parent2Value) {
		
		return _getIndex(getDomainIndex(ourValue), _getParentIndices(parent2Value));
	}
	
	
	protected void adaptContentSize() {
		
		logger.trace("resizing CPT for node {} for domain {} ({}) and parents card {} => {}", name, domain.size(), domain, getParentsCardinality(), getCardinality());

		final int card = getCardinality();
		//final int ndim = 1+parents.size();
				
		// TODO only if it changed ?
		
		// TODO keep the old array, reuse its probas, etc.
		content = new BigDecimal[card];
		
		// adapt the association domain / size
		
		// adapt multipliers to be able to fetch data later
	    multipliers = new int[parents.size()];
	    for (int idxParent = 0; idxParent<parentsArray.length; idxParent++) {
	    	int currentfactor = getDomainSize();
		    for (int j=idxParent+1; j<parentsArray.length; j++) {
		    	currentfactor *= parentsArray[j].getDomainSize();
		    }
	    	multipliers[idxParent] = currentfactor;
	    }
	   // multipliers[multipliers.length-1] = 1; // currentfactor*getDomainSize();
	    
		logger.trace("=> novel index multipliers {}", multipliers);

	    
	}

	/**
	 * returns the flatten index for the value of index ourDomainIdx in our domain and the given indices for parents
	 * 
	 * @see http://codinghighway.com/2014/01/27/c-multidimensional-arrays/
	 * 
	 * @param ourDomainIdx
	 * @param parentIndices
	 * @return
	 */
	protected int _getIndex(int ourDomainIdx, int... parentIndices) {
		
		logger.trace(
				"computing flatten index for {}={}, parents {} in {} with multipliers {}", 
				name, 
				domain.get(ourDomainIdx), 
				parentIndices, 
				parentsArray,
				multipliers
				);

		int idx = 0;
		
		// add our index
		idx += ourDomainIdx;	
		
		// add the indices of parents
		for (int idxP = 0; idxP < parentIndices.length; idxP++) {
			idx += parentIndices[idxP] * multipliers[idxP];
	    }
		
		logger.trace("computed flatten index {} for {} {} in {} values {} with multipliers {}", idx, name, ourDomainIdx, domain, parentIndices, multipliers);

		return idx;
	}
	

	public void setProbabilities(double p, String key, Object ... parentAndValue) {
		setProbabilities(new BigDecimal(p), key, parentAndValue);
	}
	

	public void setProbabilities(BigDecimal p, String key, Object ... parentAndValue) {
		countZeros = null;
		content[_getIndex(key, parentAndValue)] = p;
	}
	
	public void setProbabilities(BigDecimal[] values) {
		if (values.length != getParentsCardinality()*getDomainSize())
			throw new IllegalArgumentException("wrong size for the content");
		countZeros = null;
		this.content = values;
	}


	public BigDecimal getProbability(String key, Object ... parentAndValue) {
		return content[_getIndex(key, parentAndValue)];
	}
	

	public BigDecimal getProbability(String key, Map<NodeCategorical,String> parent2Value) {
		return content[_getIndex(key, parent2Value)];
	}
	

	
	/**
	 * Returns the probability stored for our domain value "key" and the parents domain values "values"
	 * @param key
	 * @param values
	 * @return
	 */
	protected BigDecimal getProbability(int key, int[] values) {
		//logger.trace("get probability for key {} in {} and values {} in {}", key, domain, values, parentsArray.length > 0 ? parentsArray[0].getDomain(): "");
		//try {
			return content[_getIndex(key, values)];
		//} catch (ArrayIndexOutOfBoundsException e) {
		//	throw new RuntimeException("Wrong internal storage for the data: stored "+content.length+" values for "+getParentsDimensionality()+" parents dim and "+domain.size()+ " values", e);
		//}
	}
	
	
	protected int[] _getParentIndices(Map<NodeCategorical,String> parent2Value) {
		
		if (parent2Value.size() != parents.size() || !parent2Value.keySet().containsAll(parents)) {
			throw new IllegalArgumentException("expecting all the parents values to be defined");
		}
		
		// find the indices of parents
		int[] parentIndices = new int[parents.size()];
		
		for (int i=0; i<parentIndices.length; i=i+1) {
		
			parentIndices[i] = parentsArray[i].getDomainIndex(parent2Value.get(parentsArray[i]));
		}
		
		return parentIndices;
	}
	
	/**
	 * For a list of parameters such as gender, male, age, 0-15, returns the indices of the values 
	 * for each index of parent. 
	 * 
	 * @param parentAndValue
	 * @return
	 */
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

			// TODO inefficient
			idxParent = Arrays.asList(parentsArray).indexOf(parent);
			
			// find attribute
			String value = (String)parentAndValue[i+1];
			int idxInDomain = parent.getDomainIndex(value);
			
			parentIndices[idxParent] = idxInDomain;
		}
		
		return parentIndices;
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
	
	/**
	 * returns the dimensionality of all the parents; for instance if there is only one parent gender (male|female), then the dimensionality is 2; 
	 * if there is a second parent age3, then the dimensionality will be 6. 
	 * If there is no parent dimensionality is 1.
	 * @return
	 */
	public int getParentsDimensionality() {
		return parents.stream().mapToInt(NodeCategorical::getDomainSize).reduce(1, Math::multiplyExact); 
	}
	
	public void normalize() {
		// TODOpublic void normalize() {
		// TODO 
	}
	
	/**
	 * returns the probability conditional to all parents P(V=v | parents=*)
	 * @param d
	 * @return
	 */
	public BigDecimal getConditionalProbability(String att) {
		
		logger.trace("computing conditional probability p({}={})", name, att);
		
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
	
		logger.trace("computed conditional probability p({}={})={}", name, att, res);

		return res;
		
	}
	
	public BigDecimal getConditionalProbabilityPosterior(
						String att, 
						Map<NodeCategorical,String> evidence, 
						Map<NodeCategorical,Map<String,BigDecimal>> alreadyComputed) {

		return getConditionalProbabilityPosterior(att, evidence, alreadyComputed, Collections.emptyMap());
	}
	

	/**
	 * returns P(V=v | parents) TODO terms and semantics
	 * @param d
	 * @return
	 */
	public BigDecimal getConditionalProbabilityPosterior(
							String att, 
							Map<NodeCategorical,String> evidence, 
							Map<NodeCategorical,Map<String,BigDecimal>> alreadyComputed, 
							Map<NodeCategorical,String> forcedValue) {
		
		logger.trace("computing posteriors for p({}={}|{})", name, att, evidence);
		logger.trace("alreadyComputed: {}", alreadyComputed);
		
		
		// quickest exist: maybe evidence says somehting about us, in this case we just return it !
		if (evidence.containsKey(this)) {
			
			if (evidence.get(this).equals(att)) {
				logger.trace("from evidence, posteriors p({}={})=1.0", name, att);
				return BigDecimal.ONE;
			} else {
				logger.trace("from evidence, posteriors p({}={})=0.0", name, att);
				return BigDecimal.ZERO;
			}
		}
		
		// another quick exit: maybe that was already computed in the past, so why bother ? 
		if (alreadyComputed != null) {
			Map<String,BigDecimal> done = alreadyComputed.get(this);
			if (done != null) {
				BigDecimal res = done.get(att);
				if (res != null) {
					return res;
				}
			}
		}
		
		// quick exit: maybe we have no parent, in this case we just return the probability we store
		if (!hasParents()) {
			logger.trace("no parent, returning internal probability");
			return getConditionalProbability(att);
		}
		
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

		// list all the dimensions to be explored
		int[] idxParents = new int[parents.size()];
		int totalCard = 1;
		for (int p=0; p<parents.size(); p++) {
			if (forcedValue.containsKey(parentsArray[p])) {
				// if the value is locked, use it 
				idxParents[p] = parentsArray[p].getDomainIndex(forcedValue.get(parentsArray[p]));
			} else {
				idxParents[p] = parentsArray[p].getDomainSize()-1;
				totalCard *= parentsArray[p].getDomainSize();
			}
			
		}
		int cursorParents = parents.size()-1;
		
		for (int nb=0; nb<totalCard;nb++) {
			
			
			logger.trace("now cursor parents {} idxParents {}", cursorParents, idxParents);
			
			logger.trace("adding to probability p({}={}|*) from parents {}", name, att, parents.stream().
					collect(Collectors.toMap(NodeCategorical::getName, p -> p.getValueIndexed(idxParents[Arrays.asList(parentsArray).indexOf(p)]))) 
					);
			
			BigDecimal pUsGivenSomething = getProbability(idxAtt, idxParents);
			BigDecimal pSomething = BigDecimal.ONE;
			for (NodeCategorical p: parents) {
				int idxPValue = idxParents[Arrays.asList(parentsArray).indexOf(p)]; // TODO inefficient
				String pAtt = p.getValueIndexed(idxPValue);
								
				logger.trace("computing posteriors for parent p({}={})", p.name, pAtt);
				BigDecimal cpp = p.getConditionalProbabilityPosterior(pAtt, evidence, alreadyComputed); 
				pSomething = pSomething.multiply(cpp);
				logger.trace("cumulated * {} = {}", cpp, pSomething);

				if (pSomething.compareTo(BigDecimal.ZERO) == 0) {
					// we can even break that loop: no multiplication will even change that result !
					logger.trace("reached p=0, stopping there");
					break;
				} 
			
			}
					
			//resNonCond = resNonCond.add(pUsGivenSomething);
			resCond = resCond.add(pUsGivenSomething.multiply(pSomething));
			logger.trace("the probability p({}={}|*) is now after addition {}", name, att, resCond);

			/*
			if (resCond.compareTo(BigDecimal.ONE) == 0) {
				// we can even break that loop: the probability will never become greater than 1!
				logger.trace("reached p=1, stopping there");
				break;
			} 
		 */
			// climb upwards until we find a place where we can increase the index
			logger.trace("initial cursor parents {} idxParents {}", cursorParents, idxParents);
			
			
			/*
			 * idxParents [3 2]  cursor parents 1
			 * idxParents [2 2] cursor parents 1
			 * idxParents [1 2] cursor parents 1
			 * idxParents [0 2] cursor parents 1
			 * idxPraents [-1 2] cursor parents 1 !!!
			 * idxParents [3 1]
			 * [2 1]
			 * [1 1]
			 * [0 1]
			 * [-1 1] !!!
			 * [3 0] 
			 * [2 0]
			 * [1 0]
			 * [0 0]
			 * [-1 0] !!!
			 * [-1 -1] !!
			 */
			
			// shift to the lower value for the current parent domain
			//idxParents[cursorParents]--;
			for (int p=0; p<idxParents.length; p++) {
				
				// do not change if locked
				if (forcedValue.containsKey(parentsArray[p]))
					continue;

				idxParents[p] --;
				if (idxParents[p] < 0)
					idxParents[p] = parentsArray[p].getDomainSize()-1;
				else 
					break;
			}
			if (idxParents[0] < 0)
				break;
		}
	
		logger.debug("computed posteriors for p({}={}|{})={}", name, att, evidence, resCond);

		Map<String,BigDecimal> v2p = alreadyComputed.get(this);
		if (v2p == null) {
			v2p = new HashMap<>();
			try {
				alreadyComputed.put(this, v2p);
			} catch (UnsupportedOperationException e) {
				// ignore it; probably we were called with an empty map
			}
		}
		v2p.put(att, resCond);
		
		return resCond;
		
	}
	
	
	public BigDecimal getConditionalProbabilityPosterior(String att) {
		
		return getConditionalProbabilityPosterior(
				att, 
				Collections.emptyMap(), 
				Collections.emptyMap(), 
				Collections.emptyMap()
				);
		
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
				
		// every single probability in our probas is non null and in [0:1]
		for (int i=0; i<content.length; i++) {
			if ( content[i] == null || (content[i].compareTo(BigDecimal.ZERO) < 0) || (content[i].compareTo(BigDecimal.ONE) > 0)) 
				return false;
		}
		
		// each conditional probability in our domain is summing to 1 for each combination of parents values
		for (String v: domain) {
			BigDecimal post = getConditionalProbability(v);
			if (post.setScale(6, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(getParentsDimensionality()).setScale(6)) == 1)
				return false;
			sumConditionals = sumConditionals.add(post);
		}
		
		// TODO check the conditionals sum to 1 conditionnaly !
		
		return sumConditionals.setScale(6,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(getParentsCardinality()).setScale(6))==0;
	}
	
	/**
	 * lists the problems identified during the validation, or null if no problem.
	 * @return
	 */
	public List<String> collectInvalidityReasons() {

		BigDecimal sum = getSum();
		if (sum.setScale(6,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(getParentsDimensionality()).setScale(6))!=0) {
			List<String> res = new LinkedList<>();
			res.add("invalid sum: "+sum);
			return res;
		}
			
		return null;
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
			sb.append(p.getName());

		}
		sb.append(")");
		return sb.toString();
	}

	
}
