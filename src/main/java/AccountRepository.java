import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(long id);

    void updateBalance(Account account);
    void saveTransaction(Transaction transaction);

    List<Transaction> findTransactionsByAccountId(long accountId);
}