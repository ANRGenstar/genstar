package spll.algo.variable;

import org.opengis.feature.Feature;

public class SPLAreaMatcher implements ISPLVariableFeatureMatcher<Feature, SPLRawVariable, Double> {

	private double area = 0d;
	
	private final SPLRawVariable variable;

	private Feature feature;
	
	protected SPLAreaMatcher(Feature feat, SPLRawVariable variable){
		this.feature = feat;
		this.variable = variable;
	}
	
	public void setArea(double area){
		this.area = area;
	}
	
	@Override
	public Double getValue(){
		return area;
	}
	
	@Override
	public SPLRawVariable getVariable(){
		return variable;
	}

	@Override
	public String getName() {
		return variable.getName();
	}

	@Override
	public Feature getFeature() {
		return feature;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(area);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((feature == null) ? 0 : feature.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SPLAreaMatcher other = (SPLAreaMatcher) obj;
		if (Double.doubleToLongBits(area) != Double.doubleToLongBits(other.area))
			return false;
		if (feature == null) {
			if (other.feature != null)
				return false;
		} else if (!feature.equals(other.feature))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}
	
	
	
}
