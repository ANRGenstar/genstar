package gospl.sampler.sr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import core.util.random.GenstarRandom;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.sampler.IDistributionSampler;

/**
 * Sample method to draw from a discrete distribution, based on binary search algorithm
 * <p> 
 * 
 * FIXME: 
 * <ul> 
 *  <li> method reset sampler
 *  <li> junit test because it is not robust at all
 * </ul>
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public class GosplBinarySampler implements IDistributionSampler {
		
	private List<ACoordinate<Attribute<? extends IValue>, IValue>> keys;
	private List<Double> sop;
	
	private final double EPSILON = Math.pow(10, -6);
	
	private Logger log = LogManager.getLogger();
	private final static Level LEVEL = Level.TRACE;
	
	// -------------------- setup methods -------------------- //
	

	@Override
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution){
		if(distribution == null)
			throw new NullPointerException();
		if(distribution.getMatrix().isEmpty())
			throw new IllegalArgumentException("Cannot setup a sampler with an empty distribution matrix "+distribution);
		
		int size = distribution.size();
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Setup binary sample of size: "+
				size, log, LEVEL);
		gspu.sysoStempPerformance(0, this);
		this.keys = new ArrayList<>(distribution.getMatrix().keySet());
		this.sop = new ArrayList<>(size);
		double sumOfProbabilities = 0d;
		int count = 1;
		for(ACoordinate<Attribute<? extends IValue>, IValue> key : keys){
			sumOfProbabilities += distribution.getVal(key).getValue();
			sop.add(sumOfProbabilities);
			if(size > 10 && count++ % (size / 10) == 0)
				gspu.sysoStempPerformance(count * 1d / size, this);
		}
		if(Math.abs(sumOfProbabilities - 1d) > EPSILON)
			throw new IllegalArgumentException("Sum of probabilities for this sampler exceed 1 (SOP = "+sumOfProbabilities+")");
	}

	// -------------------- main contract -------------------- //
		
	@Override
	public ACoordinate<Attribute<? extends IValue>, IValue> draw(){
		
		int count = 0;
		
		double rand = GenstarRandom.getInstance().nextDouble();
		int floor = 0;
		int top = keys.size() - 1;
		int mid;
		while(floor <= top){
			
			// MIDDLE IS AN INTERVAL
			mid = (floor + top) / 2;
			double lowMid = mid == 0 ? 0.0 : sop.get(mid-1);
			double highMid = sop.get(mid);
			
			if(rand >= lowMid && rand < highMid) return keys.get(mid);
			if(rand < highMid) top = mid - 1;
			if(rand >= highMid) floor = mid + 1;
			
			if(count++ > keys.size())
				throw new RuntimeException("Infinity loop: floor = "+floor+" | top = "+top+" | mid = "+mid
						+"\nRand = "+rand+" | mid range = ["+lowMid+";"+highMid+"] "
								+ "\n next key = "+sop.get(mid+1)+" | previous key = "+sop.get(mid - 2 < 0 ? 0 : mid - 2));
			
		}
		
		throw new RuntimeException("Sample engine has not been able to draw one coordinate !!!\n"
				+ "random ("+rand+"), floor ("+floor+" = "+sop.get(floor)+") and top ("+top+" = "+sop.get(top)+") could not draw index\n"
						+ "befor floor is: "+sop.get(floor-1));
						
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final Collection<ACoordinate<Attribute<? extends IValue>, IValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).mapToObj(i -> draw()).collect(Collectors.toList());
	}
		
	
	// -------------------- utility -------------------- //
	
	@Override
	public String toCsv(String csvSeparator){
		List<Attribute<? extends IValue>> attributs = new ArrayList<>(keys
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = String.join(csvSeparator, attributs.stream().map(att -> att.getAttributeName()).collect(Collectors.toList()));
		s += "; Probability\n";
		double formerProba = 0d;
		for(ACoordinate<Attribute<? extends IValue>, IValue> coord : keys){
			String line = "";
			for(Attribute<? extends IValue> att : attributs){
				if(coord.getDimensions().contains(att)){
					if(line.isEmpty())
						line += coord.getMap().get(att).getStringValue();
					else
						line += csvSeparator + coord.getMap().get(att).getStringValue();
				} else {
					if(line.isEmpty())
						line += " ";
					else
						line += csvSeparator + " ";
				}
			}
			double actualProba = sop.get(keys.indexOf(coord)) - formerProba; 
			formerProba = sop.get(keys.indexOf(coord));
			s += line + csvSeparator + actualProba +"\n";
		}
		return s;
	}
	
}
