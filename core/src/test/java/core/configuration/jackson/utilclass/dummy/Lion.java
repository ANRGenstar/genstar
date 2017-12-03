package core.configuration.jackson.utilclass.dummy;

public class Lion implements IAnimal {

	private String name;
	public Lion(String name) {this.setName(name);}
	@Override public String getName() {return name;}
	@Override public void setName(String name) {this.name = name;}
}
