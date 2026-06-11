import java.util.regex.Pattern;

public class UserFormProcessor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    public void processForm(String email, String password, double age) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format!");
        }

        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Invalid password! Must be 8+ characters with letters and numbers.");
        }

        if (age < 18.0 || age > 120.0) {
            throw new IllegalArgumentException("Age must be between 18 and 120!");
        }

        System.out.println("Processing registration for: " + email);
    }
}