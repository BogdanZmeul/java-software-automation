public interface NotificationSenderService {
    void sendNotification(long userId, String message);
    void alertSecurityTeam(String message);
}