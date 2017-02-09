package spin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.apache.poi.util.SystemOutLogger;

public class TestEtc {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Hashtable<String, ArrayList<Double>> blabla = new Hashtable<String, ArrayList<Double>>();
		
		ArrayList<Double> bro = new ArrayList<Double>(Arrays.asList(2.1, 4.0));
		

		blabla.put("One", new ArrayList<Double>(Arrays.asList(2.1, 4.0)));
		
		blabla.put("Two", new ArrayList<Double>(Arrays.asList(3.3, 5.0, 2.1)));
		

		blabla.put(
				"three", new ArrayList<Double>(Arrays.asList(2.30, 3.3))
				);
		

		blabla.put(
				"four", new ArrayList<Double>(Arrays.asList(4.0, 2.1, 2.3))
				);
		

		System.out.println(blabla);

		blabla.values().stream()
			.flatMap(f -> f.stream())
			.distinct()
			.sorted()
			.forEach(System.out::println);
		
	}

}
