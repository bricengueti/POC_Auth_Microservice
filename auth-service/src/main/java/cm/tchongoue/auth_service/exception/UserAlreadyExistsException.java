package cm.tchongoue.auth_service.exception;

// UserAlreadyExistsException.java

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}