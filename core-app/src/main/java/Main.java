public class Main {
    public static void main(String[] args) {

        System.out.println("Testing invalid object:");
        ProcessedUserForm badUser = new ProcessedUserForm("", "not-an-email", "123", "weak", 15, 30.5);
        RuntimeValidator.validate(badUser);

        System.out.println("Testing valid object:");
        ProcessedUserForm goodUser = new ProcessedUserForm("john_doe", "john@example.com",
                "+1234567890", "StrongPass1", 25, 75.5);
        RuntimeValidator.validate(goodUser);
    }
}