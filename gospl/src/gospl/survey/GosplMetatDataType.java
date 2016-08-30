package gospl.survey;

/**
 * This {@link Enum} represents the types of data a synthetic population generation could input. Each refer to 
 * specific form of data: 
 * 
 * <p><ul>
 * <li> {@link GosplMetatDataType#Sample}: individual data <p> e.g. indiv1, age = 22, sex = female ..., indiv2, age = 56, sex = male ..., etc.
 * <li> {@link GosplMetatDataType#ContingencyTable}: contingent of a category of individual <p> e.g. age(22) = 35968 individuals 
 * <li> {@link GosplMetatDataType#IndivFrequencyTable}: proportion of category related to one referent category <p> e.g. age(22) = 49% / 51% of male / female
 * <li> {@link GosplMetatDataType#CompletFrequencyTable}: proportion of a category of individual <p> e.g. age(22) = 12% of the population
 * </ul><p>
 * 
 * @author kevinchapuis
 *
 */
public enum GosplMetatDataType {

	Sample,
	ContingencyTable,
	IndivFrequencyTable,
	CompletFrequencyTable;

}
