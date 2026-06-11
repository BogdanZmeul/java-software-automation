import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"fast", "slow", "system"})
@SelectClasses(UserFormProcessorTest.class)
public class FullValidationSuite {
}