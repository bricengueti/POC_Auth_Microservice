package cm.tchongoue.auth_service.exception;

// PasswordMismatchException.java
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) {
        super(message);
    }
}
