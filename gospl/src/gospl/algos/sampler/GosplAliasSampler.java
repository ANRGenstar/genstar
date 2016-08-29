package gospl.algos.sampler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gospl.distribution.INDimensionalMatrix;
import gospl.distribution.coordinate.ACoordinate;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;

/******************************************************************************
 * File: AliasMethod.java
 * Author: Keith Schwarz (htiek@cs.stanford.edu)
 *
 * An implementation of the alias method implemented using Vose's algorithm.
 * The alias method allows for efficient sampling of random values from a
 * discrete probability distribution (i.e. rolling a loaded die) in O(1) time
 * each after O(n) preprocessing time.
 *
 * For a complete writeup on the alias method, including the intuition and
 * important proofs, please see the article "Darts, Dice, and Coins: Smpling
 * from a Discrete Distribution" at
 *
 *                 http://www.keithschwarz.com/darts-dice-coins/
 * 
 */
public class GosplAliasSampler implements ISampler<ACoordinate<IAttribute, IValue>> {

	private final List<ACoordinate<IAttribute, IValue>> indexedKey;
	private final List<Double> initProba;
	
	/* The random number generator used to sample from the distribution. */
	private final Random random;

	/* The probability and alias tables. */
	private final int[] alias;
	private final double[] probability;

	/**
	 * Constructs a new AliasMethod to sample from a discrete distribution and
	 * hand back outcomes based on the probability distribution.
	 * <p>
	 * Given as input a list of probabilities corresponding to outcomes 0, 1,
	 * ..., n - 1, this constructor creates the probability and alias tables
	 * needed to efficiently sample from this distribution.
	 *
	 * @param probabilities The list of probabilities.
	 */
	protected GosplAliasSampler(INDimensionalMatrix<IAttribute, IValue, Double> distribution) {
		this(distribution, ThreadLocalRandom.current());
	}

	/**
	 * Constructs a new AliasMethod to sample from a discrete distribution and
	 * hand back outcomes based on the probability distribution.
	 * <p>
	 * Given as input a list of probabilities corresponding to outcomes 0, 1,
	 * ..., n - 1, along with the random number generator that should be used
	 * as the underlying generator, this constructor creates the probability 
	 * and alias tables needed to efficiently sample from this distribution.
	 *
	 * @param probabilities The list of probabilities.
	 * @param indexedKey 
	 * @param random The random number generator
	 * @param the upper bound of probabilities (in maths always 1 but for floating point issues should be scale up when distribution is huge)
	 */
	protected GosplAliasSampler(INDimensionalMatrix<IAttribute, IValue, Double> distribution, Random random) {
		
		if(distribution == null)
			throw new NullPointerException();
		if(distribution.isEmpty())
			throw new IllegalArgumentException("Probability vector must be nonempty.");
		
		Map<ACoordinate<IAttribute, IValue>, Double> sortedMap = distribution.getMatrix().entrySet()
				.parallelStream().sorted(Map.Entry.<ACoordinate<IAttribute, IValue>, Double>comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
		this.indexedKey = new ArrayList<>(sortedMap.keySet());
		this.initProba = new ArrayList<>(sortedMap.values());
		
		/* Allocate space for the probability and alias tables. */
		probability = new double[distribution.size()];
		alias = new int[distribution.size()];

		/* Store the underlying generator. */
		this.random = random;

		/* Compute the average probability and cache it for later use. */
		final double average = 1.0 / distribution.size();

		/* Make a copy of the probabilities list, since we will be making
		 * changes to it.
		 */
		List<Double> probabilities = new ArrayList<Double>(initProba);

		/* Create two stacks to act as worklists as we populate the tables. */
		Deque<Integer> small = new ArrayDeque<Integer>();
		Deque<Integer> large = new ArrayDeque<Integer>();

		/* Populate the stacks with the input probabilities. */
		for (int i = 0; i < probabilities.size(); ++i) {
			/* If the probability is below the average probability, then we add
			 * it to the small list; otherwise we add it to the large list.
			 */
			if (probabilities.get(i) >= average)
				large.add(i);
			else
				small.add(i);
		}

		/* As a note: in the mathematical specification of the algorithm, we
		 * will always exhaust the small list before the big list.  However,
		 * due to floating point inaccuracies, this is not necessarily true.
		 * Consequently, this inner loop (which tries to pair small and large
		 * elements) will have to check that both lists aren't empty.
		 */
		while (!small.isEmpty() && !large.isEmpty()) {
			/* Get the index of the small and the large probabilities. */
			int less = small.removeLast();
			int more = large.removeLast();

			/* These probabilities have not yet been scaled up to be such that
			 * 1/n is given weight 1.0.  We do this here instead.
			 */
			probability[less] = probabilities.get(less) * probabilities.size();
			alias[less] = more;

			/* Decrease the probability of the larger one by the appropriate
			 * amount.
			 */
			probabilities.set(more, 
					(probabilities.get(more) + probabilities.get(less)) - average);

			/* If the new probability is less than the average, add it into the
			 * small list; otherwise add it to the large list.
			 */
			if (probabilities.get(more) >= 1.0 / probabilities.size())
				large.add(more);
			else
				small.add(more);
		}

		/* At this point, everything is in one list, which means that the
		 * remaining probabilities should all be 1/n.  Based on this, set them
		 * appropriately.  Due to numerical issues, we can't be sure which
		 * stack will hold the entries, so we empty both.
		 */
		while (!small.isEmpty())
			probability[small.removeLast()] = 1.0;
		while (!large.isEmpty())
			probability[large.removeLast()] = 1.0;
	}

	/**
	 * Samples a value from the underlying distribution.
	 *
	 * @return A random value sampled from the underlying distribution.
	 */
	@Override
	public ACoordinate<IAttribute, IValue> draw() {
		/* Generate a fair die roll to determine which column to inspect. */
		int column = random.nextInt(probability.length);

		/* Generate a biased coin toss to determine which option to pick. */
		boolean coinToss = random.nextDouble() < probability[column];
		
		return indexedKey.get(coinToss ? column : alias[column]);
	}
	
	@Override
	public List<ACoordinate<IAttribute, IValue>> draw(int numberOfDraw){
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}
	
	@Override
	public String toCsv(String csvSeparator){
		List<IAttribute> attributs = new ArrayList<>(indexedKey
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = String.join(csvSeparator, attributs.stream().map(att -> att.getName()).collect(Collectors.toList()));
		s += "; Probability\n";
		for(ACoordinate<IAttribute, IValue> coord : indexedKey){
			String line = "";
			for(IAttribute att : attributs){
				if(coord.getDimensions().contains(att)){
					if(line.isEmpty())
						s += csvSeparator+coord.getMap().get(att);
					else
						s += csvSeparator+coord.getMap().get(att).getInputStringValue();
				} else {
					if(line.isEmpty())
						s += " ";
					else
						s += csvSeparator+" ";
				}
			}
			s += line + csvSeparator + initProba.get(indexedKey.indexOf(coord))+"\n";
		}
		return s;
	}
}

