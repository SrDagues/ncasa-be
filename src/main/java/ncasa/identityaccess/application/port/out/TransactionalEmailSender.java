package ncasa.identityaccess.application.port.out;

public interface TransactionalEmailSender {
    void send(TransactionalEmail email);
}
