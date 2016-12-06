package gospl.algo.sampler.sr;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.random.GenstarRandom;
import gospl.algo.sampler.IDistributionSampler;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

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
public class GosplAliasSampler implements IDistributionSampler {

	protected Logger logger = LogManager.getLogger();
	
	private List<ACoordinate<APopulationAttribute, APopulationValue>> indexedKey;
	private List<Double> initProba;
	

	/* The probability and alias tables. */
	private int[] alias;
	private double[] probability;

	// -------------------- setup methods -------------------- //

	@Override
	public void setDistribution(GosplBasicDistribution distribution){
		if(distribution == null)
			throw new NullPointerException();
		if(distribution.isEmpty())
			throw new IllegalArgumentException("Probability vector must be nonempty.");
		
		this.indexedKey = new ArrayList<>(distribution.keySet());
		this.initProba = new ArrayList<>(distribution.values());
		
		/* Allocate space for the probability and alias tables. */
		probability = new double[distribution.size()];
		alias = new int[distribution.size()];

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

	@Override
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution) {
		this.setDistribution(new GosplBasicDistribution(distribution));
	}
	

	// -------------------- main contract -------------------- //

	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final List<ACoordinate<APopulationAttribute, APopulationValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 *
	 * @return A random value sampled from the underlying distribution.
	 */
	@Override
	public ACoordinate<APopulationAttribute, APopulationValue> draw() {
		/* Generate a fair die roll to determine which column to inspect. */
		int column =  GenstarRandom.getInstance().nextInt(probability.length);

		/* Generate a biased coin toss to determine which option to pick. */
		boolean coinToss = GenstarRandom.getInstance().nextDouble() < probability[column];
		
		return indexedKey.get(coinToss ? column : alias[column]);
	}
	
	
	@Override
	public String toCsv(String csvSeparator){
		List<APopulationAttribute> attributs = new ArrayList<>(indexedKey
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = String.join(csvSeparator, attributs.stream().map(att -> att.getAttributeName()).collect(Collectors.toList()));
		s += "; Probability\n";
		for(ACoordinate<APopulationAttribute, APopulationValue> coord : indexedKey){
			String line = "";
			for(APopulationAttribute att : attributs){
				if(coord.getDimensions().contains(att)){
					if(line.isEmpty())
						s += csvSeparator+coord.getMap().get(att);
					else
						s += csvSeparator+coord.getMap().get(att).getStringValue();
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

