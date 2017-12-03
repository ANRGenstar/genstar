package core.configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Main utility class to go from java object to JSON formated file and the way back
 * 
 * @author kevinchapuis
 *
 */
public class GenstarJsonUtil {

	public static final String BASE_DIR = "MAIN DIRECTORY";
	
	public final static String INPUT_FILES = "INPUT FILES";
	
	public final static String DEMO_DICO = "DEMOGRAPHIC DICTIONARY";
	public static final String DEMO_RECORDS = "DEMOGRAPHIC RECORDS";
	
	public final static String GENSTAR_EXT = "gns";
	
	private final ObjectMapper om;
	
	/**
	 * Default inner Jackson {@link ObjectMapper}
	 */
	public GenstarJsonUtil() {
		this(Arrays.asList(SerializationFeature.INDENT_OUTPUT), Collections.emptyList());
	}
	
	/**
	 * User configured inner Jackson {@link ObjectMapper}. Each provided feature
	 * will turn default feature configuration to not-default (if true, will be false and reverse)
	 * 
	 * @param serializationFeatures
	 * @param deserializationFeatures
	 */
	public GenstarJsonUtil(List<SerializationFeature> serializationFeatures,
			List<DeserializationFeature> deserializationFeatures) {
		this.om = new ObjectMapper();
		om.registerModule(new SimpleModule().addSerializer(Path.class, new ToStringSerializer()));
		serializationFeatures.stream().forEach(feat -> om.configure(feat, 
				!om.getSerializationConfig().isEnabled(feat)));
		deserializationFeatures.stream().forEach(feat -> om.configure(feat, 
				!om.getDeserializationConfig().isEnabled(feat)));
	}
	
	// ---------------------------------------------- //
	
	/**
	 * Write down a java object to a JSON Gen* formated file
	 * 
	 * @param toFile
	 * @param targetToMarshal
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void marshalToGenstarJson(Path toFile, Object targetToMarshal, 
			SerializationFeature... features) 
			throws JsonGenerationException, JsonMappingException, IOException { 
		Stream.of(features).forEach(f -> om.enable(f)); 
		om.writeValue(toFile.toFile(), targetToMarshal);
	}
	
	/**
	 * Write down a java object to a JSON formated String
	 * 
	 * @param target
	 * @param features
	 * @return
	 * @throws JsonProcessingException
	 */
	public String genstarJsonToString(Object target, SerializationFeature... features) 
			throws JsonProcessingException {
		Stream.of(features).forEach(f -> om.enable(f));
		return om.writeValueAsString(target);
	}

	/**
	 * Convert a JSON Gen* formated file to a Gen* specific java object 
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public <T> T unmarshalFromGenstarJson(Path json, Class<T> clazz) 
			throws IOException, IllegalArgumentException {
		if(!json.toString().endsWith(GENSTAR_EXT))
			throw new IllegalArgumentException("The file "+json.toFile().getName()+" is not a \"gns\" file");
		return om.readerFor(clazz).readValue(json.toFile());
	} 
	
	// ------------------ STATIC UTILITIES ------------------ //
	
	/**
	 * Pretty print of JsonNode internal fields
	 * @param node
	 * @return
	 */
	public static String getJsonNodeContentToString(JsonNode node) {
		Map<String, JsonNode> innerNodes = new HashMap<>();
		node.fields().forEachRemaining(e -> innerNodes.put(e.getKey(), e.getValue()));
		return innerNodes.entrySet().stream().map(e -> e.getKey()
				+" ["+ e.getValue().getNodeType().toString() +"]"
				+" is null ? "+e.getValue().isNull())
				.collect(Collectors.joining("\n"));
	}
}
