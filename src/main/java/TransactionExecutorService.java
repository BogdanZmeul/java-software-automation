import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class TransactionExecutorService {
    private final AccountRepository accountRepository;
    private final CommissionService commissionService;
    private final NotificationSenderService notificationSenderService;
    private static long transactionIdSequence = 1L;
    private static final double LARGE_TRANSFER_AMOUNT = 10000.0;

    public TransactionExecutorService(AccountRepository accountRepository, CommissionService commissionService, NotificationSenderService notificationSenderService) {
        this.accountRepository = accountRepository;
        this.commissionService = commissionService;
        this.notificationSenderService = notificationSenderService;
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

        double commission = commissionService.calculateCommission(amount);
        double totalCost = amount + commission;

        if (source.getBalance() < totalCost) {
            notificationSenderService.sendNotification(source.getId(), "Transfer failed: not enough funds");
            throw new IllegalArgumentException("Not enough funds in source account");
        }

        source.setBalance(source.getBalance() - totalCost);
        destination.setBalance(destination.getBalance() + amount);

        accountRepository.updateBalance(source);
        accountRepository.updateBalance(destination);

        long newTransactionId = transactionIdSequence++;

        Transaction transaction = new Transaction(newTransactionId, fromAccountId,
                toAccountId, amount, "SUCCESS", Instant.now()
        );
        accountRepository.saveTransaction(transaction);

        notificationSenderService.sendNotification(source.getId(), "Sent money to " + destination.getOwnerName());
        notificationSenderService.sendNotification(toAccountId, "Received money from " + source.getOwnerName());

        if (amount > LARGE_TRANSFER_AMOUNT) {
            notificationSenderService.alertSecurityTeam("Large money transfer detected from account: " + fromAccountId);
        }

        return transaction;
    }

    public List<Transaction> getHistory(long accountId) {
        return accountRepository.findTransactionsByAccountId(accountId);
    }
}