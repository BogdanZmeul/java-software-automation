import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class UserFormProcessorTest {
    private UserFormProcessor processor;

    @BeforeEach
    @DisplayName("Initialize processor")
    void initProcessor() {
        processor = new UserFormProcessor();
    }

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
    @Tag("slow")
    @DisplayName("Dynamic tests for invalid boundary ages")
    Stream<DynamicTest> dynamicTestsForAgeValidation() {
        Random random = new Random();

        Stream<Double> tooYoung = random.doubles(100, -50.0, 18.0).boxed();
        Stream<Double> tooOld = random.doubles(100, 120.0001, 200.0).boxed();

        Stream<Double> allInvalidAges = Stream.concat(tooYoung, tooOld);

        return allInvalidAges.map(age ->
                DynamicTest.dynamicTest("Testing boundary age: " + age, () -> {
                    UserFormProcessor localProcessor = new UserFormProcessor();

                    Exception exception = assertThrows(IllegalArgumentException.class, () ->
                            localProcessor.processForm("john@gmail.com", "GoodPass123", age)
                    );
                    assertEquals("Age must be between 18 and 120!", exception.getMessage());
                })
        );
    }

    @Test
    @Tag("system")
    @DisplayName("Execute heavy password brute-force validation only on CI server")
    void testHeavyValidationOnlyOnCI() {
        String isCI = System.getenv("CI");
        assumeTrue("true".equalsIgnoreCase(isCI), "Skipping test testHeavyValidationOnlyOnCI");

        System.out.println("Running testHeavyValidationOnlyOnCI CI server");
        assertDoesNotThrow(() ->
                processor.processForm("admin@gmail.com", "SuperComplexPass!@#123", 35.0)
        );
    }



}
