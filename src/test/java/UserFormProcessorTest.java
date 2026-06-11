import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class UserFormProcessorTest {
    private final UserFormProcessor processor = new UserFormProcessor();

    @Test
    @Tag("fast")
    @DisplayName("Should successfully process valid user data")
    void testValidUserForm() {
        assertDoesNotThrow(() -> processor.processForm("john@gmail.com", "GoodPass123", 20.0),
                "Valid form data should not throw any exceptions");
    }

    @ParameterizedTest
    @ValueSource(strings = {"adfdsffds", "@sdafsdf.com", "afasfasfsa@.com", "asff@afsfa412asf"})
    @Tag("fast")
    @DisplayName("Should throw exception for various invalid emails")
    void testInvalidEmailsWithSingleParam(String invalidEmail) {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                processor.processForm(invalidEmail, "GoodPass123", 25.0)
        );
        assertEquals("Invalid email format!", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "short, Invalid password! Must be 8+ characters with letters and numbers.",
            "nouppercase1, Invalid password! Must be 8+ characters with letters and numbers.",
            "N0LETTERS, Invalid password! Must be 8+ characters with letters and numbers."
    })
    @Tag("slow")
    @DisplayName("Should throw specific exception messages for weak passwords")
    void testWeakPasswordsWithMultipleParams(String weakPassword, String expectedErrorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                processor.processForm("john@gmail.com", weakPassword, 30.0)
        );
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @TestFactory
    @Tag("fast")
    @DisplayName("Dynamic tests for invalid boundary ages")
    Stream<DynamicTest> dynamicTestsForAgeValidation() {
        List<Double> invalidAges = Arrays.asList(17.9, 15.0, 120.1, -5.0);

        return invalidAges.stream().map(age ->
                DynamicTest.dynamicTest("Testing boundary age: " + age, () -> {
                    Exception exception = assertThrows(IllegalArgumentException.class, () ->
                            processor.processForm("john@gmail.com", "GoodPass123", age)
                    );
                    assertEquals("Age must be between 18 and 120!", exception.getMessage());
                })
        );
    }

    @Test
    @Tag("system")
    @DisplayName("Execute international text validation only if system encoding is UTF-8")
    void testInternationalEncodingAssumption() {
        String fileEncoding = Charset.defaultCharset().displayName();
        assumeTrue("UTF-8".equalsIgnoreCase(fileEncoding),
                "Aborting test: Standard UTF-8 environment is required for this check.");

        assertDoesNotThrow(() ->
                processor.processForm("john@gmail.com", "ValidPass999", 21.5)
        );
    }



}
