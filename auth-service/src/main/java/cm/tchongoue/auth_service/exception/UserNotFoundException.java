package cm.tchongoue.auth_service.exception;

// UserNotFoundException.java

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
