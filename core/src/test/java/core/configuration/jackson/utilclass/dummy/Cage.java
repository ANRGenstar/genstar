package core.configuration.jackson.utilclass.dummy;

import java.util.ArrayList;
import java.util.Collection;

public class Cage<A extends IAnimal> {

	private Collection<A> animals;
	public Cage() {this.animals = new ArrayList<>();}
	public Collection<A> getAnimals() {return animals;}
	public void setAnimals(Collection<A> animals) {this.animals = animals;}

}
