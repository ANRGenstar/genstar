package io.datareaders.georeader.geodat;

import java.util.List;

public interface IGeoGSAttributes<A, D> {

	public List<A> getData();
	
	public D getValue(A attribute);
	
	public String getGenstarName();
	
	public GSFeature transposeToGenstarFeature();
	
}
