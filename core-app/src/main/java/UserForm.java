import compile.GenerateValidation;
import runtime.*;

@GenerateValidation(className = "ProcessedUserForm")
public class UserForm {
    @CheckEmpty
    String name;
    @CheckEmail
    String email;
    @CheckPhoneNumber
    String phoneNumber;
    @CheckPassword
    String password;
    @CheckRange(min = 18, max = 100)
    int age;
    @CheckRange(min = 1, max = 250)
    double weight;
}
