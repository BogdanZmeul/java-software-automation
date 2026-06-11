import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags("fast")
@SelectClasses(UserFormProcessorTest.class)
public class FastValidationSuite {
}