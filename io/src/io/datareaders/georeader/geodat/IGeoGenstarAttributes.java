package io.datareaders.georeader.geodat;

import java.util.List;

public interface IGeoGenstarAttributes<A, D> {

	public List<A> getData();
	
	public D getValue(A attribute);
	
	public String getGenstarName();
	
	public GenstarFeature transposeToGenstarFeature();
	
}
