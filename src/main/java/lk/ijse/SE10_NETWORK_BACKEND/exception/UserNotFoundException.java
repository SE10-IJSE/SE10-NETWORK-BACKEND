package lk.ijse.SE10_NETWORK_BACKEND.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
