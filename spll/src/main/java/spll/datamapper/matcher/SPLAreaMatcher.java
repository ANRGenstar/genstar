package spll.datamapper.matcher;

import spll.datamapper.variable.SPLVariable;
import spll.entity.GSFeature;

public class SPLAreaMatcher implements ISPLMatcher<SPLVariable, Double> {

	private double area;
	
	private final SPLVariable variable;

	private GSFeature feature;
	
	protected SPLAreaMatcher(GSFeature feat, SPLVariable variable){
		this(feat, variable, 1d);
	}
	
	protected SPLAreaMatcher(GSFeature feat, SPLVariable variable, double area){
		this.feature = feat;
		this.variable = variable;
		this.area = area;
	}
	
	@Override
	public boolean expandValue(Double area){
		this.area += area;
		return true;
	}
	
	@Override
	public Double getValue(){
		return area;
	}
	
	@Override
	public SPLVariable getVariable(){
		return variable;
	}

	@Override
	public String getName() {
		return variable.getName();
	}

	@Override
	public GSFeature getFeature() {
		return feature;
	}
	
	// -------------------------------------------------- //
	
	@Override
	public String toString() {
		return feature.getGenstarName()+" => ["+getVariable()+" = "+area+"]";
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
