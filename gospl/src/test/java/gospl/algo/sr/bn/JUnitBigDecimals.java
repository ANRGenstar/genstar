package gospl.algo.sr.bn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

/**
 * import with import static gospl.algo.bn.JUnitBigDecimals.assertEqualsBD;


 * @author Samuel Thiriot
 *
 */
public class JUnitBigDecimals {


	public static void assertEqualsBD(String message, double ref, BigDecimal d, int precision) {
		
		assertNotNull("the inference engine returned no result", d);
		
		System.out.println("comparing "+ref+" with "+d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue()+" prec "+Math.pow(10, -precision));
		assertEquals(
				message,
				ref,
				d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue(),
				Math.pow(10, -precision)
				);
	}
	
	public static void assertEqualsBD(double ref, BigDecimal d, int precision) {
		System.out.println("comparing "+ref+" with "+d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue()+" prec "+Math.pow(10, -precision));
		assertEquals(
				ref,
				d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue(),
				Math.pow(10, -precision)
				);
	}
	

	public static void assertEqualsBD(BigDecimal ref, BigDecimal d, int precision) {
		System.out.println("comparing "+ref.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue()+" with "+d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue()+" prec "+Math.pow(10, -precision));
		assertEquals(
				ref.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue(),
				d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue(),
				Math.pow(10, -precision)
				);
	}
	

	public static void assertEqualsBD(String message, BigDecimal ref, BigDecimal d, int precision) {
		System.out.println("comparing "+ref.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue()+" with "+d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue()+" prec "+Math.pow(10, -precision));
		assertEquals(
				message,
				ref.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue(),
				d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue(),
				Math.pow(10, -precision)
				);
	}
	
	private JUnitBigDecimals() {}

}
