package core.util.random;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class TestRandomUtils {

	private Logger logger = LogManager.getLogger();
	
	@Test
	public void testOneofList() {
		List<Integer> l = Arrays.asList(1,2,3,4,10,11,42,111);
		// count occurences of values
		Map<Integer,Integer> value2freq = new HashMap<>(l.size());
		for (Integer v: l) {
			value2freq.put(v, 0);
		}
		// draw many samples
		for (int i=0;i<10000;i++) {
			Integer found = GenstarRandomUtils.oneOf(l);
			value2freq.put(found,value2freq.get(found)+1);
		}
		logger.info("for list {}, got {}", l, value2freq);
		
		// TODO check distribution
		fail("Not yet implemented");
	}

	@Test
	public void testOneofSet() {
		Set<Integer> s = new HashSet<>(Arrays.asList(1,2,3,4,10,11,42,111));
		// count occurences of values
		Map<Integer,Integer> value2freq = new HashMap<>(s.size());
		for (Integer v: s) {
			value2freq.put(v, 0);
		}
		// draw many samples
		for (int i=0;i<10000;i++) {
			Integer found = GenstarRandomUtils.oneOf(s);
			value2freq.put(found,value2freq.get(found)+1);
		}
		logger.info("for set {}, got {}", s, value2freq);
		
		// TODO check distribution
		fail("Not yet implemented");
	}

	
}
