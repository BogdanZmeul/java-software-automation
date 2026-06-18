import java.time.Instant;

public class Transaction {
    private final long id;
    private final long sourceAccountId;
    private final long destinationAccountId;
    private final double amount;
    private final String status;
    private final Instant timestamp;

    public Transaction(long id, long sourceAccountId, long destinationAccountId, double amount, String status, Instant timestamp) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public long getSourceAccountId() {
        return sourceAccountId;
    }

    public long getDestinationAccountId() {
        return destinationAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}