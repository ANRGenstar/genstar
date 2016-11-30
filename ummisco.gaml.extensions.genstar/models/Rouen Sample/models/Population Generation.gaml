/**
* Name: PopulationGeneration
* Author: Alexis Drogoul
* Description: Shows how to generate a simple population of agents from a Genstar configuration file
* Tags: Population generation
*/
model PopulationGeneration


global
{
	init
	{float t <- machine_time;
//		create people  number: 1000000;
//			write string(machine_time - t) + " ms";
//		ask people{
//			do die;
//		}
//		 t <- machine_time;
		create people from: xml_file("../includes/GSC_RouenIndividual.xml") number: 1000000 with: [sex::read("Sexe"),age::read("Age"), csp::read("CSP")];
		write string(machine_time - t) + " ms";
//		ask people {
//			write string(self) + " age: " + age + " / csp: " + csp + "/ sex: " + sex;
//		}
	}

}

species people
{
	int age;
	string csp;
	string sex;
}

experiment creation;

