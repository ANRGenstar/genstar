package spin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import spin.generator.RandomNetworkGeneratorTest;
import spin.objects.SpinNetworkTest;

@RunWith(Suite.class)
@SuiteClasses({RandomNetworkGeneratorTest.class,SpinNetworkTest.class})
public class AllTests {

}
