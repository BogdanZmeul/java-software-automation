public class Account {
    private final long id;
    private final String ownerName;
    private double balance;
    private boolean blocked;

    public Account(long id, String ownerName, double balance, boolean blocked) {
        this.id = id;
        this.ownerName = ownerName;
        this.balance = balance;
        this.blocked = blocked;
    }

    public long getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}