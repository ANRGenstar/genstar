package spll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.opengis.referencing.operation.TransformException;

import core.configuration.dictionary.DemographicDictionary;
import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.generator.util.GSUtilGenerator;
import spll.io.SPLGeofileBuilder;
import spll.io.SPLVectorFile;
import spll.io.exception.InvalidGeoFormatException;

public class SpllSetupTest {

	public IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> pop;
	public IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popBig;
	public SPLVectorFile sfAdmin;
	public SPLVectorFile sfBuildings;
	public SPLVectorFile sfRoads;
	public List<IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue>> endogeneousVarFile;
	
	@SuppressWarnings("unchecked")
	public SpllSetupTest() {
		DemographicDictionary<DemographicAttribute<? extends IValue>> atts = new DemographicDictionary<>();
		try {
			atts.addAttributes(DemographicAttributeFactory.getFactory()
					.createAttribute("iris", GSEnumDataType.Nominal, Arrays.asList("765400102", "765400101")));
		} catch (GSIllegalRangedData e1) {
			e1.printStackTrace();
		}
		
		GSUtilGenerator ug = new GSUtilGenerator(atts);
				
		pop = ug.generate(50);
		popBig = ug.generate(5000);
		
		try {
			sfBuildings = SPLGeofileBuilder.getShapeFile(new File("src/test/resources/buildings.shp"), Arrays.asList("name", "type"), null);
			sfAdmin = SPLGeofileBuilder.getShapeFile(new File("src/test/resources/irisR.shp"), null);
			sfRoads = SPLGeofileBuilder.getShapeFile(new File("src/test/resources/roads.shp"), null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidGeoFormatException e) {
			e.printStackTrace();
		}
		Collection<String> stringPathToAncilaryGeofiles = new ArrayList<>();
		stringPathToAncilaryGeofiles.add("src/test/resources/CLC12_D076_RGF_S.tif");
		endogeneousVarFile = new ArrayList<>();
		for(String path : stringPathToAncilaryGeofiles){
			try {
				endogeneousVarFile.add(new SPLGeofileBuilder().setFile(new File(path)).buildGeofile());
			} catch (IllegalArgumentException | TransformException | IOException | InvalidGeoFormatException e2) {
				e2.printStackTrace();
			}
		}
	}
	
}
