package gospl.io.ipums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.icu.util.StringTokenizer;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.value.IValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

/**
 * A class that represent an attribute in IPUMS formated dictionary. It includes data description of attribute, 
 * i.e. name and the reference, and of value, encoded and real value
 * 
 * @author kevinchapuis
 *
 */
public class IPUMSAttribute {
	
	private Logger log = LogManager.getLogger();
	private static final Level LEVEL = Level.TRACE; 
	
	private Attribute<? extends IValue> theAttribute;
	
	private String codeName; 
	private String attName;
	private String attNameAgg;
	private List<String> rawContent;
	private List<String> rawContentAgg;
	
	private String lineSpliter = ReadIPUMSDictionaryUtils.TAB_SEPARATOR;
	public static final List<String> REGEX_CHAR = Arrays.asList("}");
	
	/**
	 * Basic constructor for IPUMS attribute
	 * 
	 * @param codeName: the name associated to attribute with encoded value
	 * @param attName: the name associated to attribute with full value 
	 * @param rawContent: the values
	 */
	public IPUMSAttribute(String codeName, String attName, List<String> rawContent) {
		this.codeName = codeName;
		this.attName = attName;
		this.rawContent = rawContent;
		try {
			this.theAttribute = this.createAttribute();
		} catch (GSIllegalRangedData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor with specified line spliter
	 * 
	 * @param codeName
	 * @param attName
	 * @param rawContent
	 * @param lineSpliter
	 */
	public IPUMSAttribute(String codeName, String attName, List<String> rawContent, String lineSpliter) {
		this.codeName = codeName;
		this.attName = attName;
		this.rawContent = rawContent;
		this.lineSpliter = lineSpliter;
		try {
			this.theAttribute = this.createAttribute();
		} catch (GSIllegalRangedData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor for aggregated attribute
	 * 
	 * @param codeNameRef : the code of the referent
	 * @param attNameRef : the name of the referent
	 * @param rawContentRef : the values (encoded & full) of referent attribute
	 * @param codeNameAgg : the code of the aggregate
	 * @param attNameAgg : the name of the aggregate
	 * @param rawContentAgg : the values (encoded & full) of aggregate attribute
	 */
	public IPUMSAttribute(String codeNameRef, String attNameRef, List<String> rawContentRef,
			String attNameAgg, List<String> rawContentAgg) {
		this.codeName = codeNameRef;
		this.attName = attNameRef;
		this.rawContent = rawContentRef;
		this.attNameAgg = attNameAgg;
		this.rawContentAgg = rawContentAgg;
		try {
			this.theAttribute = this.createAttribute();
		} catch (GSIllegalRangedData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the {@link Attribute} according to data passed in constructor: code name of the attribute, the full name of the attribute
	 * and the content lines as provided by RTF dictionary of IPUMS data
	 * 
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public Attribute<? extends IValue> getAttribute() throws GSIllegalRangedData{
		return theAttribute;
	}
	
	/**
	 * The full name of the attribute
	 * @return
	 */
	public String getName() {
		return attName;
	}
	
	/**
	 * The attribute code name
	 * @return
	 */
	public String getCode() {
		return codeName;
	}
	
	/*
	 * Main method to create attribute from constructor passed argument
	 * 
	 */
	private Attribute<? extends IValue> createAttribute() throws GSIllegalRangedData{
		
		Map<String, String> theRecord = readValues(rawContent, new HashMap<String, String>());
		AttributeFactory factory = AttributeFactory.getFactory();
		
		GSDataParser parser = new GSDataParser();
		Set<GSEnumDataType> rt = theRecord.values().stream().map(s -> parser.getValueType(s)).collect(Collectors.toSet());
		GSEnumDataType refType = rt.size() == 1 ? rt.iterator().next() : GSEnumDataType.Nominal;
		
		if(rawContentAgg == null) {
			return factory.createAttribute(codeName, refType, new ArrayList<>(theRecord.values()), theRecord);
		} else {
			
			if(rawContent.size() < rawContentAgg.size())
				throw new IllegalArgumentException("Aggregated attribute "+attName+" has more values ("+rawContentAgg.size()
					+") than detailed attribute "+attNameAgg+" ("+rawContent.size()+")");
			
			Map<String, Collection<String>> theMap = null;
			if(!refType.equals(GSEnumDataType.Order))
				theMap = readMapper(rawContent, rawContentAgg, new HashMap<>(), new Supplier<Collection<String>>() {
					@Override public Collection<String> get() { return new ArrayList<>(); }
				});
			
			switch (refType) {
				case Order: 
					LinkedHashMap<String, String> theLinkedRecord = readValues(rawContent, new LinkedHashMap<>());
					LinkedHashMap<String, List<String>> theLinkedMap = readMapper(rawContent, rawContentAgg, new LinkedHashMap<>(), 
							new Supplier<List<String>>() { 
								@Override public List<String> get() { return new ArrayList<>(); }
							}
					);
					return factory.createOrderedAggregatedAttribute(attNameAgg, 
							factory.createOrderedAttribute(attName, 
									new GSCategoricTemplate(), new ArrayList<>(theLinkedRecord.values()),
									theLinkedRecord), 
							theLinkedMap);
				case Nominal:
					return factory.createNominalAggregatedAttribute(attNameAgg, 
							factory.createNominalAttribute(attName, new GSCategoricTemplate(), theRecord), 
							theMap);
				case Range:
					return factory.createRangeAggregatedAttribute(attNameAgg,
							factory.createRangeAttribute(attName, theRecord), 
							theMap);
				default:
					throw new RuntimeException("Cannot construct an aggregated IPUMS attribute of type: "+refType);
			}
		}
		
	}
	
	/*
	 * Read the value from line based RTF data from IPUMS
	 */
	private <M extends Map<String, String>> M readValues(List<String> rawContent, M theRecord){
		
		for(String line : rawContent) {
			StringTokenizer st = new StringTokenizer(line, lineSpliter);
			if(st.countTokens() == 2) {
				theRecord.put(validateValue(st.nextToken()), validateValue(st.nextToken()));
			} else if (st.countTokens() == 1) {
				// TODO something to do with one token line parsing ????
			} else {
				throw new RuntimeException("Parsed value tokens do not match value format: "+st.toString());
			}
		}
		
		return theRecord;
	}
	
	/*
	 * Read aggregated value from line based RTF data from IPUMS
	 * WARNING: key are the referent's referent value
	 */
	private <M extends Map<String, C>, C extends Collection<String>> M readMapper(
			List<String> rawContentReferent, List<String> rawContentAggregate, 
			M theMapper, Supplier<C> collectionSupplier) {
		
		Level LocalLevel = LEVEL;
		
		Map<String, String> detailedCodes = this.readValues(rawContentReferent, new HashMap<>());
		Map<String, String> aggregatedCodes = this.readValues(rawContentAggregate, new HashMap<>());
		
		log.log(LocalLevel, "Detailed Codes: {}", detailedCodes.entrySet().stream().map(e -> e.toString())
				.collect(Collectors.joining(", ")));
		log.log(LocalLevel, "Aggregated Codes: {}", aggregatedCodes.entrySet().stream().map(e -> e.toString())
				.collect(Collectors.joining(", ")));
				
		for(String dCode : detailedCodes.keySet()) {
			String aCode = aggregatedCodes.keySet().stream()
					.filter(rCode -> dCode.startsWith(rCode))
					.findFirst().get();
			if(!theMapper.containsKey(aCode))
				theMapper.put(aCode, collectionSupplier.get());
			theMapper.get(aCode).add(dCode);	
		}
		
		return theMapper;
		
	}
	
	private String validateValue(String val) {
		for(String regex : REGEX_CHAR) {
			val = val.replace(regex, "");
		}
		return val.trim();
	}
	
	
}
