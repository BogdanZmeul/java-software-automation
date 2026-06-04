import runtime.CheckEmpty;
import runtime.CheckEmail;
import runtime.CheckPassword;
import runtime.CheckPhoneNumber;
import runtime.CheckRange;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class RuntimeValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    public static boolean validate(Object target) {
        Class<?> clazz = target.getClass();
        System.out.println("Starting validation for: " + clazz.getSimpleName());
        boolean hasErrors = false;

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(target);
                String strValue = (value != null) ? value.toString() : "";

                if (field.isAnnotationPresent(CheckEmpty.class)) {
                    if (value == null || strValue.trim().isEmpty()) {
                        System.out.println("Field '" + field.getName() + "' cannot be empty!");
                        hasErrors = true;
                    }
                }

                if (field.isAnnotationPresent(CheckEmail.class) && !strValue.isEmpty()) {
                    if (!EMAIL_PATTERN.matcher(strValue).matches()) {
                        System.out.println("Field '" + field.getName() + "' has invalid email format.");
                        hasErrors = true;
                    }
                }

                if (field.isAnnotationPresent(CheckPhoneNumber.class) && !strValue.isEmpty()) {
                    if (!PHONE_PATTERN.matcher(strValue).matches()) {
                        System.out.println("Field '" + field.getName() + "' has invalid phone format.");
                        hasErrors = true;
                    }
                }

                if (field.isAnnotationPresent(CheckPassword.class) && !strValue.isEmpty()) {
                    if (!PASSWORD_PATTERN.matcher(strValue).matches()) {
                        System.out.println("Field '" + field.getName() + "' is weak. Needs 8+ chars, letter and number.");
                        hasErrors = true;
                    }
                }

                if (field.isAnnotationPresent(CheckRange.class)) {
                    CheckRange range = field.getAnnotation(CheckRange.class);
                    if (value instanceof Number) {
                        double numValue = ((Number) value).doubleValue();
                        if (numValue < range.min() || numValue > range.max()) {
                            System.out.println("Field '" + field.getName() + "' value " + numValue +
                                    " is out of bounds [" + range.min() + " - " + range.max() + "]");
                            hasErrors = true;
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                System.out.println("Failed to read field: " + field.getName());
            }
        }

        if (!hasErrors) {
            System.out.println("Object is valid.");
        }
        return !hasErrors;
    }
}