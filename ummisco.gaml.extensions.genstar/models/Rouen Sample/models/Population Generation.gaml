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
	{
		create people from: xml_file("../includes/GSC_RouenIndividual.xml") number: 100000 with: [age::read("Age")];

	}

}

species people
{
	int age;
}

experiment creation;

