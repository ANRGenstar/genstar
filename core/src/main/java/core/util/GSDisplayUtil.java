package core.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GSDisplayUtil {
	
	public static final String ELEMENT_SEPARATOR = ", ";
	
	public static String prettyPrint(final Collection<?> theCollection, String elementSeparator) {
		return prettyPrint(theCollection, elementSeparator, 0);
	}
	
	public static String prettyPrint(final Collection<?> theCollection, String elementSeparator, int numberOfEntry) {
		
		Class<?> clazz = theCollection.getClass();
		numberOfEntry = numberOfEntry <= 0 ? theCollection.size() : numberOfEntry;  
		
		switch (clazz.getCanonicalName()) {
		case "List":
			return prettyPrintList((List<?>) theCollection, elementSeparator, numberOfEntry);
		case "Map":
			return prettyPrintMap((Map<?,?>)theCollection, elementSeparator, numberOfEntry);
		case "Set":
			return prettyPrintSet((Set<?>)theCollection, elementSeparator, numberOfEntry);
		default:
			return theCollection.stream()
					.limit(numberOfEntry)
					.map(e -> e.toString())
					.collect(Collectors.joining(elementSeparator));
		}
		
	}
	
	public static String prettyPrintList(final List<?> theList, String elementSeparator, int numberOfEntry) {
		
		return theList.stream()
				.limit(numberOfEntry)
				.map(e -> e.toString())
				.collect(Collectors.joining(elementSeparator));
		
	}
	
	public static String prettyPrintMap(final Map<?,?> theMap, String elementSeparator, int numberOfEntry) {
		
		return theMap.entrySet().stream()
				.limit(numberOfEntry)
				.map(e -> '"'+e.getKey().toString()+"="+e.getValue().toString()+'"')
				.collect(Collectors.joining(elementSeparator));
		
	}
	
	public static String prettyPrintSet(final Set<?> theSet, String elementSeparator, int numberOfEntry) {
		
		return theSet.stream()
				.limit(numberOfEntry)
				.map(e -> e.toString())
				.collect(Collectors.joining(elementSeparator));
		
		
	}
	
}
