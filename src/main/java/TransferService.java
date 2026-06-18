import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class TransferService {
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private static long transactionIdSequence = 1;

    public TransferService(AccountRepository accountRepository, NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
    }

    public Transaction executeTransfer(long fromAccountId, long toAccountId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        Optional<Account> sourceOptional = accountRepository.findById(fromAccountId);
        if (sourceOptional.isEmpty()) {
            throw new IllegalArgumentException("Source account not found");
        }
        Account source = sourceOptional.get();

        Optional<Account> destinationOptional = accountRepository.findById(toAccountId);
        if (destinationOptional.isEmpty()) {
            throw new IllegalArgumentException("Destination account not found");
        }
        Account destination = destinationOptional.get();

        if (source.isBlocked() || destination.isBlocked()) {
            throw new IllegalStateException("Cannot transfer funds involving blocked accounts");
        }

        if (source.getBalance() < amount) {
            notificationService.sendNotification(source.getId(), "Transfer failed: Insufficient funds");
            throw new IllegalArgumentException("Insufficient funds in source account");
        }

        source.setBalance(source.getBalance() - amount);
        destination.setBalance(destination.getBalance() + amount);

        accountRepository.updateBalance(source);
        accountRepository.updateBalance(destination);

        long newTransactionId = transactionIdSequence++;

        Transaction transaction = new Transaction(
                newTransactionId,
                fromAccountId,
                toAccountId,
                amount,
                "SUCCESS",
                Instant.now()
        );
        accountRepository.saveTransaction(transaction);

        notificationService.sendNotification(source.getId(), "Sent money to " + destination.getOwnerName());
        notificationService.sendNotification(toAccountId, "Received money from " + source.getOwnerName());

        if (amount > 10000.0) {
            notificationService.alertSecurityTeam("Large money transfer detected from account: " + fromAccountId);
        }

        return transaction;
    }

    public List<Transaction> getHistory(long accountId) {
        return accountRepository.findTransactionsByAccountId(accountId);
    }
}