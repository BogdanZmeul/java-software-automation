public interface NotificationService {
    void sendNotification(long userId, String message);
    void alertSecurityTeam(String message);
}