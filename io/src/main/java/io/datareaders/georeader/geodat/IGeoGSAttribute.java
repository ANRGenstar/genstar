package io.datareaders.georeader.geodat;

import java.util.Collection;

public interface IGeoGSAttribute<A, D> {

	public Collection<A> getProperties();
	
	public D getValue(A attribute);
	
	public String getGenstarName();
	
	public GSFeature transposeToGenstarFeature();
	
}
