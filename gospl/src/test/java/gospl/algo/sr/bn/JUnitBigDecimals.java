package gospl.algo.sr.bn;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

/**
 * import with import static gospl.algo.bn.JUnitBigDecimals.assertEqualsBD;


 * @author Samuel Thiriot
 *
 */
public class JUnitBigDecimals {


	public static void assertEqualsBD(double ref, BigDecimal d, int precision) {
		System.out.println("comparing "+ref+" with "+d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue()+" prec "+Math.pow(10, -precision));
		assertEquals(
				ref,
				d.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue(),
				Math.pow(10, -precision)
				);
	}
	
	private JUnitBigDecimals() {}

}
