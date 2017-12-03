package core.configuration.jackson.utilclass;

import java.util.ArrayList;
import java.util.Collection;

public class Dico<A extends IAtt<? extends IVal>> {

	Collection<A> theCollection;
	
	public Dico() {
		theCollection = new ArrayList<>();
	}
	
	public A add(A att) {
		if(theCollection.add(att))
			return att;
		return null;
	}
	
	public Collection<A> getTheCollection(){
		return theCollection;
	}
	
	public void setTheCollection(Collection<A> theCollection) {
		this.theCollection = theCollection;
	}
	
}
