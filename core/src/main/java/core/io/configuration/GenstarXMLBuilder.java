package core.io.configuration;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import core.io.geo.IGSGeofile;
import core.io.geo.ShapeFile;
import core.io.survey.IGSSurvey;
import core.metamodel.IAttribute;
import core.metamodel.IValue;

/**
 * TODO: build a proper GenstarXML congiguration file builder
 * 
 * @author kevinchapuis
 *
 */
public class GenstarXMLBuilder {

	private DocumentBuilder docBuilder;

	private Document doc;
	private String pathToFile;

	public void writeGenstarXML() throws ParserConfigurationException, 
	TransformerFactoryConfigurationError, TransformerException {
		//root elements
		docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = docBuilder.newDocument();

		//write the content into xml file
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMSource source = new DOMSource(doc);

		StreamResult result =  new StreamResult(new File(pathToFile));
		transformer.transform(source, result);
	}


	// ----------------------------------------------------------- //
	// ---------------- NEW CONFIGURATION ELEMENT ---------------- //
	// ----------------------------------------------------------- //


	public boolean addGenstarAttribute(IAttribute<? extends IValue> attribute){
		// TODO
		return true;
	}

	public boolean addSurveyFile(IGSSurvey survey){
		// TODO
		return true;
	}

	public boolean addMainGeoFile(ShapeFile mainGeofile){
		// TODO
		return true;
	}

	public boolean addAncillaryGeoFile(IGSGeofile... ancillaryGeofile){
		// TODO
		return true;
	}

//	Element rootElement = doc.createElement("company");
//	doc.appendChild(rootElement);
//
//	//staff elements
//	Element staff = doc.createElement("Staff");
//	rootElement.appendChild(staff);
//
//	//set attribute to staff element
//	Attr attr = doc.createAttribute("id");
//	attr.setValue("1");
//	staff.setAttributeNode(attr);
//
//	//shorten way
//	//staff.setAttribute("id", "1");
//
//	//firstname elements
//	Element firstname = doc.createElement("firstname");
//	firstname.appendChild(doc.createTextNode("yong"));
//	staff.appendChild(firstname);
//
//	//lastname elements
//	Element lastname = doc.createElement("lastname");
//	lastname.appendChild(doc.createTextNode("mook kim"));
//	staff.appendChild(lastname);
//
//	//nickname elements
//	Element nickname = doc.createElement("nickname");
//	nickname.appendChild(doc.createTextNode("mkyong"));
//	staff.appendChild(nickname);
//
//	//salary elements
//	Element salary = doc.createElement("salary");
//	salary.appendChild(doc.createTextNode("100000"));
//	staff.appendChild(salary);

}
